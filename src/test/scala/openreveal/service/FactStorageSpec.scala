package openreveal.service

import com.hp.hpl.jena.rdf.model.{Resource, Model}
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.model._
import openreveal.rdf.RdfInMemoryModelProvider
import openreveal.schema.OpenRevealSchema
import openreveal.service.impl.JenaFactStorage
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by Paul Lysak on 02.06.15.
 */
class FactStorageSpec extends FlatSpec with Matchers {
  val s = OpenRevealSchema

  "FactStorage" should "create user" in {
    val TestEnv(_, model) = createEnv()

    val user1Resource = getUserRes(model)

    user1Resource.getRequiredProperty(RDF.`type`).getObject === s.User.a
    user1Resource.getRequiredProperty(s.User.email).getLiteral.getString === "user1@a.b.com"
    user1Resource.getRequiredProperty(s.Entity.reportedBy).getObject === user1Resource
    user1Resource.getRequiredProperty(s.Entity.reportedAt).getLiteral.getString === sampleDateTimeStr
  }

  it should "create party" in {
    val res = testEntityCreation(PoliticalParty("Imperial_Party", "Imperial Party", "Tatouine"), s.PoliticalParty.a)
  }

  it should "create person" in {
    testEntityCreation(Person("Darth_Vader", "Darth Vader"), s.Person.a)
  }

  it should "create company" in {
    testEntityCreation(GenericCompany("Death_Star_Inc", "Death Star Inc.", "Tatouine"), s.GenericCompany.a)
  }

  it should "create trademark" in {
    testEntityCreation(TradeMark("Battle_Clones", "Battle Clones", "Tatouine"), s.TradeMark.a)
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

  private def testEntityCreation(entity: Entity, expectedType: Resource): Resource = {
    val TestEnv(storage, model) = createEnv()
    storage.defineEntity(EntityDefinition(SAMPLE_USER, fixedClock.now(), entity))

    val res = model.getResource(entity.id)

    res.getRequiredProperty(RDF.`type`).getObject shouldBe expectedType
    res.getRequiredProperty(s.Entity.name).getLiteral.toString shouldBe entity.name
    res.getRequiredProperty(s.PoliticalParty.reportedBy).getObject shouldBe getUserRes(model)
    res.getRequiredProperty(s.PoliticalParty.reportedAt).getLiteral.getString shouldBe sampleDateTimeStr

    entity match {
      case r: Registrable =>
        res.getRequiredProperty(s.Registrable.registeredInCountry).getLiteral.getString shouldBe r.registeredInCountry
      case _ => //no additional checks
    }

    res
  }

  private def createEnv(): TestEnv = {
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
