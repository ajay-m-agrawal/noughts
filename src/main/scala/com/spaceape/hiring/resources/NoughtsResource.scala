package com.spaceape.hiring.resources

import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs._
import javax.ws.rs.core.Response.Status.ACCEPTED
import javax.ws.rs.core.Response.status
import javax.ws.rs.core.{MediaType, Response}

import com.spaceape.hiring.model._
import com.spaceape.hiring.service.GameService
import org.hibernate.validator.constraints.NotEmpty
;

@Path("/game")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class NoughtsResource(gameService: GameService) {
  
  @POST
  def createGame(@QueryParam("player1Id") @NotEmpty player1: String, @QueryParam("player2Id") @NotEmpty player2: String): NewGameResponse = {
    NewGameResponse(gameService.createGame(player1, player2))
  }

  @GET
  @Path("/{gameId}")
  def getGame(@PathParam("gameId") @NotEmpty gameId: String): GameState = {
    val game = gameService.getGame(gameId)
    GameState(game.winnerId, game.gameOver)
  }

  @PUT
  @Path("/{gameId}")
  def makeMove(@PathParam("gameId") @NotEmpty gameId: String, @NotNull @Valid move: Move): Response = {
    gameService.makeMove(gameId, move)
    status(ACCEPTED).build()
  }

}