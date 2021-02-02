package sangria.marshalling

import io.circe._
import sangria.util.tag
import sangria.util.tag.@@

object circe {
  implicit object CirceResultMarshaller extends ResultMarshaller {
    type Node = Json
    type MapBuilder = ArrayMapBuilder[Node]

    def emptyMapNode(keys: Seq[String]) = new ArrayMapBuilder[Node](keys)
    def addMapNodeElem(
        builder: MapBuilder,
        key: String,
        value: Node,
        optional: Boolean): ArrayMapBuilder[Json] =
      builder.add(key, value)

    def mapNode(builder: MapBuilder): Json = Json.fromFields(builder)
    def mapNode(keyValues: Seq[(String, Json)]): Json = Json.fromFields(keyValues)

    def arrayNode(values: Vector[Json]): Json = Json.fromValues(values)
    def optionalArrayNodeValue(value: Option[Json]): Json = value match {
      case Some(v) => v
      case None => nullNode
    }

    def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]): Json = value match {
      case v: String => Json.fromString(v)
      case v: Boolean => Json.fromBoolean(v)
      case v: Int => Json.fromInt(v)
      case v: Long => Json.fromLong(v)
      case v: Float => Json.fromDoubleOrNull(v)
      case v: Double => Json.fromDoubleOrNull(v)
      case v: BigInt => Json.fromBigInt(v)
      case v: BigDecimal => Json.fromBigDecimal(v)
      case v => throw new IllegalArgumentException("Unsupported scalar value: " + v)
    }

    def enumNode(value: String, typeName: String): Json = Json.fromString(value)

    def nullNode: Json = Json.Null

    def renderCompact(node: Json): String = node.noSpaces
    def renderPretty(node: Json): String = node.spaces2
  }

  implicit object CirceMarshallerForType extends ResultMarshallerForType[Json] {
    val marshaller: CirceResultMarshaller.type = CirceResultMarshaller
  }

  implicit object CirceInputUnmarshaller extends InputUnmarshaller[Json] {
    def getRootMapValue(node: Json, key: String): Option[Json] = node.asObject.get(key)

    def isMapNode(node: Json): Boolean = node.isObject
    def getMapValue(node: Json, key: String): Option[Json] = node.asObject.get(key)
    def getMapKeys(node: Json): Iterable[String] = node.asObject.get.keys

    def isListNode(node: Json): Boolean = node.isArray
    def getListValue(node: Json): Vector[Json] = node.asArray.get

    def isDefined(node: Json): Boolean = !node.isNull
    def getScalarValue(node: Json): Any = {
      def invalidScalar = throw new IllegalStateException(s"$node is not a scalar value")

      node.fold(
        jsonNull = invalidScalar,
        jsonBoolean = identity,
        jsonNumber = num => num.toBigInt.orElse(num.toBigDecimal).getOrElse(invalidScalar),
        jsonString = identity,
        jsonArray = _ => invalidScalar,
        jsonObject = _ => invalidScalar
      )
    }

    def getScalaScalarValue(node: Json): Any = getScalarValue(node)

    def isEnumNode(node: Json): Boolean = node.isString

    def isScalarNode(node: Json): Boolean =
      node.isBoolean || node.isNumber || node.isString

    def isVariableNode(node: Json) = false
    def getVariableName(node: Json) = throw new IllegalArgumentException(
      "variables are not supported")

    def render(node: Json): String = node.noSpaces
  }

  implicit object circeToInput extends ToInput[Json, Json] {
    def toInput(value: Json): (Json, CirceInputUnmarshaller.type) = (value, CirceInputUnmarshaller)
  }

  implicit object circeFromInput extends FromInput[Json] {
    val marshaller: CirceResultMarshaller.type = CirceResultMarshaller
    def fromResult(node: marshaller.Node): marshaller.Node = node
  }

  implicit def circeEncoderToInput[T: Encoder]: ToInput[T, Json] =
    (value: T) => implicitly[Encoder[T]].apply(value) -> CirceInputUnmarshaller

  implicit def circeDecoderFromInput[T: Decoder]: FromInput[T] =
    new FromInput[T] {
      val marshaller: CirceResultMarshaller.type = CirceResultMarshaller
      def fromResult(node: marshaller.Node): T = implicitly[Decoder[T]].decodeJson(node) match {
        case Right(obj) => obj
        case Left(error) => throw InputParsingError(Vector(error.getMessage))
      }
    }

  implicit object CirceAnyInputUnmarshaller extends InputUnmarshaller[Json @@ AnySupport] {
    type Node = Json @@ AnySupport

    def getRootMapValue(node: Node, key: String): Option[Node] =
      node.asObject.get(key).map(value => tag[AnySupport].apply[Json](value))

    def isMapNode(node: Node): Boolean = node.isObject
    def getMapValue(node: Node, key: String): Option[Node] =
      node.asObject.get(key).map(value => tag[AnySupport].apply[Json](value))

    def getMapKeys(node: Node): Iterable[String] = node.asObject.get.keys

    def isListNode(node: Node): Boolean = node.isArray
    def getListValue(node: Node): Vector[Node] =
      node.asArray.get.map(value => tag[AnySupport].apply[Json](value))

    def isDefined(node: Node): Boolean = !node.isNull
    def getScalarValue(node: Node): Any = {
      def invalidScalar = throw new IllegalStateException(s"$node is not a scalar value")

      node.fold(
        jsonNull = invalidScalar,
        jsonBoolean = identity,
        jsonNumber = num => num.toBigInt.orElse(num.toBigDecimal).getOrElse(invalidScalar),
        jsonString = identity,
        jsonArray = _ => invalidScalar,
        jsonObject = jsonObject => Json.fromJsonObject(jsonObject)
      )
    }

    import sangria.marshalling.MarshallingUtil._
    import sangria.marshalling.scalaMarshalling._

    def getScalaScalarValue(node: Node): Any =
      if (node.isObject) convert[Json @@ AnySupport, Any @@ ScalaInput](node)
      else  getScalarValue(node)

    def isEnumNode(node: Node): Boolean = node.isString

    def isScalarNode(node: Node): Boolean =
      node.isObject || node.isBoolean || node.isNumber || node.isString

    def isVariableNode(node: Node) = false
    def getVariableName(node: Node) = throw new IllegalArgumentException(
      "variables are not supported")

    def render(node: Node): String = node.noSpaces
  }
}
