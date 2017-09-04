package com.spaceape.hiring

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.ACCEPTED

import com.spaceape.hiring.model._
import com.spaceape.hiring.resources.{LeadersResource, NoughtsResource}
import com.spaceape.hiring.service.GameService
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar


class LeadersResourceTest extends FunSuite with MockitoSugar{

  trait Context{
    val gameService = mock[GameService]
    val leaderResource = new LeadersResource(gameService)
  }

  test("test getLeaderBoard must return top n leaders") {
    new Context {

      val leaders : List[PlayerScore] = List(PlayerScore("1", 10), PlayerScore("2", 5))
      when(gameService.getLeaderBoard(10)) thenReturn leaders
      assert(leaderResource.getLeaderBoard(10) == leaders)
    }
  }





}
