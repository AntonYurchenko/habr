package com.github.habr.endpoint.http

import com.github.habr.endpoint.http.HttpConsumer._
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.json.Json
import org.scalatest.{FlatSpec, Matchers}

/**
  * Test for [[com.github.habr.endpoint.http.HttpConsumer]]
  *
  * @author Anton Yurchenko
  */
class HttpConsumerTest extends FlatSpec with Matchers {

  val validator: Option[JsonObject] => JsonObject = new HttpConsumer().validator

  "Empty request" should "throw EmptyRequest exception" in intercept[EmptyRequest](validator(None))

  "Request without field 'user_id'" should "throw FieldNotFound exception" in intercept[FieldNotFound] {
    val request = Json.obj("event_name" -> "click_to_banner", "timestamp" -> 1549583640)
    validator(Some(request))
  }

  "Request without field 'event_name'" should "throw FieldNotFound exception" in intercept[FieldNotFound] {
    val request = Json.obj("user_id" -> "test_user", "timestamp" -> 1549583640)
    validator(Some(request))
  }

  "Request without field 'timestamp'" should "throw FieldNotFound exception" in intercept[FieldNotFound] {
    val request = Json.obj("user_id" -> "test_user", "event_name" -> "click_to_banner")
    validator(Some(request))
  }

  "Request with all fields" should "be valid" in {
    val request = Json.obj("user_id" -> "test_user", "event_name" -> "click_to_banner", "timestamp" -> 1549583640)
    validator(Some(request)) shouldEqual request
  }

}
