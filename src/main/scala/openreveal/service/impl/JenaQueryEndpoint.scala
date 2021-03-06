package openreveal.service.impl

import com.hp.hpl.jena.rdf.model.{Resource, Statement, Selector, SimpleSelector}
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.model._
import openreveal.rdf.RdfModelProvider
import openreveal.schema.OpenRevealSchema.Entity
import openreveal.service.QueryEndpoint
import openreveal.schema.{OpenRevealSchema => S}
import org.joda.time.{LocalDate, DateTime}
import org.slf4j.LoggerFactory
import scala.collection.JavaConversions._

/**
 * Created by Paul Lysak on 02.07.15.
 */
class JenaQueryEndpoint(rdfModelProvider: RdfModelProvider) extends QueryEndpoint {
  override def discoverRelations(entity: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int, maxItems: Int): (Set[Entity], Set[Fact]) = {
    rdfModelProvider.readWithModel({model =>

      val ent = model.getResource(entity.id)
      //TODO retrieve schema fact types from factTypes
      val sFactTypes = factTypes.map({t => S.byClass(t).a})
      val facts = model.listStatements(new SimpleSelector(null, null, ent) {
        override def selects(s: Statement): Boolean = {
          sFactTypes.contains(s.getSubject.getProperty(RDF.`type`).getResource)
        }
      })
//      val facts = model.listSubjectsWithProperty(S.Fact.subject, ent)
      val factsL = facts.toList
      val factsS = factsL.toSet[Statement].flatMap(s => resourceToFact(s.getSubject).toSet)
      println(factsL)
      println(factsS)
      //TODO convert facts to domain objects
      val entitiesS: Set[Entity] = Set(entity) ++ factsS.flatMap(_.affectedEntities)

//      (Set(resourceToEntity(ent).get), factsS)
      (entitiesS, factsS)
    })
  }

  override def discoverConnection(entity1: Entity, entity2: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int, maxItems: Int): Set[Fact] = ???

  private def resourceToEntity(res: Resource): Option[Entity] = {
    val id = res.getURI
    val name = Option(res.getProperty(S.Entity.name)).map(_.getString).orNull
    val regIn = Option(res.getProperty(S.Registrable.registeredInCountry)).map(_.getString).orNull

    Option(res.getProperty(RDF.`type`)).map(_.getObject).fold[Option[Entity]]({
      log.warn(s"No type specified for entity resource $id")
      None
    })({
      case S.User.a =>
        val email = Option(res.getProperty(S.User.email)).map(_.getString).orNull
        Option(User(id, email))
      case S.Person.a => Option(Person(id, name))
      case S.Media.a => Option(Media(id, name, regIn))
      case S.GenericCompany.a => Option(GenericCompany(id, name, regIn))
      case a =>
        log.warn(s"Can't build entity from resource $id with type $a")
        None
    })
  }

  private def getDateTime(res: Resource, prop: com.hp.hpl.jena.rdf.model.Property): Option[DateTime] =
    Option(res.getProperty(prop)).map(s => DateTime.parse(s.getString))

  private def getLocalDate(res: Resource, prop: com.hp.hpl.jena.rdf.model.Property): Option[LocalDate] =
    Option(res.getProperty(prop)).map(s => LocalDate.parse(s.getString))


  private def getEntity(res: Resource, prop: com.hp.hpl.jena.rdf.model.Property): Option[Entity] =
    Option(res.getProperty(prop)).flatMap(r => resourceToEntity(r.getResource))

  private def getUser(res: Resource, prop: com.hp.hpl.jena.rdf.model.Property): Option[User] =
    getSpecificEntity(res, prop, classOf[User])
//    getEntity(res, prop) match {
//      case Some(u: User) => Some(u)
//      case Some(other) =>
//        log.warn(s"Not a User entity: $other")
//        None
//      case _ => None
//    }

  private def getMedia(res: Resource, prop: com.hp.hpl.jena.rdf.model.Property): Option[Media] =
    getSpecificEntity(res, prop, classOf[Media])
//    getEntity(res, prop) match {
//      case Some(m: Media) => Some(m)
//      case Some(other) =>
//        log.warn(s"Not a Media entity: $other")
//        None
//      case _ => None
//    }

  private def getSpecificEntity[E <: Entity](res: Resource, prop: com.hp.hpl.jena.rdf.model.Property, eClass: Class[E]): Option[E] =
    getEntity(res, prop) match {
      case Some(m: E) if eClass.isInstance(m) => Some(m)
      case Some(other) =>
        log.warn(s"Entity $other type isn't compatible with $eClass")
        None
      case _ => None
    }


  private def resourceToFact(res: Resource): Option[Fact] = {
    val id = res.getURI
    val reportedBy = getUser(res, S.Fact.reportedBy).orNull
    val reportedAt = getDateTime(res, S.Fact.reportedAt).orNull
    val media = getMedia(res, S.Fact.media)
    val articleUrl = res.getProperty(S.Fact.articleUrl).getString
    val articlePublishedAt = getDateTime(res, S.Fact.articlePublishedAt)
    val subject: Option[Entity] = getEntity(res, S.Fact.subject)

    Option(res.getProperty(RDF.`type`)).map(_.getObject).fold[Option[Fact]]({
      log.warn(s"No type specified for fact resource $id")
      None
    })(t => (t, subject) match {
      case (S.PersonFact.a, Some(s: Person)) =>
        val citizenOf: Set[String] = Option(res.getProperty(S.PersonFact.citizenOf)).
          toSet[Statement].flatMap(_.getBag.iterator().map(_.toString).toSet)
        val livesIn = Option(res.getProperty(S.PersonFact.livesIn)).map(_.getString)
        Option(PersonFact(
          id, reportedBy, reportedAt,
          media, articleUrl, articlePublishedAt,
          s,
          citizenOf, livesIn))
      case (S.OwnerFact.a, Some(s: Owner)) =>
        val owns = getSpecificEntity(res, S.OwnerFact.owns, classOf[Property])
        val ownsSince = getLocalDate(res, S.OwnerFact.ownsSince)
        val sharePercents = Option(res.getProperty(S.OwnerFact.sharePercents)).map(_.getInt)

        Option(OwnerFact(
          id, reportedBy, reportedAt,
          media, articleUrl, articlePublishedAt,
          s,
          owns.get, ownsSince, sharePercents))
      case a =>
        log.warn(s"Can't build fact from resource $id with type $a and subject $subject")
        None
    })
  }


  private val log = LoggerFactory.getLogger(classOf[JenaQueryEndpoint])
}
