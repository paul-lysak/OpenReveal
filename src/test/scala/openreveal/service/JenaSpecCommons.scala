package openreveal.service

import com.hp.hpl.jena.rdf.model.Model
import openreveal.model.User
import openreveal.rdf.RdfInMemoryModelProvider
import openreveal.service.impl.{JenaQueryEndpoint, JenaFactStorage}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by Paul Lysak on 02.07.15.
 */
trait JenaSpecCommons {
  def createEnv(): TestEnv = {
     val modelProvider = new RdfInMemoryModelProvider()

    val storage = new JenaFactStorage(modelProvider, fixedClock)
    storage.createUser(SAMPLE_USER.id, SAMPLE_USER.email)

    val qEndpoint = new JenaQueryEndpoint(modelProvider)

    TestEnv(storage, qEndpoint, modelProvider.model)
  }

  case class TestEnv(storage: JenaFactStorage, queryEndpoint: JenaQueryEndpoint, model: Model)

  val SAMPLE_USER_NAME = "user1"
  val SAMPLE_USER = User("user1", "user1@a.b.com")

  val sampleDateTimeStr = "2015-07-12T17:00Z"
  val sampleDateTime = DateTime.parse(sampleDateTimeStr)

  val fixedClock = new Clock {
    override def now(): DateTime = sampleDateTime
  }


}
