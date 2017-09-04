package com.spaceape.hiring.model

case class Game(player1: String, player2: String, winnerId: Option[String], gameOver: Boolean, nextTurn: Option[String], cells: List[List[Option[String]]], moveInProgress: Boolean)

object Game{

  val initialCells = List(
    List(None, None, None),
    List(None, None, None),
    List(None, None, None)
  )

  def apply(player1: String, player2: String): Game = Game(player1, player2, None, gameOver = false, Option(player1), initialCells, moveInProgress = false)
}