package sangria.marshalling

import io.circe.Json
import org.scalatest.{Matchers, WordSpec}

import sangria.marshalling.circe._
import sangria.marshalling.testkit._

class CirceSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour {
  "SprayJson integration" should {
    behave like `value (un)marshaller` (CirceResultMarshaller)

    behave like `AST-based input unmarshaller` (circeJsonFromInput)
    behave like `AST-based input marshaller` (CirceResultMarshaller)
  }

  val toRender = Json.obj(
    "a" → Json.array(Json.empty, Json.int(123), Json.array(Json.obj("foo" → Json.string("bar")))),
    "b" → Json.obj(
      "c" → Json.bool(true),
      "d" → Json.empty))

  "InputUnmarshaller" should {
    "throw an exception on invalid scalar values" in {
      an [IllegalStateException] should be thrownBy
          CirceInputUnmarshaller.getScalarValue(Json.obj())
    }

    "throw an exception on variable names" in {
      an [IllegalArgumentException] should be thrownBy
          CirceInputUnmarshaller.getVariableName(Json.string("$foo"))
    }

    "render JSON values" in {
      val rendered = CirceInputUnmarshaller.render(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }

  "ResultMarshaller" should {
    "render pretty JSON values" in {
      val rendered = CirceResultMarshaller.renderPretty(toRender)

      rendered.replaceAll("\r", "") should be (
        """{
          |  "a" : [
          |    null,
          |    123,
          |    [
          |      {
          |        "foo" : "bar"
          |      }
          |    ]
          |  ],
          |  "b" : {
          |    "c" : true,
          |    "d" : null
          |  }
          |}""".stripMargin.replaceAll("\r", ""))
    }

    "render compact JSON values" in {
      val rendered = CirceResultMarshaller.renderCompact(toRender)

      rendered should be ("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }
}
