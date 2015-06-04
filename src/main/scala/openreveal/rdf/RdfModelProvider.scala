package openreveal.rdf

import com.hp.hpl.jena.rdf.model.Model

/**
 * Created by Paul Lysak on 04.06.15.
 */
trait RdfModelProvider {
  def readWithModel[T](op: (Model => T)): T

  def writeWithModel[T](op: (Model => T)): T
}
