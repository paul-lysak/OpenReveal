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
class FactStorageSpec extends FlatSpec with Matchers with JenaSpecCommons {
  val s = OpenRevealSchema

  "FactStorage" should "create user" in {
    val TestEnv(_, _, model) = createEnv()

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


  it should "register ownership full fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")
    val company = GenericCompany("DS", "Death Star Inc.", "Tatouine")

    val fact = OwnerFact(id = "DV_user1_own",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      owns = company,
      ownsSince = Option(LocalDate.parse("2015-02-01")),
      sharePercents = Some(30))
    val res = testFactCreation(fact, s.OwnerFact.a)

    res.getRequiredProperty(s.OwnerFact.owns).getResource.getURI shouldBe company.id
    res.getRequiredProperty(s.OwnerFact.ownsSince).getString shouldBe dateStr(fact.ownsSince.get)
    res.getRequiredProperty(s.OwnerFact.sharePercents).getInt shouldBe fact.sharePercents.get
  }

  it should "register ownership minimal fact" in {
    val subj = Person("Darth_Vader", "Darth Vader")
    val company = GenericCompany("DS", "Death Star Inc.", "Tatouine")

    val fact = OwnerFact(id = "DV_user1_own",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      owns = company,
      ownsSince = None,
      sharePercents = None)
    val res = testFactCreation(fact, s.OwnerFact.a)

    res.getRequiredProperty(s.OwnerFact.owns).getResource.getURI shouldBe company.id
    res.hasProperty(s.OwnerFact.ownsSince) shouldBe false
    res.hasProperty(s.OwnerFact.sharePercents) shouldBe false
  }

  it should "register trademark ownersip fact" in {
    val subj = GenericCompany("Clones_Holding", "Clones Holding LTD", "PlanetA")
    val tm = TradeMark("BattleClone", "Battle Clone", "PlanetA")

    val fact = OwnerFact(id = "DV_user1_own",
      reportedBy = SAMPLE_USER,
      reportedAt = fixedClock.now(),
      media = None,
      articleUrl = "google.com",
      articlePublishedAt = None,
      subject = subj,
      owns = tm,
      ownsSince = None,
      sharePercents = None)
    val res = testFactCreation(fact, s.OwnerFact.a)

    res.getRequiredProperty(s.OwnerFact.owns).getResource.getURI shouldBe tm.id
    res.hasProperty(s.OwnerFact.ownsSince) shouldBe false
    res.hasProperty(s.OwnerFact.sharePercents) shouldBe false
  }

  private def testEntityCreation(entity: Entity, expectedType: Resource): Resource = {
    val TestEnv(storage, _, model) = createEnv()
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
    val TestEnv(storage, _, model) = createEnv()
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


  private def getUserRes(implicit rdfModel: Model): Resource = rdfModel.getResource(SAMPLE_USER.id)

  private val articleDateTime = DateTime.parse("2015-06-01T10:00:00Z")
  private val articleDateTimeStr = "2015-06-01T10:00:00.000Z"

  private def dateTimeStr(dt: DateTime) = ISODateTimeFormat.dateTime().print(dt)

  private def dateStr(d: LocalDate) = ISODateTimeFormat.date().print(d)
}
