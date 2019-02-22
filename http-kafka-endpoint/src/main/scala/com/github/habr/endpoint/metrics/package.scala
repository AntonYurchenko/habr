package com.github.habr.endpoint

import java.io.Writer

import io.prometheus.client.{CollectorRegistry, Counter}
import io.prometheus.client.exporter.common.TextFormat
import io.vertx.core.buffer.Buffer
import io.vertx.scala.ext.web.RoutingContext


/**
  * The package object contains metrics of the application and handler of request to metric page.
  * Metrics prints on target page in prometheus format.
  *
  * @author Anton Yurcheko
  */
package object metrics {

  val httpRequests: Counter = Counter.build()
    .name("http_request_counter")
    .help("Counter of http requests to REST")
    .labelNames("status")
    .register()

  val kafkaMessages: Counter = Counter.build()
    .name("kafka_message_counter")
    .help("Counter of sent messages to kafka")
    .labelNames("status")
    .register()


  /**
    * The class is custom writer for creation metrics page
    */
  class MetricsWriter extends Writer {

    val buffer: Buffer = Buffer.buffer

    override def write(buf: Array[Char], off: Int, len: Int): Unit =
      buffer.appendString(new String(buf, off, len))

    override def flush(): Unit = {}

    override def close(): Unit = {}

  }


  /**
    * The method is handler for request of metrics page
    *
    * @param rc RoutingContext
    */
  def metricsHandler(rc: RoutingContext): Unit = {
    val wr = new MetricsWriter
    TextFormat.write004(wr, CollectorRegistry.defaultRegistry.metricFamilySamples())
    rc.response() setStatusCode 200 putHeader("Content-Type", TextFormat.CONTENT_TYPE_004) end wr.buffer
  }

}
