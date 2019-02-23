enablePlugins(PackPlugin)

name := "http-kafka-endpoint"
version := "0.1"
scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "io.vertx" %% "vertx-lang-scala" % "3.6.3",
  "io.vertx" %% "vertx-web-scala" % "3.6.3",
  "io.vertx" %% "vertx-kafka-client-scala" % "3.6.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.typesafe" % "config" % "1.3.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "io.prometheus" % "simpleclient" % "0.5.0",
  "io.prometheus" % "simpleclient_httpserver" % "0.5.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// Settings of packing and running
packMain := Map(name.value -> "com.github.habr.endpoint.app.Run")
packGenerateWindowsBatFile := false
packJvmOpts := Map(name.value -> Seq("-Xms3g", "-Xmx5g"))