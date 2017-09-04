package com.spaceape.hiring;

import io.dropwizard.Configuration
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.NotEmpty;

class NoughtsConfiguration extends Configuration {

  @JsonProperty
  @NotEmpty
  val gameMongoURI: String = "NOT CONFIGURED"
  
  
}