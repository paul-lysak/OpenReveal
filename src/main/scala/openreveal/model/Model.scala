package openreveal.model

import org.joda.time.{LocalDate, DateTime}

/**
 * Created by Paul Lysak on 02.06.15.
 */

trait Id {
  val id: String
}

//Entities

trait Entity extends Id {
  val name: String
}

trait Owner extends Entity;

trait Property extends Entity;

trait Registrable {
  val registeredInCountry: Country
}

trait Organization extends Owner with Registrable

trait Company extends Organization with Property


case class User(id: String, email: String) extends Entity {
  val name = id
}

case class PoliticalParty(id: String, name: String, registeredInCountry: String) extends Organization

case class Person(id: String,
                  name: String) extends Owner

case class TradeMark(id: String,
                     name: String,
                     registeredInCountry: Country) extends Property with Registrable

//Company types
case class GenericCompany(id: String, name: String, registeredInCountry: Country) extends Company

case class Media(id: String, name: String, registeredInCountry: Country) extends Company


//Facts

trait Fact extends Id {
  type Subject <: Entity

  //TODO get rid of type params, use path-dependent types
  val id: String
  val reportedBy: User
  val reportedAt: DateTime

  val articleUrl: String
  val articlePublishedAt: Option[DateTime]
  val media: Option[Media]

  val subject: Subject

  def affectedEntities: Set[Entity] = Set(subject)
}

case class EntityDefinition[T <: Entity](reportedBy: User,
                                         reportedAt: DateTime,
                                         entity: T)

case class PersonFact(id: String,
                     reportedBy: User,
                     reportedAt: DateTime,
                     media: Option[Media],
                     articleUrl: String,
                     articlePublishedAt: Option[DateTime],
                     subject: Person,

                     citizenOf: Set[Country] = Set(),
                     livesIn: Option[String] = None) extends Fact {
  type Subject = Person
}


case class OwnerFact(id: String,
                     reportedBy: User,
                     reportedAt: DateTime,
                     media: Option[Media],
                     articleUrl: String,
                     articlePublishedAt: Option[DateTime],
                     subject: Owner,

                     owns: Property,
                     ownsSince: Option[LocalDate] = None,
                     sharePercents: Option[Int] = None) extends Fact {
  type Subject = Owner

  override def affectedEntities: Set[Entity] = super.affectedEntities ++ Set(owns)
}

case class MemberFact(id: String,
                     reportedBy: User,
                     reportedAt: DateTime,
                     media: Option[Media],
                     articleUrl: String,
                     articlePublishedAt: Option[DateTime],
                     subject: Person,

                     memberOf: Organization,
                     memberSince: Option[LocalDate],
                     position: Option[String],
                     positionSince: Option[LocalDate]) extends Fact {
  type Subject = Person

  override def affectedEntities: Set[Entity] = super.affectedEntities ++ Set(memberOf)
}



