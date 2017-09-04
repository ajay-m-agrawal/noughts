package com.spaceape.hiring.service

import javax.ws.rs.core.Response.Status._

import com.mongodb.DuplicateKeyException
import com.spaceape.hiring.NoughtsException
import com.spaceape.hiring.model.{Game, Move, PlayerScore}
import com.spaceape.hiring.repository.GameRepository
import org.bson.types.ObjectId

class GameService(gameRepository: GameRepository) {
  def getLeaderBoard(numberOfLeaders: Int): List[PlayerScore] = {
    gameRepository.getLeaders(numberOfLeaders)
  }


  def getGame(gameId: String) : Game = {
    validateGameId(gameId)
    gameRepository.getGame(gameId).getOrElse(throw NoughtsException(NOT_FOUND.getStatusCode, s"$gameId does not exist"))
  }

  def createGame(player1: String, player2: String): String = {
    try {
      validatePlayers(player1, player2)
      gameRepository.createGame(Game(player1, player2))
    }
    catch {
      case ex: DuplicateKeyException =>
          throw NoughtsException(FORBIDDEN.getStatusCode, s"Game in progress between $player1 and $player2")

    }
  }


  def makeMove(gameId: String, move: Move) {
    validateGameId(gameId)
    val game = gameRepository.updateMove(gameId, move)
    if(game.isDefined) {
      val winnerId = winner(game.get)
      val isGameOver = gameOver(game.get, winnerId)
      gameRepository.completeMove(gameId, if (isGameOver) None else nextPlayer(game.get, winnerId), winnerId, isGameOver)
    }else{
      rejectionException(gameId, move)
    }
  }



  private def validateGameId(gameId: String)  {
    try {
      new ObjectId(gameId)
    }
    catch {
      case ex: IllegalArgumentException =>
        throw NoughtsException(BAD_REQUEST.getStatusCode, s"$gameId is not a valid game id")
    }
  }

  private def validatePlayers(player1: String, player2: String)  {
      if (player1 == player2)
        throw NoughtsException(BAD_REQUEST.getStatusCode, "Need two different players to initiate a game")
  }

  private def rejectionException(gameId: String, move: Move) = {
    val gameById = gameRepository.getGame(gameId).getOrElse(throw NoughtsException(NOT_FOUND.getStatusCode, s"$gameId does not exist"))
    if (gameById.gameOver) throw NoughtsException(FORBIDDEN.getStatusCode, s"$gameId is over")
    if (gameById.moveInProgress) throw NoughtsException(FORBIDDEN.getStatusCode, s"Move in progress for $gameId")
    if (gameById.cells(move.x)(move.y).isDefined) throw NoughtsException(CONFLICT.getStatusCode, s"Cell in $gameId at position x = ${move.x} and y = ${move.y} is already filled")
    if (move.playerId != gameById.nextTurn.get) throw NoughtsException(CONFLICT.getStatusCode, s"Next turn belongs to ${gameById.nextTurn.get} and not ${move.playerId}")
    throw NoughtsException(INTERNAL_SERVER_ERROR.getStatusCode, s"Unknown error occurred in processing the move, please retry")
  }

  private def gameOver(game: Game, winnerId: Option[String]): Boolean = {
    winnerId.isDefined || game.cells.flatten.flatten.size == 9
  }

  private def nextPlayer(game: Game, winnerId: Option[String]): Option[String] = {
    if (winnerId.isDefined) None else if (game.nextTurn.get == game.player2) Option(game.player1) else Option(game.player2)
  }

  private def winner(game: Game) : Option[String] = {
    val cells = game.cells
    if(cells.flatten.flatten.size > 4) {
      if (List(cells(0)(0), cells(1)(1), cells(2)(2)).distinct.size == 1) return cells.head.head
      if (List(cells(2)(0), cells(1)(1), cells(0)(2)).distinct.size == 1) return cells(2).head

      val transposedCells = game.cells.transpose
      for (i <- 0 until 2) {
        if (cells(i).distinct.size == 1)
          return cells(i).head
        if (transposedCells(i).distinct.size == 1)
          return transposedCells(i).head
      }
    }
    None
  }

}
