package com.spaceape.hiring

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.ACCEPTED

import com.spaceape.hiring.model.{Game, GameState, Move, NewGameResponse}
import com.spaceape.hiring.resources.NoughtsResource
import com.spaceape.hiring.service.GameService
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar


class NoughtsResourceTest extends FunSuite with MockitoSugar{

  trait Context{
    val gameService = mock[GameService]
    val noughtsResource = new NoughtsResource(gameService)
  }

  test("testCreateGame must create a new game") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "ABCD"

      when(gameService.createGame(player1, player2)) thenReturn gameId

      val response: NewGameResponse = noughtsResource.createGame(player1, player2)

      assert(response == NewGameResponse(gameId))
    }
  }

  test("testGetGame must return game by Id") {
    new Context {

      val gameId = "ABCD"
      val newGame = Game("1", "2")

      when(gameService.getGame(gameId)) thenReturn newGame

      val gameState : GameState = noughtsResource.getGame(gameId)

      assert(gameState == GameState(None, gameOver = false))
    }
  }

  test("testMakeMove must return accepted response") {
    new Context {

      val gameId = "ABCD"
      val move = Move("1", 0, 0)


      val response : Response = noughtsResource.makeMove(gameId, move)
      assert(response.getStatus() == ACCEPTED.getStatusCode)
      verify(gameService).makeMove(gameId, move)
    }
  }



}
