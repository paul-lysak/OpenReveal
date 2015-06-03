package openreveal

import java.io.StringReader

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.schema.OpenRevealSchema
import openreveal.service.{Clock, DefaultClock}
import openreveal.service.impl.JenaFactStorage
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mutable.Specification

/**
 * Created by Paul Lysak on 02.06.15.
 */
class FactStorageSpec extends Specification {
  "FactStorage" should {
    "create user" in {
      val model = ModelFactory.createDefaultModel();

      val storage = new JenaFactStorage(model, fixedClock)
      storage.createUser("user1", "user1@a.b.com")

      val user1Resource = model.getResource("user1")

      user1Resource.getRequiredProperty(RDF.`type`).getObject === OpenRevealSchema.User.a
      user1Resource.getRequiredProperty(OpenRevealSchema.User.email).getLiteral.getString === "user1@a.b.com"
      user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reporter).getObject === user1Resource
      val expectedReportedAt = ISODateTimeFormat.dateTime().print(sampleDateTime)
      user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reportedAt).getLiteral.getString === expectedReportedAt
    }
  }

  val sampleDateTime = DateTime.now()
  val fixedClock = new Clock {
    override def now(): DateTime = sampleDateTime
  }

}
