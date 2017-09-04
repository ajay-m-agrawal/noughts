package com.spaceape.hiring.model

import org.hibernate.validator.constraints.NotEmpty
import javax.validation.constraints.NotNull


case class Move(@NotEmpty playerId: String, @NotNull x: Int, @NotNull y: Int)