package openreveal.service.impl

import java.util.Random

import com.hp.hpl.jena.rdf.model.Model
import openreveal.model.Fact
import openreveal.service.{Clock, FactStorage}

import scala.util.Random


/**
 * Created by Paul Lysak on 02.06.15.
 */
class JenaFactStorage(rdfModel: Model, val clock: Clock) extends FactStorage {
  override def saveFact(fact: Fact): Unit = {
    //TODO convert fact to triples and save it to underlying rdfModel
    ???
  }

  override def generateFactId(): String =
    //TODO more secure uuid-like algorithm
    Random.nextString(6)
}
