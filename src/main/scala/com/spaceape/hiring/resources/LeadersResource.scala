package com.spaceape.hiring.resources

import javax.validation.constraints.Min
import javax.ws.rs._
import javax.ws.rs.core.MediaType

import com.spaceape.hiring.model._
import com.spaceape.hiring.service.GameService
;

@Path("/leaders")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class LeadersResource(gameService: GameService) {
  
  @GET
  def getLeaderBoard(@DefaultValue("10") @QueryParam("numberOfLeaders") @Min(1) numberOfLeaders: Int): List[PlayerScore] = {
    gameService.getLeaderBoard(numberOfLeaders)
  }

}