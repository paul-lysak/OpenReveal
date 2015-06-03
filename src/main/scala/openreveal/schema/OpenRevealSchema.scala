package openreveal.schema

import com.hp.hpl.jena.rdf.model.ModelFactory

/**
 * Created by Paul Lysak on 03.06.15.
 */
object OpenRevealSchema {
  val uri = "http://openreveal.org/schema/1.0/"

  private val m = ModelFactory.createDefaultModel()

  object Entity {
    val reporter = m.createProperty(uri, "reporter")
    val reportedAt = m.createProperty(uri, "reportedAt")
  }

  object User {
    val a = m.createResource(uri + "User")
    val email = m.createProperty(uri, "email")
  }

}
