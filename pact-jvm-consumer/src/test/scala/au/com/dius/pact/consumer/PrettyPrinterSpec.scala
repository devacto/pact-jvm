package au.com.dius.pact.consumer

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import au.com.dius.pact.model._
import Fixtures._
import au.com.dius.pact.model.PathMismatch
import au.com.dius.pact.model.HeaderMismatch
import org.json4s.jackson.JsonMethods._
import au.com.dius.pact.model.PathMismatch
import au.com.dius.pact.model.BodyMismatch
import au.com.dius.pact.model.HeaderMismatch
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PrettyPrinterSpec extends Specification {
  def print(mismatch: RequestPartMismatch) = {
    PrettyPrinter.print(PactSessionResults.empty.addAlmostMatched(PartialRequestMatch(interaction, Seq(mismatch))))
  }

  def plus = "+++ "

  "header mismatch" in {
    print(HeaderMismatch(Map("foo"-> "bar"), Map())) must beEqualTo(
      s"""--- Headers
        |$plus
        |@@ -1,1 +1,0 @@
        |-foo = bar""".stripMargin
    )
  }

  "path mismatch" in {
    print(PathMismatch("/foo/bar", "/foo/baz")) must beEqualTo(
    s"""--- Path
      |$plus
      |@@ -1,1 +1,1 @@
      |-/foo/bar
      |+/foo/baz""".stripMargin
    )
  }

  "body mismatch" in {
    import org.json4s.JsonDSL._

    print(BodyMismatch(pretty(map2jvalue(Map("foo"->"bar"))), pretty(map2jvalue(Map("ork" -> "Bif"))))) must beEqualTo(
    s"""--- Body
      |$plus
      |@@ -1,3 +1,3 @@
      | {
      |-  "foo" : "bar"
      |+  "ork" : "Bif"
      | }""".stripMargin
    )
  }
}
