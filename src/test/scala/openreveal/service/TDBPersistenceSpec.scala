package openreveal.service

import java.nio.file.Files

import com.hp.hpl.jena.vocabulary.RDF
import openreveal.rdf.RdfTdbModelProvider
import openreveal.schema.OpenRevealSchema
import openreveal.service.impl.JenaFactStorage
import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, FlatSpec}

/**
 * Created by Paul Lysak on 03.06.15.
 *
 */
class TDBPersistenceSpec extends FlatSpec with BeforeAndAfterAll {
  val tmpDir = Files.createTempDirectory("openreveal_tdb_test")

  "TDB" should "persist some data" in {
    val modelProvider = new RdfTdbModelProvider(tmpDir.toString)

    val storage1 = new JenaFactStorage(modelProvider, DefaultClock)
    storage1.createUser("user1", "user1@a.b.com")

    modelProvider.readWithModel({ model =>
      val userRes1 = model.getResource("user1")
      userRes1.getRequiredProperty(RDF.`type`).getObject === OpenRevealSchema.User.a
    })
  }

  override def afterAll() {
    FileUtils.deleteDirectory(tmpDir.toFile)
  }
}
