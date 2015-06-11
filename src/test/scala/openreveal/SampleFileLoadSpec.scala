package openreveal

import com.hp.hpl.jena.rdf.model.ModelFactory
import com.hp.hpl.jena.util.FileManager
import org.scalatest.FlatSpec

/**
 * Created by Paul Lysak on 30.05.15.
 */
class SampleFileLoadSpec extends FlatSpec {

  "Jena" should "read turtle file" in {
    // create an empty model
    val inputFileName = "src/test/resources/sample_data.turtle"
    val model = ModelFactory.createDefaultModel();

    val in = FileManager.get().open(inputFileName);
    if (in == null) {
      throw new IllegalArgumentException("File: " + inputFileName + " not found");
    }

    //read the RDF/XML file
    model.read(in, "", "TURTLE");

    // write it to standard out
    model.write(System.out);
  }
}
