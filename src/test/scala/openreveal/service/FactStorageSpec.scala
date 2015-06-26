package openreveal.service

import com.hp.hpl.jena.rdf.model.{Resource, Model}
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.model._
import openreveal.rdf.RdfInMemoryModelProvider
import openreveal.schema.OpenRevealSchema
import openreveal.service.impl.JenaFactStorage
import org.joda.time.{LocalDate, DateTime}
import org.joda.time.format.ISODateTimeFormat
import org.scalatest.{Matchers, FlatSpec}
import scala.collection.JavaConversions._

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

  it should "create media" in {
    testEntityCreation(Media("Galaxy_Tomorrow", "Galaxy Tomorrow", "Tatouine"), s.Media.a)
  }


  it should "register person full fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")
    val media = Media("Galaxy_Times", "Galaxy Times", "UA")

    val fact = PersonFact(id = "DV_user1_person",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = Option(media),
      articleUrl = "google.com",
      articlePublishedAt = Option(articleDateTime),
      subject = subj,
      citizenOf = Set("UA", "PA"),
      livesIn = Some("UA"))
    val res = testFactCreation(fact, s.PersonFact.a)

    val actualCitizen = res.getRequiredProperty(s.PersonFact.citizenOf).getBag.iterator().toSet.map(_.asLiteral().getString)
    actualCitizen shouldBe fact.citizenOf
    Option(res.getRequiredProperty(s.PersonFact.livesIn).getString) shouldBe fact.livesIn
  }

  it should "register person minimal fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")

    val fact = PersonFact(id = "DV_user1_person",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      citizenOf = Set(),
      livesIn = None)
    val res = testFactCreation(fact, s.PersonFact.a)

    res.hasProperty(s.PersonFact.citizenOf) shouldBe false
    res.hasProperty(s.PersonFact.livesIn) shouldBe false
  }


  it should "register member full fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")
    val party = PoliticalParty("Imperial", "Imperial Party Of Galaxy", "Tatouine")

    val fact = MemberFact(id = "DV_user1_party",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      memberOf= party,
      memberSince = Option(LocalDate.parse("2015-01-01")),
      position = Some("Vice-president"),
      positionSince = Some(LocalDate.parse("2015-01-05")) )
    val res = testFactCreation(fact, s.MemberFact.a)

    res.getRequiredProperty(s.MemberFact.memberOf).getResource.getURI shouldBe party.id
    res.getRequiredProperty(s.MemberFact.memberSince).getString shouldBe dateStr(fact.memberSince.get)
    res.getRequiredProperty(s.MemberFact.position).getString shouldBe fact.position.get
    res.getRequiredProperty(s.MemberFact.positionSince).getString shouldBe dateStr(fact.positionSince.get)
  }

  it should "register member minimal fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")
    val party = PoliticalParty("Imperial", "Imperial Party Of Galaxy", "Tatouine")

    val fact = MemberFact(id = "DV_user1_party",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      memberOf= party,
      memberSince = None,
      position = None,
      positionSince = None )
    val res = testFactCreation(fact, s.MemberFact.a)

    res.getRequiredProperty(s.MemberFact.memberOf).getResource.getURI shouldBe party.id
    res.hasProperty(s.MemberFact.memberSince) shouldBe false
    res.hasProperty(s.MemberFact.position) shouldBe false
    res.hasProperty(s.MemberFact.positionSince) shouldBe false
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
    res.getRequiredProperty(s.Reported.reportedBy).getObject shouldBe getUserRes(model)
    res.getRequiredProperty(s.Reported.reportedAt).getLiteral.getString shouldBe sampleDateTimeStr

    entity match {
      case r: Registrable =>
        res.getRequiredProperty(s.Registrable.registeredInCountry).getLiteral.getString shouldBe r.registeredInCountry
      case _ => //no additional checks
    }

    res
  }

  private def testFactCreation(fact: Fact, expectedType: Resource): Resource = {
    val TestEnv(storage, model) = createEnv()
    val entities = Set(fact.subject) ++ fact.media.toSet

    entities.foreach(e => storage.defineEntity(EntityDefinition(SAMPLE_USER, fixedClock.now(), e)))

    storage.saveFact(fact)

    val res = model.getResource(fact.id)
    res.getRequiredProperty(RDF.`type`).getObject shouldBe expectedType

    res.getRequiredProperty(s.Reported.reportedBy).getObject shouldBe getUserRes(model)
    res.getRequiredProperty(s.Reported.reportedAt).getLiteral.getString shouldBe sampleDateTimeStr
    fact.media.fold(
      res.hasProperty(s.Fact.media) shouldBe false)(media =>
      res.getRequiredProperty(s.Fact.media).getResource.getURI shouldBe media.id)
    res.getRequiredProperty(s.Fact.articleUrl).getLiteral.getString shouldBe fact.articleUrl
    fact.articlePublishedAt.fold(
      res.hasProperty(s.Fact.articlePublishedAt) shouldBe false )(dt =>
      res.getRequiredProperty(s.Fact.articlePublishedAt).getLiteral.getString shouldBe dateTimeStr(dt) )
    res.getRequiredProperty(s.Fact.subject).getResource.getURI shouldBe fact.subject.id

    res
  }

  private def createEnv(): TestEnv = {
     val modelProvider = new RdfInMemoryModelProvider()

    val storage = new JenaFactStorage(modelProvider, fixedClock)
    storage.createUser(SAMPLE_USER.id, SAMPLE_USER.email)


    TestEnv(storage, modelProvider.model)
  }

  private def getUserRes(implicit rdfModel: Model): Resource = rdfModel.getResource(SAMPLE_USER.id)

  private case class TestEnv(storage: JenaFactStorage, model: Model)

  private val SAMPLE_USER_NAME = "user1"
  private val SAMPLE_USER = User("user1", "user1@a.b.com")

  private val sampleDateTime = DateTime.now()
  private val sampleDateTimeStr = ISODateTimeFormat.dateTime().print(sampleDateTime)
  private val fixedClock = new Clock {
    override def now(): DateTime = sampleDateTime
  }

  private val articleDateTime = DateTime.parse("2015-06-01T10:00:00Z")
  private val articleDateTimeStr = "2015-06-01T10:00:00.000Z"

  private def dateTimeStr(dt: DateTime) = ISODateTimeFormat.dateTime().print(dt)

  private def dateStr(d: LocalDate) = ISODateTimeFormat.date().print(d)
}
