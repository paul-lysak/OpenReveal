package openreveal.service.impl

import com.hp.hpl.jena.rdf.model.{Statement, Selector, SimpleSelector}
import com.hp.hpl.jena.vocabulary.RDF
import openreveal.model.{Fact, Entity}
import openreveal.rdf.RdfModelProvider
import openreveal.service.QueryEndpoint
import openreveal.schema.{OpenRevealSchema => S}

/**
 * Created by Paul Lysak on 02.07.15.
 */
class JenaQueryEndpoint(rdfModelProvider: RdfModelProvider) extends QueryEndpoint {
  override def discoverRelations(entity: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int, maxItems: Int): (Set[Entity], Set[Fact]) = {
    rdfModelProvider.readWithModel({model =>

      val ent = model.getResource(entity.id)
      //TODO retrieve schema fact types from factTypes
      val sFactTypes = Set(S.OwnerFact.a, S.PersonFact.a)
      val facts = model.listStatements(new SimpleSelector(null, null, ent) {
        override def selects(s: Statement): Boolean = {
          sFactTypes.contains(s.getSubject.getProperty(RDF.`type`).getResource)
        }
      })
//      val facts = model.listSubjectsWithProperty(S.Fact.subject, ent)
      val factsL = facts.toList
      println(factsL)
      //TODO convert facts to domain objects

      ???
    })
  }

  override def discoverConnection(entity1: Entity, entity2: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int, maxItems: Int): Set[Fact] = ???
}
