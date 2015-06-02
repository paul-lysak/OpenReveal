package openreveal.service

import openreveal.model.{User, EntityDefinitionFact, Fact}
import org.joda.time.DateTime

/**
 * Created by Paul Lysak on 02.06.15.
 */
trait FactStorage {
  def saveFact(fact: Fact)

  def generateFactId(): String

  def createUser(id: String, email: String) = {
    val user = User(id, email)
    saveFact(EntityDefinitionFact(generateFactId(),
      reporter = user,
      reportedAt = clock.now(),
      entity = user))
  }

  val clock: Clock
}
