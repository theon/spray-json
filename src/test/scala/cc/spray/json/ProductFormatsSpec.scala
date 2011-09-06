package cc.spray.json

import org.specs2.mutable._

class ProductFormatsSpec extends Specification with DefaultJsonProtocol {

  case class Test2(a: Int, b: Double)
  implicit val test2Format = jsonFormat(Test2, "a", "b")

  case class Test3[A, B](as: List[A], bs: List[B])
  implicit def test3Format[A: JsonFormat, B: JsonFormat] = jsonFormat(Test3.apply[A, B], "as", "bs")
  
  "A JsonFormat created with `jsonFormat`, for a case class with 2 elements," should {
    val obj = Test2(42, 4.2)
    val json = JsObject(JsField("a", 42), JsField("b", 4.2))
    "convert to a respective JsObject" in {
      obj.toJson mustEqual json
    }
    "convert a JsObject to the respective case class instance" in {
      json.fromJson[Test2] mustEqual obj 
    }
    "throw a DeserializationException if the JsObject does not define the right members" in (
      JsObject(JsField("a", 42), JsField("x", 4.2)).fromJson[Test2] must
              throwA(new DeserializationException("Object is missing required member 'b'"))  
    )
    "ignore additional members during deserialization" in {
      JsObject(JsField("a", 42), JsField("b", 4.2), JsField("c", 'no)).fromJson[Test2] mustEqual obj 
    }
    "throw a DeserializationException if the JsValue is not a JsObject" in (
      JsNull.fromJson[Test2] must throwA(new DeserializationException("Object expected"))  
    )
  }

  "A JsonFormat for a generic case class and created with `jsonFormat`" should {
    val obj = Test3(42 :: 43 :: Nil, "x" :: "y" :: "z" :: Nil)
    val json = JsObject(
      JsField("as", JsArray(JsNumber(42), JsNumber(43))),
      JsField("bs", JsArray(JsString("x"), JsString("y"), JsString("z")))
    )
    "convert to a respective JsObject" in {
      obj.toJson mustEqual json
    }
    "convert a JsObject to the respective case class instance" in {
      json.fromJson[Test3[Int, String]] mustEqual obj
    }
  }

}