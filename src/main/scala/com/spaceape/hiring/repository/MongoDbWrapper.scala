package com.spaceape.hiring.repository

import com.mongodb.casbah.MongoURI
import com.mongodb.casbah.Imports._
import com.mongodb.casbah

class MongoDbWrapper(mongoConnectString: String) {

  private val uri = MongoURI(mongoConnectString)

  val mongoDB: casbah.MongoDB = MongoConnection(uri).getDB(uri.database.get)
}
