package au.com.dius.pact.model

import org.specs2.mutable.Specification
import au.com.dius.pact.model.Pact.MergeSuccess
import au.com.dius.pact.model.Pact.MergeConflict

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PactSpec extends Specification {
  "Pact" should {
    "Locate Interactions" in {
      val description = "descriptiondata"
      val state = Some("stateData")

      val interaction = Interaction(
        description,
        state,
        Request(HttpMethod.Get,"", None, None, None, None),
        Response(200, None, None, None)
      )

      val pact = Pact(
        Provider("foo"),
        Consumer("bar"),
        Seq(interaction)
      )

      pact.interactionFor(description, state) must beSome(interaction)
    }

    "merge" should {
      import Fixtures._

      "allow different descriptions" in {
        val newInteractions = Seq(interaction.copy(description = "different"))
        val result = pact merge pact.copy(interactions = newInteractions)
        result must beEqualTo(MergeSuccess(Pact(provider, consumer, interactions ++ newInteractions )))
      }

      "allow different states" in {
        val newInteractions = Seq(interaction.copy(providerState = Some("different")))
        val result = pact merge pact.copy(interactions = newInteractions)
        result must beEqualTo(MergeSuccess(Pact(provider, consumer, interactions ++ newInteractions )))
      }

      "allow identical interactions without duplication" in {
        val result = pact merge pact.copy()
        result must beEqualTo(MergeSuccess(pact))
      }

      "refuse different requests for identical description and states" in {
        val newInteractions = Seq(interaction.copy(request = request.copy(path = "different")))
        val result = pact merge pact.copy(interactions = newInteractions)
        result must beEqualTo(MergeConflict(Seq((interaction, newInteractions.head))))
      }

      "refuse different responses for identical description and states" in {
        val newInteractions = Seq(interaction.copy(response = response.copy(status = 503)))
        val result = pact merge pact.copy(interactions = newInteractions)
        result must beEqualTo(MergeConflict(Seq((interaction, newInteractions.head))))
      }
    }

    "mimeType" should {
        "default to text" in {
            val request = Request(HttpMethod.Get,"", None, None, None, None)
            request.mimeType must beEqualTo("text/plain")
        }

        "get the mime type from the headers" in {
            val request = Request(HttpMethod.Get,"", None, Some(Map("Content-Type" -> "text/html")), None, None)
            request.mimeType must beEqualTo("text/html")
        }

        "handle charsets in the content type" in {
            val request = Request(HttpMethod.Get,"", None, Some(Map("Content-Type" -> "application/json; charset=UTF-8")), None, None)
            request.mimeType must beEqualTo("application/json")
        }

        "use regexp detection when not supplied" should {

          "for json" in {
            var request = Request(HttpMethod.Get,"", None, None, Some("{\"json\": true}"), None)
            request.mimeType must beEqualTo("application/json")
            request = Request(HttpMethod.Get,"", None, None, Some("{}"), None)
            request.mimeType must beEqualTo("application/json")
            request = Request(HttpMethod.Get,"", None, None, Some("[]"), None)
            request.mimeType must beEqualTo("application/json")
            request = Request(HttpMethod.Get,"", None, None, Some("[1,2,3]"), None)
            request.mimeType must beEqualTo("application/json")
            request = Request(HttpMethod.Get,"", None, None, Some("  \"string\"  "), None)
            request.mimeType must beEqualTo("application/json")
          }

          "for xml" in {
            var request = Request(HttpMethod.Get,"", None, None, Some("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<json>false</json>"), None)
            request.mimeType must beEqualTo("application/xml")
            request = Request(HttpMethod.Get,"", None, None, Some("<json>false</json>"), None)
            request.mimeType must beEqualTo("application/xml")
          }

          "for text" in {
            val request = Request(HttpMethod.Get,"", None, None, Some("this is not json"), None)
            request.mimeType must beEqualTo("text/plain")
          }

          "for html" in {
            val request = Request(HttpMethod.Get,"", None, None, Some("<html><body>this is also not json</body></html>"), None)
            request.mimeType must beEqualTo("text/html")
          }

        }
    }

    "request" should {
      "provide an way to find a header case insensitive" in {
        val request = Request(HttpMethod.Get, "", None, Some(Map("Cookie" -> "cookie-value")), None, None)
        request.findHeaderByCaseInsensitiveKey("cookie") must beSome("Cookie" -> "cookie-value")
      }
    }
  }
}
