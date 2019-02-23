package com.github.habr.endpoint.http

import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.ext.web.{Router, RoutingContext}
import io.vertx.scala.ext.web.handler.BodyHandler
import com.github.habr.endpoint.config.{Config => conf}
import com.github.habr.endpoint.http.HttpConsumer.{EmptyRequest, FieldNotFound, RequestException}
import com.github.habr.endpoint.kafka.KafkaSender
import com.github.habr.endpoint.metrics.{httpRequests, metricsHandler}
import com.typesafe.scalalogging.LazyLogging
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.DeploymentOptions

import scala.util.{Failure, Success, Try}


/**
  * The vertical contains http REST server.
  * It receives POST requests with JSON and sends them to KafkaSender by event bus.
  *
  * @see [[https://vertx.io/docs/vertx-core/scala/]]
  * @see [[https://vertx.io/docs/vertx-web/scala/]]
  * @author Anton Yurchenko
  */
class HttpConsumer extends ScalaVerticle with LazyLogging {

  // initialisation
  lazy val server: HttpServer = vertx.createHttpServer()
  lazy val router: Router = Router.router(vertx)


  /**
    * The method executes after starting of the vertical
    */
  override def start(): Unit = {

    // routing of request endpoints
    router.post("/events")
      .consumes("application/json")
      .produces("application/json")
      .handler(BodyHandler.create())
      .handler(eventHandler)
    router.get("/metrics").handler(metricsHandler)
    router.route.failureHandler(errorHandler)

    // start of http server on host and port from configuration
    server.requestHandler(router.accept).listenFuture(conf.httpPort, conf.httpHost).onComplete {
      case Success(_) => logger.info(s"Http server with id $deploymentID has been started")
      case Failure(ex) => logger.error(ex.getMessage)
    }

  }


  /**
    * The method executes before stopping of the vertical
    */
  override def stop(): Unit = server.closeFuture().onComplete {
    case Success(_) => logger.info(s"Http server with id $deploymentID has been stopped")
    case Failure(ex) => logger.error(ex.getMessage)
  }


  /**
    * The function is validator of received JSON.
    * It checks containing of fields 'user_id', 'event_name' and 'timestamp' into JSON.
    * If JSON is not valid function throw RequestException
    *
    * @see [[com.github.habr.endpoint.http.HttpConsumer.RequestException]]
    */
  @throws[RequestException]
  val validator: Option[JsonObject] => JsonObject = (_: Option[JsonObject]) match {
    case Some(json) if !json.containsKey("user_id") => throw new FieldNotFound("user_id")
    case Some(json) if !json.containsKey("event_name") => throw new FieldNotFound("event_name")
    case Some(json) if !json.containsKey("timestamp") => throw new FieldNotFound("timestamp")
    case Some(json) => json
    case None => throw new EmptyRequest
  }


  /**
    * The method is handler of POST requests to /events
    *
    * @param rc RoutingContext
    */
  def eventHandler(rc: RoutingContext): Unit = try {
    val body = validator(Try(rc.getBodyAsJson()).getOrElse(None))
    logger.debug(s"== POST /event ==> $body")
    vertx.eventBus().sendFuture[JsonObject](KafkaSender.name, body).onComplete {
      case Success(status) =>
        val response = status.body()
        rc.response().setStatusCode(200).end(response.toBuffer)
        httpRequests.labels("200").inc()
        logger.debug(s"<== POST /event == $response")
      case Failure(ex) => throw ex
    }
  } catch {
    case ex: Exception => rc.fail(ex)
  }


  /**
    * The method is handler of failure requests
    *
    * @param rc RoutingContext
    */
  def errorHandler(rc: RoutingContext): Unit = {
    rc.failure() match {
      case ex: RequestException =>
        rc.response().setStatusCode(400).end(Json.obj("error" -> ex.getMessage).toBuffer)
        httpRequests.labels("400").inc()
        logger.warn(ex.getMessage)
      case ex: Throwable =>
        rc.response().setStatusCode(500).end(Json.obj("error" -> "Internal Server Error").toBuffer)
        httpRequests.labels("500").inc()
        logger.error(ex.getMessage)
    }

  }
}

object HttpConsumer {


  /**
    * Name of the vertical
    */
  val name: String = ScalaVerticle.nameForVerticle[HttpConsumer]


  /**
    * Deploy options of the vertical (default for the vertical)
    */
  val options: DeploymentOptions = DeploymentOptions()


  /**
    * Parent exception for all bed requests
    *
    * @param msg String
    */
  class RequestException(msg: String) extends Exception(msg)


  /**
    * Exception for empty result
    */
  class EmptyRequest extends RequestException("Empty request")


  /**
    * Exception for not found field
    *
    * @param field String
    */
  class FieldNotFound(field: String) extends RequestException(s"Request should contain field: '$field'")

}
