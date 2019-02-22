package com.github.habr.endpoint.app

import com.github.habr.endpoint.config.Config
import com.github.habr.endpoint.http.HttpConsumer
import com.github.habr.endpoint.kafka.KafkaSender
import com.typesafe.scalalogging.LazyLogging
import io.vertx.scala.core.Vertx

/**
  * The object is entry point of the application.
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
