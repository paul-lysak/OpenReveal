package openreveal.model

import org.joda.time.DateTime

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
//TODO represent following properties as a fact
//                  citizenOf: Seq[Country],
//                  livesIn: Option[Country]) extends Owner

case class TradeMark(id: String,
                     name: String,
                     registeredInCountry: Country) extends Property with Registrable

//Company types
case class GenericCompany(id: String, name: String, registeredInCountry: String) extends Company

case class Media(id: String, name: String, registeredInCountry: String) extends Company


//Facts

trait Fact extends Id {
  val id: String
  val reportedBy: User
  val reportedAt: DateTime
}

trait ArticleFact extends Fact {
  val articleUrl: String
  val articlePublishedAt: Option[DateTime]
  val media: Option[Media]
}

case class EntityDefinition[T <: Entity](reportedBy: User,
                                         reportedAt: DateTime,
                                         entity: T)

case class OwnerFact(id: String,
                     reportedBy: User,
                     reportedAt: DateTime,
                     media: Option[Media],
                     articleUrl: String,
                     articlePublishedAt: Option[DateTime],
                     subject: Owner,
                     owns: Property,
                     sharePercents: Int) extends Fact



