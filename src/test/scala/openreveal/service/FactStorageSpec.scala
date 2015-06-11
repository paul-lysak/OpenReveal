package openreveal.service

import com.hp.hpl.jena.rdf.model.{Resource, Model}
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.model.{PoliticalParty, EntityDefinition, User}
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
    val TestEnv(_, model) = createEnv()

    val user1Resource = getUserRes(model)

    user1Resource.getRequiredProperty(RDF.`type`).getObject === OpenRevealSchema.User.a
    user1Resource.getRequiredProperty(OpenRevealSchema.User.email).getLiteral.getString === "user1@a.b.com"
    user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reporter).getObject === user1Resource
    user1Resource.getRequiredProperty(OpenRevealSchema.Entity.reportedAt).getLiteral.getString === sampleDateTimeStr
  }

  it should "create party" in {
    val TestEnv(storage, model) = createEnv()
    storage.defineEntity(EntityDefinition(SAMPLE_USER, fixedClock.now(),
      PoliticalParty("Imperial_Party", "Imperial Party", "Tatouine")))

    val user1Resource = model.getResource(SAMPLE_USER.id)

    val partyRes = model.getResource("Imperial_Party")
    partyRes.getRequiredProperty(RDF.`type`).getObject === OpenRevealSchema.User.a
    partyRes.getRequiredProperty(OpenRevealSchema.Entity.name).getLiteral.getString === "Imperial Party"
    partyRes.getRequiredProperty(OpenRevealSchema.PoliticalParty.registeredInCountry).getLiteral.getString === "Tatouine"
    partyRes.getRequiredProperty(OpenRevealSchema.PoliticalParty.reporter).getObject === user1Resource
    partyRes.getRequiredProperty(OpenRevealSchema.PoliticalParty.reportedAt).getLiteral.getString === sampleDateTimeStr
  }

  it should "create person" in {
    ???
  }

  it should "create company" in {
    ???
  }

  it should "create trademark" in {
    ???
  }

  it should "register politican fact" in {
    ???
  }

  it should "register ownership fact" in {
    ???
  }

  it should "register trademark fact" in {
    ???
  }

  def createEnv(): TestEnv = {
     val modelProvider = new RdfInMemoryModelProvider()

    val storage = new JenaFactStorage(modelProvider, fixedClock)
    storage.createUser(SAMPLE_USER.id, SAMPLE_USER.email)


    TestEnv(storage, modelProvider.model)
  }

  private def getUserRes(implicit rdfModel: Model): Resource = rdfModel.getResource(SAMPLE_USER.id)

  case class TestEnv(storage: JenaFactStorage, model: Model)

  val SAMPLE_USER_NAME = "user1"
  val SAMPLE_USER = User("user1", "user1@a.b.com")

  val sampleDateTime = DateTime.now()
  val sampleDateTimeStr = ISODateTimeFormat.dateTime().print(sampleDateTime)
  val fixedClock = new Clock {
    override def now(): DateTime = sampleDateTime
  }

}
