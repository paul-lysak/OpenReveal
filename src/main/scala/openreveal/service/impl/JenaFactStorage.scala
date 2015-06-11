package openreveal.service.impl


import com.hp.hpl.jena.query.{ReadWrite, Dataset}
import com.hp.hpl.jena.rdf.model.{Resource, Model}
import openreveal.exceptions.ValidationException
import openreveal.model._
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

  override def defineEntity[T <: Entity](entityDef: EntityDefinition[T]): T = {
    rdfModelProvider.writeWithModel({rdfModel =>
      val res = entityToResource(rdfModel, entityDef.entity)
      res.addProperty(OpenRevealSchema.Entity.reporter, res)
      res.addLiteral(OpenRevealSchema.Entity.reportedAt, clock.nowIsoString())
      entityDef.entity
    })
  }

  override def generateFactId(): String =
    //TODO more secure uuid-like algorithm
    Random.nextString(6)

  private def factToResource(rdfModel: Model, fact: Fact): Resource = {
    fact match {
      case _ => throw new ValidationException(s"Fact not supported at the moment: $fact")
    }
  }

  private def entityToResource(rdfModel: Model, entity: Entity) = {
    entity match {
      case User(id, email) =>
        val res = rdfModel.createResource(id, OpenRevealSchema.User.a)
        res.addProperty(OpenRevealSchema.User.email, email)
      case PoliticalParty(id, name, country) =>
        val res = rdfModel.createResource(id, OpenRevealSchema.PoliticalParty.a)
        res.addProperty(OpenRevealSchema.PoliticalParty.name, name)
        res.addProperty(OpenRevealSchema.PoliticalParty.registeredInCountry, country)
      case _ => throw new ValidationException(s"Entity not supported at the moment: $entity")
    }
  }
}

