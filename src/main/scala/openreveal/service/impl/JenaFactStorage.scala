package openreveal.service.impl


import com.hp.hpl.jena.rdf.model.{Resource, Model}
import openreveal.Constants
import openreveal.exceptions.ValidationException
import openreveal.model.{User, Entity, EntityDefinitionFact, Fact}
import openreveal.schema.OpenRevealSchema
import openreveal.service.{Clock, FactStorage}

import scala.util.Random


/**
 * Created by Paul Lysak on 02.06.15.
 */
class JenaFactStorage(rdfModel: Model, val clock: Clock) extends FactStorage {
  override def saveFact(fact: Fact): Unit = {
    factToResource(fact)
  }

  override def generateFactId(): String =
    //TODO more secure uuid-like algorithm
    Random.nextString(6)

  private def factToResource(fact: Fact): Resource = {
    fact match {
      case EntityDefinitionFact(id, reporter, reportedAt, entity) =>
        val res = entityToResource(entity)
        res.addProperty(OpenRevealSchema.Entity.reporter, res)
        res.addLiteral(OpenRevealSchema.Entity.reportedAt, clock.nowIsoString())
        res
      case _ => throw new ValidationException(s"Fact not supported at the moment: $fact")
    }
  }

  private def entityToResource(entity: Entity) = {
    entity match {
      case User(id, email) =>
        val res = rdfModel.createResource(id, OpenRevealSchema.User.a)
        res.addProperty(OpenRevealSchema.User.email, email)
      case _ => throw new ValidationException(s"Entity not supported at the moment: $entity")
    }

  }
}
