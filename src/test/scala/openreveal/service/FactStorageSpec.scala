package openreveal.service

import com.hp.hpl.jena.vocabulary.RDF
import openreveal.rdf.RdfInMemoryModelProvider
import openreveal.schema.OpenRevealSchema
import openreveal.service.impl.JenaFactStorage
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.FlatSpec

/**
 * Created by Paul Lysak on 02.06.15.
 */
class FactStorageSpec extends FlatSpec {
  "FactStorage" should "create user" in {
    val modelProvider = new RdfInMemoryModelProvider()

    val storage = new JenaFactStorage(modelProvider, fixedClock)
    storage.createUser("user1", "user1@a.b.com")

    val user1Resource = modelProvider.model.getResource("user1")

    user1Resource.getRequiredProperty(RDF.`type`).getObject === OpenRevealSchema.User.a
    user1Resource.getRequiredProperty(OpenRevealSchema.User.email).getLiteral.getString === "user1@a.b.com"
    user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reporter).getObject === user1Resource
    val expectedReportedAt = ISODateTimeFormat.dateTime().print(sampleDateTime)
    user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reportedAt).getLiteral.getString === expectedReportedAt
  }

  val sampleDateTime = DateTime.now()
  val fixedClock = new Clock {
    override def now(): DateTime = sampleDateTime
  }

}
