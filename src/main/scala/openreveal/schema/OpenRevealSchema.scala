package openreveal.schema

import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Created by Paul Lysak on 03.06.15.
 */
object OpenRevealSchema {
  val uri = "http://openreveal.org/schema/1.0/"

  private val m = ModelFactory.createDefaultModel()

  trait ReportedT {
    val reportedBy = m.createProperty(uri, "reportedBy")
    val reportedAt = m.createProperty(uri, "reportedAt")
  }

  object Reported extends ReportedT

  trait EntityT extends ReportedT {
    val name = m.createProperty(uri, "name")
  }

  object Entity extends EntityT

  object User extends EntityT {
    val a = m.createResource(uri + "User")
    val email = m.createProperty(uri, "email")
  }

  trait RegistrableT extends EntityT {
    val registeredInCountry = m.createProperty(uri, "registeredInCountry")
  }

  object Registrable extends RegistrableT

  object PoliticalParty extends RegistrableT {
    val a = m.createResource(uri + "PoliticalParty")
  }

  object GenericCompany extends RegistrableT {
    val a = m.createResource(uri + "GenericCompany")
  }

  object Media extends RegistrableT {
    val a = m.createResource(uri + "Media")
  }


  object TradeMark extends RegistrableT {
    val a = m.createResource(uri + "TradeMark")
  }

  object Person extends EntityT {
    val a = m.createResource(uri + "Person")
  }



  trait FactT extends ReportedT {
    val subject = m.createProperty(uri, "subject")
    val articleUrl = m.createProperty(uri, "articleUrl")
    val articlePublishedAt = m.createProperty(uri, "articlePublishedAt")
    val media = m.createProperty(uri, "media")
  }

  object Fact extends FactT

  object PersonFact extends FactT {
    val a = m.createResource(uri + "PersonFact")

    val citizenOf = m.createProperty(uri, "citizenOf")
    val livesIn = m.createProperty(uri, "livesIn")
  }

  object MemberFact extends FactT {
    val a = m.createResource(uri + "MemberFact")

    val memberOf = m.createProperty(uri, "memberOf")
    val memberSince = m.createProperty(uri, "memberSince")
    val position = m.createProperty(uri, "position")
    val positionSince = m.createProperty(uri, "positionSince")
  }

}
