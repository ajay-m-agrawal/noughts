package com.spaceape.hiring;

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.spaceape.hiring.repository.{GameRepository, MongoDbWrapper}
import com.spaceape.hiring.resources.{LeadersResource, NoughtsResource}
import com.spaceape.hiring.service.GameService

object NoughtsApplication {
  def main(args: Array[String]) {
    new NoughtsApplication().run(args:_*)
  }
}

class NoughtsApplication extends Application[NoughtsConfiguration] {
  override def getName() = "noughts"

  override def initialize(bootstrap: Bootstrap[NoughtsConfiguration]) {
    
  }

  override def run(configuration: NoughtsConfiguration, environment: Environment) {
    val mongoDbWrapper = new MongoDbWrapper(configuration.gameMongoURI)
    val gameRepository = new GameRepository(mongoDbWrapper)
    val noughtsResource = new NoughtsResource(gameService = new GameService(gameRepository))
    val leadersResource = new LeadersResource(gameService = new GameService(gameRepository))
    val objectMapper = new ObjectMapper()
    objectMapper.registerModule(DefaultScalaModule)
    val errorHandler = new NoughtsExceptionMapper()
    environment.jersey().register(errorHandler)
    environment.jersey().register(new JacksonMessageBodyProvider(objectMapper))
    environment.jersey().register(noughtsResource)
    environment.jersey().register(leadersResource)
  }

}
