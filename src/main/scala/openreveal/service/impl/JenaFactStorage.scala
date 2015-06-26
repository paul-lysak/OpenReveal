package openreveal.service.impl


import com.hp.hpl.jena.query.{ReadWrite, Dataset}
import com.hp.hpl.jena.rdf.model.{Resource, Model}
import openreveal.exceptions.ValidationException
import openreveal.model._
import openreveal.rdf.RdfModelProvider
import openreveal.schema.{OpenRevealSchema => S}
import openreveal.service.{Clock, FactStorage}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

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
      val reporter = rdfModel.getResource(entityDef.reportedBy.id)
      res.addProperty(S.Entity.reportedBy, reporter)
      res.addLiteral(S.Entity.reportedAt, clock.nowIsoString())
      entityDef.entity
    })
  }

  override def generateFactId(): String =
    //TODO more secure uuid-like algorithm
    Random.nextString(6)

  private def factToResource[T <: Entity](rdfModel: Model, fact: Fact): Resource = {
    val res = fact match {
      case f: PersonFact =>
        val res = rdfModel.createResource(fact.id, S.PersonFact.a)
        if(f.citizenOf.nonEmpty) {
          val b = rdfModel.createBag()
          f.citizenOf.foreach(b.add)
          res.addProperty(S.PersonFact.citizenOf, b)
        }
        f.livesIn.foreach(res.addProperty(S.PersonFact.livesIn, _))
        res
      case _ => throw new ValidationException(s"Fact not supported at the moment: $fact")
    }

    val reporter = rdfModel.getResource(fact.reportedBy.id)
    res.addProperty(S.Fact.reportedBy, reporter)
    res.addLiteral(S.Entity.reportedAt, dateTimeStr(fact.reportedAt))
    val subj = rdfModel.getResource(fact.subject.id)
    res.addProperty(S.Fact.subject, subj)

    res.addProperty(S.Fact.articleUrl, fact.articleUrl)
    fact.articlePublishedAt.foreach(dt => res.addProperty(S.Fact.articlePublishedAt, dateTimeStr(dt)))
    fact.media.foreach({m =>
      val mRes = rdfModel.getResource(m.id)
      res.addProperty(S.Fact.media, mRes)
    })

    res
  }

  private def entityToResource(rdfModel: Model, entity: Entity) = {
    val res = entity match {
      case User(id, email) =>
        val res = rdfModel.createResource(id, S.User.a)
        res.addProperty(S.User.email, email)
      case PoliticalParty(id, name, country) => rdfModel.createResource(id, S.PoliticalParty.a)
      case Person(id, _) => rdfModel.createResource(id, S.Person.a)
      case GenericCompany(id, _, _) => rdfModel.createResource(id, S.GenericCompany.a)
      case TradeMark(id, _, _) => rdfModel.createResource(id, S.TradeMark.a)
      case Media(id, _, _) => rdfModel.createResource(id, S.Media.a)
      case _ => throw new ValidationException(s"Entity not supported at the moment: $entity")
    }

    entity match {
      case r: Registrable =>
        res.addProperty(S.Registrable.registeredInCountry, r.registeredInCountry)
      case _ => //no additional checks
    }

    res.addProperty(S.Entity.name, entity.name)
  }

  private def dateTimeStr(dt: DateTime) = ISODateTimeFormat.dateTime().print(dt)
}

