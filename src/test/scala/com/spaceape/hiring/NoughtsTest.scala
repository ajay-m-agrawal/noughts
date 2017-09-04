
package com.spaceape.hiring

import javax.ws.rs.core.Response.Status
import javax.ws.rs.core.Response.Status.{FORBIDDEN, CONFLICT}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.mashape.unirest.http.{HttpResponse, Unirest}
import com.mongodb.casbah
import com.mongodb.casbah.Imports.MongoConnection
import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.commons.MongoDBObject
import com.spaceape.hiring.model.{GameState, Move, NewGameResponse, PlayerScore}
import io.dropwizard.testing.junit.DropwizardAppRule
import org.junit.{Before, ClassRule, Test}
import org.scalatest.Matchers
import org.scalatest.junit.JUnitSuite


object NoughtsTest {
	@ClassRule def rule = new DropwizardAppRule[NoughtsConfiguration](classOf[NoughtsApplication], "test.yml")
}

class NoughtsTest extends JUnitSuite with Matchers {

  val server = "http://localhost:9080/"
  val baseUrl = s"$server/game"

  private val uri = MongoURI("mongodb://localhost/noughts")

  val mongoDB: casbah.MongoDB = MongoConnection(uri).getDB(uri.database.get)
  val collection = mongoDB("games")

  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  @Before
  def setup {
    collection.remove(MongoDBObject.newBuilder.result)
  }


  def initGame(player1Id: String, player2Id: String) = {
    val response: HttpResponse[String] = initGameAndReturnHTTPResponse(player1Id, player2Id)

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when creating game: ${response.getBody}")
    }

    objectMapper.readValue(response.getBody, classOf[NewGameResponse])
  }

  private def initGameAndReturnHTTPResponse(player1Id: String, player2Id: String) = {
    val response = Unirest.post(baseUrl)
      .queryString("player1Id", player1Id)
      .queryString("player2Id", player2Id)
      .asString()
    response
  }

  def runMoves(gameId: String, moves: Seq[Move]) = {
    moves.foreach(move => {
      val response = Unirest.put(s"$baseUrl/$gameId")
        .header("Content-Type", "application/json")
        .body(objectMapper.writeValueAsString(move))
        .asString()

      if(response.getStatus != Status.ACCEPTED.getStatusCode) {
        throw new RuntimeException(s"${response.getStatus} when making move: ${response.getBody}")
      }
    })
  }

  def runMove(gameId: String, move: Move) = {
      Unirest.put(s"$baseUrl/$gameId")
        .header("Content-Type", "application/json")
        .body(objectMapper.writeValueAsString(move))
        .asString()
  }

  def getState(gameId: String) = {
    val response = Unirest.get(s"$baseUrl/$gameId").asString()

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when getting state: ${response.getBody}")
    }

    objectMapper.readValue(response.getBody, classOf[GameState])
  }

  def getLeaders = {
    val response = Unirest.get(s"$server/leaders").asString()

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when getting state: ${response.getBody}")
    }

    objectMapper.readValue(response.getBody, classOf[List[PlayerScore]])
  }

	@Test
	def testPlayer1WinByRow {
    val gameId = initGame("1", "2").gameId
    getState(gameId) should be (GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 0, 0),
      Move("2", 1, 0),
      Move("1", 0, 1),
      Move("2", 1, 1),
      Move("1", 0, 2)))

    getState(gameId) should be (GameState(Some("1"), true))

	}


  @Test
  def testPlayer2WinByColumn {
    val gameId = initGame("1", "2").gameId
    getState(gameId) should be (GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 0),
      Move("1", 0, 1),
      Move("2", 1, 0),
      Move("1", 0, 2),
      Move("2", 2, 0)))

    getState(gameId) should be (GameState(Some("2"), true))

  }

  @Test
  def testPlayer1WinByDiagonal {
    val gameId = initGame("1", "2").gameId
    getState(gameId) should be(GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 1),
      Move("1", 0, 0),
      Move("2", 1, 0),
      Move("1", 2, 2)))

    getState(gameId) should be(GameState(Some("1"), true))

  }

  @Test
  def testDrawGame {
    val gameId = initGame("1", "2").gameId
    getState(gameId) should be(GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 0, 0),
      Move("2", 0, 1),
      Move("1", 0, 2),
      Move("2", 1, 0),
      Move("1", 1, 2),
      Move("2", 1, 1),
      Move("1", 2, 2),
      Move("2", 2, 0),
      Move("1", 2, 1)))

    getState(gameId) should be(GameState(None, true))

  }

  @Test
  def testUnfinishedGameRejection: Unit ={
    val gameId = initGame("3", "4").gameId
    val response = initGameAndReturnHTTPResponse("3", "4")
    response.getStatus should be (FORBIDDEN.getStatusCode)
    response.getBody should be ("Game in progress between 3 and 4")
  }

  @Test
  def testOccupiedCellMoveRejection: Unit ={


    var gameId = initGame("1", "2").gameId
    getState(gameId) should be (GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 0),
      Move("1", 0, 1)
      ))

    val response = runMove(gameId, Move("2", 0, 1))
    response.getStatus should be (CONFLICT.getStatusCode)
    response.getBody should be (s"Cell in $gameId at position x = 0 and y = 1 is already filled")
  }

  @Test
  def testOutOfTurnMoveRejection: Unit ={


    var gameId = initGame("1", "2").gameId
    getState(gameId) should be (GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 0),
      Move("1", 0, 1)
    ))

    val response = runMove(gameId, Move("1", 2, 1))
    response.getStatus should be (CONFLICT.getStatusCode)
    response.getBody should be ("Next turn belongs to 2 and not 1")
  }

  @Test
  def testLeaderBoard {

    var gameId = initGame("1", "2").gameId
    getState(gameId) should be (GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 0),
      Move("1", 0, 1),
      Move("2", 1, 0),
      Move("1", 0, 2),
      Move("2", 2, 0)))

    gameId = initGame("1", "2").gameId
    getState(gameId) should be(GameState(None, false))
    runMoves(gameId, Seq(
      Move("1", 1, 1),
      Move("2", 0, 1),
      Move("1", 0, 0),
      Move("2", 1, 0),
      Move("1", 2, 2)))


    val leader = getLeaders
    leader.size should be (2)
  }



}