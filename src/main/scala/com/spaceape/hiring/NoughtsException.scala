package com.spaceape.hiring

case class NoughtsException(code: Int, message: String = "",
                           cause: Throwable = None.orNull)
  extends Exception(message, cause)
