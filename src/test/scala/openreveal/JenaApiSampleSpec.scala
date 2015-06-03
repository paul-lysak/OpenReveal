package openreveal

import java.io.StringReader

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.schema.OpenRevealSchema
import openreveal.service.DefaultClock
import openreveal.service.impl.JenaFactStorage
import org.specs2.mutable.Specification

/**
 * Created by Paul Lysak on 02.06.15.
 *
 * Sample spec for exploring Jena API
 */
class JenaApiSampleSpec extends Specification {
  sequential
  val sampleData =
    """
      |@base <http://openreveal.org/data/> .
      |@prefix ore: <http://openreveal.org/schema/1.0/> .
      |<scribe1>
      | a ore:User ;
      | ore:email "scribe1@a.b.com" .
    """.stripMargin

  "FactStorage" should {
    "create user" in {
      val model = ModelFactory.createDefaultModel();
      val sampleReader = new StringReader(sampleData)
      model.read(sampleReader, "", "TURTLE")

      val scribe1Resource = model.getResource(Constants.URI_PREFIX + "scribe1")
      val propsS1 = scribe1Resource.listProperties().toList
      val s1Type = scribe1Resource.getProperty(RDF.`type`)
      val s1Email = scribe1Resource.getProperty(OpenRevealSchema.User.email)

      println(s"scribe1: props=$propsS1, type=$s1Type, email=$s1Email")

      s1Type.getObject === OpenRevealSchema.User.a
      s1Email.getLiteral.toString === "scribe1@a.b.com"
    }
  }

}
