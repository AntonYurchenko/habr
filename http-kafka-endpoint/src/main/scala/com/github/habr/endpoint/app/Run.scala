package com.github.habr.endpoint.app

import com.github.habr.endpoint.config.Config
import com.github.habr.endpoint.http.HttpConsumer
import com.github.habr.endpoint.kafka.KafkaSender
import com.typesafe.scalalogging.LazyLogging
import io.vertx.scala.core.Vertx

/**
  * The object is entry point of the application.
  * The application is example of http REST service based on Vert.x framework for sending POST requests to kafka topic.
  * Each logical part of the service is vert.x vertical which can be easy parallelized by changing of configuration.
  *
  * @see https://vertx.io
  * @author Anton Yurchenko
  */
object Run extends App with LazyLogging {

  logger.info("Start application")

  // Initialisation and print configuration
  val vertx = Vertx.vertx()
  Config.printConfig()

  // Deploy all verticals
  vertx.deployVerticle(HttpConsumer.name, HttpConsumer.options)
  vertx.deployVerticle(KafkaSender.name, KafkaSender.options)

}
