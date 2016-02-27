package sangria.marshalling

import io.circe._
import cats.data.Xor.{Right, Left}

object circe {
  implicit object CirceResultMarshaller extends ResultMarshaller {
    type Node = Json

    def emptyMapNode = Json.obj()
    def mapNode(keyValues: Seq[(String, Json)]) = Json.obj(keyValues: _*)
    def addMapNodeElem(node: Json, key: String, value: Json, optional: Boolean) = node.mapObject(_ add (key, value))

    def arrayNode(values: Vector[Json]) = Json.array(values: _*)
    def optionalArrayNodeValue(value: Option[Json]) = value match {
      case Some(v) ⇒ v
      case None ⇒ nullNode
    }

    def booleanNode(value: Boolean) = Json.bool(value)
    def floatNode(value: Double) = Json.number(value).get
    def stringNode(value: String) = Json.string(value)
    def intNode(value: Int) = Json.int(value)
    def bigIntNode(value: BigInt) = Json.bigDecimal(BigDecimal(value))
    def bigDecimalNode(value: BigDecimal) = Json.bigDecimal(value)

    def nullNode = Json.empty

    def renderCompact(node: Json) = node.noSpaces
    def renderPretty(node: Json) = node.spaces2
  }

  implicit object CirceMarshallerForType extends ResultMarshallerForType[Json] {
    val marshaller = CirceResultMarshaller
  }

  implicit object CirceInputUnmarshaller extends InputUnmarshaller[Json] {
    def getRootMapValue(node: Json, key: String) = node.asObject.get(key)

    def isMapNode(node: Json) = node.isObject
    def getMapValue(node: Json, key: String) = node.asObject.get(key)
    def getMapKeys(node: Json) = node.asObject.get.fields

    def isListNode(node: Json) = node.isArray
    def getListValue(node: Json) = node.asArray.get

    def isDefined(node: Json) = !node.isNull
    def getScalarValue(node: Json) =
      if (node.isBoolean)
        node.asBoolean.get
      else if (node.isNumber) {
        val num = node.asNumber.get
        
        (num.toBigInt orElse num.toBigDecimal).get
      } else if (node.isString)
        node.asString.get
      else
        throw new IllegalStateException(s"$node is not a scalar value")

    def getScalaScalarValue(node: Json) = getScalarValue(node)

    def isEnumNode(node: Json) = node.isString

    def isScalarNode(node: Json) =
      node.isBoolean || node.isNumber || node.isString

    def isVariableNode(node: Json) = false
    def getVariableName(node: Json) = throw new IllegalArgumentException("variables are not supported")

    def render(node: Json) = node.noSpaces
  }

  implicit object circeToInput extends ToInput[Json, Json] {
    def toInput(value: Json) = (value, CirceInputUnmarshaller)
  }

  implicit object circeFromInput extends FromInput[Json] {
    val marshaller = CirceResultMarshaller
    def fromResult(node: marshaller.Node) = node
  }

  implicit def circeEncoderToInput[T : Encoder]: ToInput[T, Json] =
    new ToInput[T, Json] {
      def toInput(value: T) = implicitly[Encoder[T]].apply(value) → CirceInputUnmarshaller
    }

  implicit def circeDecoderFromInput[T : Decoder]: FromInput[T] =
    new FromInput[T] {
      val marshaller = CirceResultMarshaller
      def fromResult(node: marshaller.Node) = implicitly[Decoder[T]].decodeJson(node) match {
        case Right(obj) ⇒ obj
        case Left(error) ⇒ throw InputParsingError(Vector(error.getMessage))
      }
    }
}