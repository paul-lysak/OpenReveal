package openreveal.service.impl


import com.hp.hpl.jena.query.{ReadWrite, Dataset}
import com.hp.hpl.jena.rdf.model.{Resource, Model}
import openreveal.Constants
import openreveal.exceptions.ValidationException
import openreveal.model.{User, Entity, EntityDefinitionFact, Fact}
import openreveal.rdf.RdfModelProvider
import openreveal.schema.OpenRevealSchema
import openreveal.service.{Clock, FactStorage}

import scala.util.Random


/**
 * Created by Paul Lysak on 02.06.15.
 */
class JenaFactStorage(rdfModelProvider: RdfModelProvider, val clock: Clock) extends FactStorage {
  override def saveFact(fact: Fact): Unit = {
    rdfModelProvider.writeWithModel(factToResource(_, fact))
  }

  override def generateFactId(): String =
    //TODO more secure uuid-like algorithm
    Random.nextString(6)

  private def factToResource(rdfModel: Model, fact: Fact): Resource = {
    fact match {
      case EntityDefinitionFact(id, reporter, reportedAt, entity) =>
        val res = entityToResource(rdfModel, entity)
        res.addProperty(OpenRevealSchema.Entity.reporter, res)
        res.addLiteral(OpenRevealSchema.Entity.reportedAt, clock.nowIsoString())
        res
      case _ => throw new ValidationException(s"Fact not supported at the moment: $fact")
    }
  }

  private def entityToResource(rdfModel: Model, entity: Entity) = {
    entity match {
      case User(id, email) =>
        val res = rdfModel.createResource(id, OpenRevealSchema.User.a)
        res.addProperty(OpenRevealSchema.User.email, email)
      case _ => throw new ValidationException(s"Entity not supported at the moment: $entity")
    }

  }
}

