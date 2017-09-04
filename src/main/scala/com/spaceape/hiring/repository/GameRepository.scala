package com.spaceape.hiring.repository

import com.mongodb.casbah.Imports.MongoDBObject
import com.mongodb.casbah.WriteConcern
import com.mongodb.util.{JSON => MongoJSON}
import com.spaceape.hiring.model.{Game, Move, PlayerScore}
import com.spaceape.hiring.util.JsonUtil.fromJson
import org.bson.types.ObjectId

class GameRepository (mongoDbWrapper: MongoDbWrapper) {
  val collection = {
    val col = mongoDbWrapper.mongoDB("games")
    col.createIndex(MongoDBObject("player1" -> 1, "player2" -> 1), MongoDBObject("partialFilterExpression" -> MongoDBObject("gameOver" -> false ), "unique" -> true))
    col.createIndex(MongoDBObject("winnerId" -> 1))
    col
  }

  def createGame(game: Game): String = {

    val gameObject = MongoDBObject(
      "player1" -> game.player1,
      "player2" -> game.player2,
      "gameOver" -> game.gameOver,
      "nextTurn" -> game.nextTurn,
      "cells" -> game.cells,
      "moveInProgress" -> game.moveInProgress
    )
    val writeResult = collection.insert(gameObject, WriteConcern.Safe)
    gameObject.get("_id").asInstanceOf[ObjectId].toString
  }

  def getGame(gameId: String): Option[Game] = {
    val result  = collection.findOneByID(new ObjectId(gameId))
    result.map(parseGame(_))
  }

  def updateMove(gameId: String, move: Move): Option[Game] = {
    val result  = collection.findAndModify(
      query = MongoDBObject("_id" -> new ObjectId(gameId), "nextTurn" -> move.playerId, "gameOver" -> false, "moveInProgress" -> false, s"cells.${move.x}.${move.y}" -> null),
      fields = null,
      sort = null,
      remove = false,
      update = MongoDBObject("$set" -> MongoDBObject(s"cells.${move.x}.${move.y}" -> move.playerId, "moveInProgress" -> true)),
      returnNew = true,
      upsert = false
    )
    result.map(parseGame(_))
  }

  def getLeaders(numberOfLeaders: Int): List[PlayerScore] = {
      val pipeline = List(
        MongoDBObject("$match" -> MongoDBObject("winnerId" -> MongoDBObject("$ne" -> null))),
        MongoDBObject("$group" -> MongoDBObject("_id" -> "$winnerId", "score" -> MongoDBObject("$sum" -> 1))),
        MongoDBObject("$sort" -> MongoDBObject("_id" -> 1)),
        MongoDBObject("$limit" -> numberOfLeaders)
      )
      val output = collection.aggregate(pipeline)
      output.results.map(parsePlayerScore(_)).toList
  }


  def completeMove(gameId: String, nextTurn: Option[String], winnerId: Option[String], gameOver: Boolean) {
    collection.findAndModify(
      MongoDBObject("_id" -> new ObjectId(gameId)),
      MongoDBObject("$set" ->
        MongoDBObject("moveInProgress" -> false, "nextTurn" -> nextTurn, "winnerId" -> winnerId, "gameOver" -> gameOver))
    )
  }

  private def parseGame(record: Any): Game = {
    fromJson[Game](MongoJSON.serialize(record))
  }

  private def parsePlayerScore(record: Any): PlayerScore = {
    fromJson[PlayerScore](MongoJSON.serialize(record))
  }

}
