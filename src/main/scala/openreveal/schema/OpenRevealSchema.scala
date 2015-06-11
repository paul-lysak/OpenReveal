package openreveal.schema

import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Created by Paul Lysak on 03.06.15.
 */
object OpenRevealSchema {
  val uri = "http://openreveal.org/schema/1.0/"

  private val m = ModelFactory.createDefaultModel()

  trait EntityT {
    val name = m.createProperty(uri, "name")
    val reportedBy = m.createProperty(uri, "reportedBy")
    val reportedAt = m.createProperty(uri, "reportedAt")
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

  object TradeMark extends RegistrableT {
    val a = m.createResource(uri + "TradeMark")
  }

  object Person extends EntityT {
    val a = m.createResource(uri + "Person")
  }

}
