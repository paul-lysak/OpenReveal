package openreveal.rdf

import com.hp.hpl.jena.rdf.model.{Model, ModelFactory}

/**
 * Created by Paul Lysak on 04.06.15.
 */
class RdfInMemoryModelProvider extends RdfModelProvider {
  val model = ModelFactory.createDefaultModel()

  override def readWithModel[T](op: (Model) => T): T = op(model)

  override def writeWithModel[T](op: (Model) => T): T = op(model)
}
