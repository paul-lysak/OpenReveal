package openreveal.service

import openreveal.model._
import openreveal.schema.{OpenRevealSchema => S}
import org.joda.time.DateTime
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by Paul Lysak on 02.07.15.
 */
class QueryEndpointSpec extends FlatSpec with Matchers with JenaSpecCommons {
  "FactStorage" should "discover direct 1-level relations" in {
    val TestEnv(storage, qEndpoint, model) = createEnv()

    val p1 = Person("p1", "Person One")
    val c1 = GenericCompany("c1", "Company One", "UA")
    val m1 = Media("m1", "Media One", "UA")

    val articleDateTime = DateTime.parse("2015-06-01T10:00:00Z").toDateTimeISO

    val fOwns1 = OwnerFact("fOwns1", SAMPLE_USER, sampleDateTime,
      None, "news.site.com/article1", None,
      subject = p1,
      owns = c1,
      ownsSince = None,
      sharePercents = Some(10))

    val fPerson1 = PersonFact("fPerson1", SAMPLE_USER, sampleDateTime,
      Some(m1), "news.site.com/article1", Some(articleDateTime),
      subject = p1,
      citizenOf = Set("UA"),
      livesIn = Some("DE"))

    storage.defineEntity(EntityDefinition(SAMPLE_USER, sampleDateTime, p1))
    storage.defineEntity(EntityDefinition(SAMPLE_USER, sampleDateTime, c1))
    storage.defineEntity(EntityDefinition(SAMPLE_USER, sampleDateTime, m1))

    storage.saveFact(fOwns1)
    storage.saveFact(fPerson1)

    qEndpoint.discoverRelations(p1, Set()) shouldBe (Set(p1), Set())
    qEndpoint.discoverRelations(p1, Set(classOf[PersonFact])) shouldEqual (Set(p1), Set(fPerson1))
    qEndpoint.discoverRelations(p1, Set(classOf[OwnerFact])) shouldBe (Set(p1, c1), Set(fOwns1))
    qEndpoint.discoverRelations(p1, Set(classOf[OwnerFact], classOf[PersonFact])) shouldBe (Set(p1, c1), Set(fPerson1, fOwns1))
  }


}
