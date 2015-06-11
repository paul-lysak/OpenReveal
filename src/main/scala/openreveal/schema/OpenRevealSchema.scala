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
    val reporter = m.createProperty(uri, "reporter")
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

  object PoliticalParty extends RegistrableT {
    val a = m.createResource(uri + "PoliticalParty")
  }



}
