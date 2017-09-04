package com.spaceape.hiring.service

import javax.ws.rs.core.Response.Status.{BAD_REQUEST, CONFLICT, FORBIDDEN, NOT_FOUND}

import com.mongodb.DuplicateKeyException
import com.spaceape.hiring.NoughtsException
import com.spaceape.hiring.model.{Game, Move, PlayerScore}
import com.spaceape.hiring.repository.GameRepository
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar


class GameServiceTest extends FunSuite with MockitoSugar{

  trait Context{
    val gameRepository = mock[GameRepository]
    val gameService = new GameService(gameRepository)
  }

  test("testCreateGame must create a new game") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"

      when(gameRepository.createGame(Game(player1, player2))) thenReturn gameId


      assert(gameService.createGame(player1, player2) == gameId)
    }
  }


  test("testCreateGame must throw NoughtsException when another game with same players in progress") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"

      when(gameRepository.createGame(Game(player1, player2))) thenThrow classOf[DuplicateKeyException]

      val exception : NoughtsException = intercept[NoughtsException]{
        gameService.createGame(player1, player2)
      }

      assert(exception.code == FORBIDDEN.getStatusCode)
      assert(exception.message == s"Game in progress between $player1 and $player2")

    }
  }

  test("testCreateGame must throw NoughtsException when both players are same") {
    new Context {

      val player1 = "1234"
      val player2 = "1234"

      val gameId = "59ad573877c8946ab2639515"


      val exception : NoughtsException = intercept[NoughtsException]{
        gameService.createGame(player1, player2)
      }

      verify(gameRepository, times(0)).createGame(Game(player1, player2))
      assert(exception.code == BAD_REQUEST.getStatusCode)
      assert(exception.message == "Need two different players to initiate a game")

    }
  }

  test("testGetGame must return game object by id") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val mockGame = Game(player1, player2)

      when(gameRepository.getGame(gameId)) thenReturn Option(mockGame)


      assert(gameService.getGame(gameId) == mockGame)
    }
  }

  test("testGetGame must throw NoughtsException when game not found") {
    new Context {

      val gameId = "59ad573877c8946ab2639515"

      when(gameRepository.getGame(gameId)) thenReturn None


      val exception : NoughtsException = intercept[NoughtsException]{
        gameService.getGame(gameId)
      }

      assert(exception.code == NOT_FOUND.getStatusCode)
      assert(exception.message == s"$gameId does not exist")

    }
  }

  test("testGetGame must throw NoughtsException when game id not valid") {
    new Context {

      val gameId = "invalid_format"



      val exception : NoughtsException = intercept[NoughtsException]{
        gameService.getGame(gameId)
      }

      assert(exception.code == BAD_REQUEST.getStatusCode)
      assert(exception.message == s"$gameId is not a valid game id")

      verify(gameRepository, times(0)).getGame(gameId)

    }
  }

  test("testMakeMove must update the requested cell with player Id") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 0)
      val cells = List(
        List(Option(player1), None, None),
        List(None, None, None),
        List(None, None, None)
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn Option(game)

      gameService.makeMove(gameId, move)

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository,times(0)).getGame(gameId)
      verify(gameRepository).completeMove(gameId, Option(player2), None, gameOver = false)
    }
  }

  test("testMakeMove must update the requested cell and calculate winner by row") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 2)
      val cells = List(
        List(Option(player1), Option(player1), Option(player1)),
        List(Option(player2), Option(player2), None),
        List(None, None, None)
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn Option(game)

      gameService.makeMove(gameId, move)

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository,times(0)).getGame(gameId)
      verify(gameRepository).completeMove(gameId, None, Option(player1), gameOver = true)
    }
  }

  test("testMakeMove must update the requested cell and calculate winner by column") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 2)
      val cells = List(
        List(Option(player1), Option(player2), None),
        List(Option(player1), Option(player2), None),
        List(Option(player1), None, None)
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn Option(game)

      gameService.makeMove(gameId, move)

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository,times(0)).getGame(gameId)
      verify(gameRepository).completeMove(gameId, None, Option(player1), gameOver = true)
    }
  }

  test("testMakeMove must update the requested cell and calculate winner by diagonal") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 2)
      val cells = List(
        List(Option(player1), Option(player2), None),
        List(Option(player2), Option(player1), None),
        List(None, None, Option(player1))
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn Option(game)

      gameService.makeMove(gameId, move)

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository,times(0)).getGame(gameId)
      verify(gameRepository).completeMove(gameId, None, Option(player1), gameOver = true)
    }
  }

  test("testMakeMove must update the requested cell and calculate draw") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 2)
      val cells = List(
        List(Option(player1), Option(player2), Option(player1)),
        List(Option(player2), Option(player2), Option(player1)),
        List(Option(player1), Option(player1), Option(player2))
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn Option(game)

      gameService.makeMove(gameId, move)

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository,times(0)).getGame(gameId)
      verify(gameRepository).completeMove(gameId, None, None, gameOver = true)
    }
  }

    test("testMakeMove must return NoughtsException when game does not exist") {
      new Context {

        val player1 = "1234"
        val player2 = "5678"

        val gameId = "59ad573877c8946ab2639515"
        val move = Move(player1, 0, 2)

        when(gameRepository.updateMove(gameId, move)) thenReturn None
        when(gameRepository.getGame(gameId)) thenReturn None

        val exception: NoughtsException = intercept[NoughtsException] {
          gameService.makeMove(gameId, move)
        }

        verify(gameRepository).updateMove(gameId, move)
        verify(gameRepository).getGame(gameId)
        verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

        assert(exception.code == NOT_FOUND.getStatusCode)
        assert(exception.message == s"$gameId does not exist")
      }
    }

  test("testMakeMove must return NoughtsException when game id is not in a valid format") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "invalid_format"
      val move = Move(player1, 0, 2)

      val exception: NoughtsException = intercept[NoughtsException] {
        gameService.makeMove(gameId, move)
      }

      verify(gameRepository, times(0)).updateMove(gameId, move)
      verify(gameRepository, times(0)).getGame(gameId)
      verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

      assert(exception.code == BAD_REQUEST.getStatusCode)
      assert(exception.message == s"$gameId is not a valid game id")
    }
  }

    test("testMakeMove must return NoughtsException when game is over") {
      new Context {

        val player1 = "1234"
        val player2 = "5678"

        val gameId = "59ad573877c8946ab2639515"
        val move = Move(player1, 0, 2)
        val cells = List(
          List(Option(player1), Option(player2), None),
          List(Option(player2), Option(player1), None),
          List(None, None, Option(player1))
        )
        val game = Game(player1, player2, None, gameOver = true, Option(player1), cells, moveInProgress = false)

        when(gameRepository.updateMove(gameId, move)) thenReturn None
        when(gameRepository.getGame(gameId)) thenReturn Option(game)

        val exception: NoughtsException = intercept[NoughtsException] {
          gameService.makeMove(gameId, move)
        }

        verify(gameRepository).updateMove(gameId, move)
        verify(gameRepository).getGame(gameId)
        verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

        assert(exception.code == FORBIDDEN.getStatusCode)
        assert(exception.message == s"$gameId is over")
      }
    }


  test("testMakeMove must return NoughtsException when move in progress") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 2)
      val cells = List(
        List(Option(player1), Option(player2), None),
        List(Option(player2), Option(player1), None),
        List(None, None, Option(player1))
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = true)

      when(gameRepository.updateMove(gameId, move)) thenReturn None
      when(gameRepository.getGame(gameId)) thenReturn Option(game)

      val exception: NoughtsException = intercept[NoughtsException] {
        gameService.makeMove(gameId, move)
      }

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository).getGame(gameId)
      verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

      assert(exception.code == FORBIDDEN.getStatusCode)
      assert(exception.message == s"Move in progress for $gameId")
    }
  }


  test("testMakeMove must return NoughtsException when cell is already occupied") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player1, 0, 0)
      val cells = List(
        List(Option(player1), Option(player2), None),
        List(Option(player2), Option(player1), None),
        List(None, None, Option(player1))
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = false)

      when(gameRepository.updateMove(gameId, move)) thenReturn None
      when(gameRepository.getGame(gameId)) thenReturn Option(game)

      val exception: NoughtsException = intercept[NoughtsException] {
        gameService.makeMove(gameId, move)
      }

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository).getGame(gameId)
      verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

      assert(exception.code == CONFLICT.getStatusCode)
      assert(exception.message == "Cell in 59ad573877c8946ab2639515 at position x = 0 and y = 0 is already filled")
    }
  }

  test("testMakeMove must return NoughtsException when it is not requested player's turn") {
    new Context {

      val player1 = "1234"
      val player2 = "5678"

      val gameId = "59ad573877c8946ab2639515"
      val move = Move(player2, 0, 2)
      val cells = List(
        List(Option(player1), Option(player2), None),
        List(Option(player2), Option(player1), None),
        List(None, None, Option(player1))
      )
      val game = Game(player1, player2, None, gameOver = false, Option(player1), cells, moveInProgress = false)

      when(gameRepository.updateMove(gameId, move)) thenReturn None
      when(gameRepository.getGame(gameId)) thenReturn Option(game)

      val exception: NoughtsException = intercept[NoughtsException] {
        gameService.makeMove(gameId, move)
      }

      verify(gameRepository).updateMove(gameId, move)
      verify(gameRepository).getGame(gameId)
      verify(gameRepository, times(0)).completeMove(gameId, None, None, gameOver = true)

      assert(exception.code == CONFLICT.getStatusCode)
      assert(exception.message == "Next turn belongs to 1234 and not 5678")
    }
  }

  test("getLeaderBoard must return top n leaders"){

    new Context {
      val leaders : List[PlayerScore] = List(PlayerScore("1", 10), PlayerScore("2", 5))
      when(gameRepository.getLeaders(10)) thenReturn leaders

      assert(gameService.getLeaderBoard(10) == leaders)
    }

  }


}
