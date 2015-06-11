package openreveal.service

import openreveal.model._
import org.joda.time.DateTime

/**
 * Created by Paul Lysak on 02.06.15.
 */
trait FactStorage {
  def saveFact(fact: Fact)

  def defineEntity[T <: Entity](entityDef: EntityDefinition[T]): T

  def generateFactId(): String

  def createUser(id: String, email: String): User = {
    val user = User(id, email)
    defineEntity(EntityDefinition(reporter = user, reportedAt = clock.now(), entity = user))
  }

  val clock: Clock
}
