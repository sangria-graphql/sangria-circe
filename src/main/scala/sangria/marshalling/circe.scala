package sangria.marshalling

import io.circe._

object circe {
  implicit object CirceResultMarshaller extends ResultMarshaller {
    type Node = Json
    type MapBuilder = ArrayMapBuilder[Node]

    def emptyMapNode(keys: Seq[String]) = new ArrayMapBuilder[Node](keys)
    def addMapNodeElem(builder: MapBuilder, key: String, value: Node, optional: Boolean) = builder.add(key, value)

    def mapNode(builder: MapBuilder) = Json.fromFields(builder)
    def mapNode(keyValues: Seq[(String, Json)]) = Json.fromFields(keyValues)

    def arrayNode(values: Vector[Json]) = Json.fromValues(values)
    def optionalArrayNodeValue(value: Option[Json]) = value match {
      case Some(v) ⇒ v
      case None ⇒ nullNode
    }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]) = value match {
      case v: String ⇒ Json.fromString(v)
      case v: Char => Json.fromString(v.toString)
      case v: Boolean ⇒ Json.fromBoolean(v)
      case v: Int ⇒ Json.fromInt(v)
      case v: Long ⇒ Json.fromLong(v)
      case v: Float ⇒ Json.fromDoubleOrNull(v)
      case v: Double ⇒ Json.fromDoubleOrNull(v)
      case v: BigInt ⇒ Json.fromBigInt(v)
      case v: BigDecimal ⇒ Json.fromBigDecimal(v)
      case v ⇒ throw new IllegalArgumentException("Unsupported scalar value: " + v)
    }

    def enumNode(value: String, typeName: String) = Json.fromString(value)

    def nullNode = Json.Null

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
    def getMapKeys(node: Json) = node.asObject.get.keys

    def isListNode(node: Json) = node.isArray
    def getListValue(node: Json) = node.asArray.get

    def isDefined(node: Json) = !node.isNull
    def getScalarValue(node: Json) = {
      def invalidScalar = throw new IllegalStateException(s"$node is not a scalar value")

      node.fold(
        jsonNull = invalidScalar,
        jsonBoolean = identity,
        jsonNumber = num ⇒ num.toBigInt orElse num.toBigDecimal getOrElse invalidScalar,
        jsonString = identity,
        jsonArray = _ ⇒ invalidScalar,
        jsonObject = _ ⇒ invalidScalar
      )
    }

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
