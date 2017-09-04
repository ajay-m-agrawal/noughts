package com.spaceape.hiring.util

import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonUtil {

    val mapper = new ObjectMapper() with ScalaObjectMapper
    val module = new SimpleModule()

     mapper.registerModule(module)
     mapper.registerModule(DefaultScalaModule)
     mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
     mapper.setSerializationInclusion(NON_NULL)


      def toJson(value: Map[Symbol, Any]): String = {
        toJson(value map { case (k,v) => k.name -> v})
      }

      def toJson(value: Any): String = {
        mapper.writeValueAsString(value)
      }

      def toMap[V](json:String)(implicit m: Manifest[V]) = fromJson[Map[String,V]](json)

      def fromJson[T](json: String)(implicit m : Manifest[T]): T = {
        mapper.readValue[T](json)
      }
  }
