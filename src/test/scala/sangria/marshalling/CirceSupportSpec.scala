package sangria.marshalling

import sangria.marshalling.circe._
import sangria.marshalling.testkit._

import io.circe.Json
import io.circe.generic.auto._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CirceSupportSpec
    extends AnyWordSpec
    with Matchers
    with MarshallingBehaviour
    with InputHandlingBehaviour {
  "Circe integration" should {
    behave.like(`value (un)marshaller`(CirceResultMarshaller))

    behave.like(`AST-based input unmarshaller`(circeFromInput))
    behave.like(`AST-based input marshaller`(CirceResultMarshaller))

    behave.like(`case class input unmarshaller`)
    behave.like(`case class input marshaller`(CirceResultMarshaller))
  }

  val toRender = Json.obj(
    "a" -> Json.arr(
      Json.Null,
      Json.fromInt(123),
      Json.arr(Json.obj("foo" -> Json.fromString("bar")))),
    "b" -> Json.obj("c" -> Json.fromBoolean(true), "d" -> Json.Null)
  )

  "InputUnmarshaller" should {
    "throw an exception on invalid scalar values" in {
      an[IllegalStateException] should be thrownBy
        CirceInputUnmarshaller.getScalarValue(Json.obj())
    }

    "throw an exception on variable names" in {
      an[IllegalArgumentException] should be thrownBy
        CirceInputUnmarshaller.getVariableName(Json.fromString("$foo"))
    }

    "render JSON values" in {
      val rendered = CirceInputUnmarshaller.render(toRender)

      rendered should be("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }

  "ResultMarshaller" should {
    "render pretty JSON values" in {
      val rendered = CirceResultMarshaller.renderPretty(toRender)

      rendered.replaceAll("\r", "") should be("""{
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

      rendered should be("""{"a":[null,123,[{"foo":"bar"}]],"b":{"c":true,"d":null}}""")
    }
  }
}
