package com.spaceape.hiring
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.status
import javax.ws.rs.ext.{ExceptionMapper, Provider}


@Provider
class NoughtsExceptionMapper extends ExceptionMapper[NoughtsException]{
  override def toResponse(e: NoughtsException): Response = {
    status(e.code).entity(e.message).build
  }
}
