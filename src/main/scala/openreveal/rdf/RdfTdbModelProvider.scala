package openreveal.rdf

import com.hp.hpl.jena.query.ReadWrite
import com.hp.hpl.jena.rdf.model.Model
import com.hp.hpl.jena.tdb.TDBFactory

/**
 * Created by Paul Lysak on 04.06.15.
 */
class RdfTdbModelProvider(dir: String) extends RdfModelProvider {
  override def readWithModel[T](op: (Model) => T): T = {
    val dataset = getDataset()
    dataset.begin(ReadWrite.READ)
    try {
      op(dataset.getDefaultModel)
    } finally {
      dataset.end()
    }
  }

  override def writeWithModel[T](op: (Model) => T): T = {
    val dataset = getDataset()
    dataset.begin(ReadWrite.WRITE)
    try {
      val res = op(dataset.getDefaultModel)
      dataset.commit()
      res
    } finally {
      dataset.end()
    }
  }


  private def getDataset() =  TDBFactory.createDataset(dir)
}
