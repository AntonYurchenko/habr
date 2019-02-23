package com.github.habr.endpoint.kafka

import com.typesafe.scalalogging.LazyLogging
import io.vertx.lang.scala.ScalaVerticle
import com.github.habr.endpoint.config.{Config => conf}
import com.github.habr.endpoint.metrics.kafkaMessages
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.json.Json
import io.vertx.scala.core.DeploymentOptions
import io.vertx.scala.core.eventbus.Message
import io.vertx.scala.kafka.client.producer.{KafkaProducer, KafkaProducerRecord}

import scala.util.{Failure, Success}


/**
  * The vertical is kafka provider.
  * It sends to kafka topic selected into application.conf messages from event bus.
  *
  * @see [[https://vertx.io/docs/vertx-core/scala/]]
  * @see [[https://vertx.io/docs/vertx-kafka-client/scala/]]
  * @author Anton Yurchenko
  */
class KafkaSender extends ScalaVerticle with LazyLogging {

  // initialisation kafka provider
  lazy val producer: KafkaProducer[Null, String] = KafkaProducer.create(vertx, conf.kafkaConf)


  /**
    * The method executes after starting of the vertical
    */
  override def start(): Unit = {

    // registration of handler for messages from event bus
    vertx.eventBus().consumer(KafkaSender.name, messageHandler)
    logger.info(s"Kafka sender with id $deploymentID has been started")
  }


  /**
    * The method executes before stopping of the vertical
    */
  override def stop(): Unit = producer.closeFuture().onComplete {
    case Success(_) => logger.info(s"Kafka sender with id $deploymentID has been stopped")
    case Failure(ex) => logger.error(ex.getMessage)
  }


  /**
    * The method is handler messages from event bus.
    * Received message send to kafka topic selected into application.conf.
    * After sending of the message handler replies by json with status in event bus.
    * If sending to kafka is successful then json contains status 'SUCCESS' and
    * number of partition into which was written message else json contains status 'FAILURE' and
    * description of exception.
    *
    * @param msg Message[JsonObject]
    */
  def messageHandler(msg: Message[JsonObject]): Unit = {
    val topic = conf.kafkaTopic
    val content = msg.body().toString
    val kafkaMessage = KafkaProducerRecord.create(topic, null, content)
    producer.writeFuture(kafkaMessage).onComplete {
      case Success(meta) =>
        val partition = meta.getPartition
        msg.reply(Json.obj("status" -> "SUCCESS", "partition" -> partition))
        kafkaMessages.labels("success").inc()
        logger.debug(s"== Kafka topic: $topic partition: $partition ==> $content")
      case Failure(ex) =>
        msg.reply(Json.obj("status" -> "FAILURE", "description" -> ex.getMessage))
        kafkaMessages.labels("failure").inc()
        logger.error(ex.getMessage)
    }
  }

}

object KafkaSender {


  /**
    * Name of the vertical
    */
  val name: String = ScalaVerticle.nameForVerticle[KafkaSender]


  /**
    * Deploy options of the vertical.
    * The vertical has status worker and executing into separate pool of threads.
    * Size of threads pool and count instances of the vertical selected into application.conf.
    */
  val options: DeploymentOptions = DeploymentOptions()
    .setWorker(true)
    .setWorkerPoolName("kafka-sender")
    .setWorkerPoolSize(conf.kafkaPoolSize)
    .setInstances(conf.kafkaInstances)

}
