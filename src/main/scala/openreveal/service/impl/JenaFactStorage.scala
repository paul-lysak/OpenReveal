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

  override def defineEntity[T <: Entity](entityDef: EntityDefinition[T], reporterUser: User): T = {
    rdfModelProvider.writeWithModel({rdfModel =>
      val res = entityToResource(rdfModel, entityDef.entity)
      val reporter = rdfModel.getResource(reporterUser.id)
      res.addProperty(OpenRevealSchema.Entity.reporter, reporter)
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
    val res = entity match {
      case User(id, email) =>
        val res = rdfModel.createResource(id, OpenRevealSchema.User.a)
        res.addProperty(OpenRevealSchema.User.email, email)
      case PoliticalParty(id, name, country) => rdfModel.createResource(id, OpenRevealSchema.PoliticalParty.a)
      case Person(id, _) => rdfModel.createResource(id, OpenRevealSchema.Person.a)
      case GenericCompany(id, _, _) => rdfModel.createResource(id, OpenRevealSchema.GenericCompany.a)
      case TradeMark(id, _, _) => rdfModel.createResource(id, OpenRevealSchema.TradeMark.a)
      case _ => throw new ValidationException(s"Entity not supported at the moment: $entity")
    }

    entity match {
      case r: Registrable =>
        res.addProperty(OpenRevealSchema.Registrable.registeredInCountry, r.registeredInCountry)
      case _ => //no additional checks
    }

    res.addProperty(OpenRevealSchema.Entity.name, entity.name)
  }
}

