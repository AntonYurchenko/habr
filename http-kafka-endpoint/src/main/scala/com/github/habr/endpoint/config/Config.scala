package com.github.habr.endpoint.config

import scala.collection.mutable
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging


/**
  * The object makes mapping values from application.conf file to objects
  *
  * @author Anton Yurchenko
  */
object Config extends LazyLogging {

  private val conf = ConfigFactory.load()

  import conf._

  // http
  lazy val httpHost: String = getString("http.host")
  lazy val httpPort: Int = getInt("http.port")

  // kafka
  lazy val kafkaTopic: String = getString("kafka.topic")
  lazy val kafkaInstances: Int = getInt("kafka.instances")
  lazy val kafkaPoolSize: Int = getInt("kafka.poolSize")
  lazy val kafkaConf: mutable.Map[String, String] = mutable.Map(
    "bootstrap.servers" -> getString("kafka.config.bootstrap.servers"),
    "key.serializer" -> getString("kafka.config.key.serializer"),
    "value.serializer" -> getString("kafka.config.value.serializer")
  )


  /**
    * The method initialises all configuration and prints it
    */
  def printConfig(): Unit = {
    logger.info(s"----------> Configuration <----------")
    logger.info(s"Http host: $httpHost")
    logger.info(s"Http port: $httpPort")
    logger.info(s"Kafka target topic: $kafkaTopic")
    logger.info(s"Kafka instances: $kafkaInstances")
    logger.info(s"Kafka work pool size: $kafkaPoolSize")
    logger.info(s"Kafka producer configuration: $kafkaConf")
    logger.info(s"-------------------------------------")
  }

}
