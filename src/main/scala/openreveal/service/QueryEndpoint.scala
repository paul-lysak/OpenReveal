package openreveal.service

import openreveal.model.{Fact, Entity}

/**
 * Created by Paul Lysak on 02.06.15.
 */
trait QueryEndpoint {
  def discoverRelations(entity: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int = 5, maxItems: Int = 1000): (Set[Entity], Set[Fact])

  def discoverConnection(entity1: Entity, entity2: Entity, factTypes: Set[Class[_ <: Fact]], maxDepth: Int = 5, maxItems: Int = 1000): Set[Fact]
}

