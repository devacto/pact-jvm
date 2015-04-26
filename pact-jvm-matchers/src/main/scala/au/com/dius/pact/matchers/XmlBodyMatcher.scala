package au.com.dius.pact.matchers

import au.com.dius.pact.model.{BodyMismatch, DiffConfig, HttpPart}

import scala.xml.{Elem, XML}

class XmlBodyMatcher extends BodyMatcher {

  override def matchBody(expected: HttpPart, actual: HttpPart, diffConfig: DiffConfig): List[BodyMismatch] = {
    (expected.body, actual.body) match {
      case (None, None) => List[BodyMismatch]()
      case (None, b) => List[BodyMismatch]()
      case (a, None) => List(BodyMismatch(a, None))
      case (Some(a), Some(b)) => compareElem("$.body", parse(a), parse(b), diffConfig, expected.matchers)
    }
  }

  def parse(xmlData: String) = {
    XML.loadString(xmlData)
  }

  def compareElem(path: String, expected: Elem, actual: Elem, config: DiffConfig,
              matchers: Option[Map[String, Map[String, String]]]): List[BodyMismatch] = {
      if (actual.label != expected.label) {
        List(BodyMismatch(expected, actual, Some(s"Expected element ${expected.label} but received ${actual.label}"), path))
      } else {
        List[BodyMismatch]()
      }

//    if (expectedValues.isEmpty && actualValues.nonEmpty) {
//      List(BodyMismatch(a, b, Some(s"Expected an empty Map but received ${valueOf(actualValues)}"), path))
//    } else {
//      var result = List[BodyMismatch]()
//      if ((diffConfig.allowUnexpectedKeys && expectedValues.size > actualValues.size) ||
//        (!diffConfig.allowUnexpectedKeys && expectedValues.size != actualValues.size)) {
//        result = result :+ BodyMismatch(a, b, Some(s"Expected a Map with at least ${expectedValues.size} elements but received ${actualValues.size} elements"), path)
//      }
//      expectedValues.foreach(entry => {
//        val s = path + "." + entry._1
//        if (actualValues.contains(entry._1)) {
//          result = result ++: compare(s, entry._2, actualValues(entry._1), diffConfig, matchers)
//        } else {
//          result = result :+ BodyMismatch(a, b, Some(s"Expected ${entry._1}=${valueOf(entry._2)} but was missing"), path)
//        }
//      })
//      result
//    }
  }

  /*implicit lazy val formats = DefaultFormats

  def matchBody(expected: HttpPart, actual: HttpPart, diffConfig: DiffConfig): List[BodyMismatch] = {
    (expected.body, actual.body) match {
      case (None, None) => List()
      case (None, b) => if (diffConfig.structural) {
        List()
      } else {
        List(BodyMismatch(None, b))
      }
      case (a, None) => List(BodyMismatch(a, None))
      case (Some(a), Some(b)) => compare("$.body", parse(a), parse(b), diffConfig, expected.matchers)
    }
  }

  def valueOf(value: Any) = {
    value match {
      case s: String => s"'$value'"
      case null => "null"
      case _ => value.toString
    }
  }

  def typeOf(value: Any) = {
    if (value == null) {
      "Null"
    } else value match {
      case _: Map[Any, Any] =>
        "Map"
      case _: List[Any] =>
        "List"
      case _ =>
        value.getClass.getSimpleName
    }
  }

  def compare(path: String, expected: Any, actual: Any, diffConfig: DiffConfig, matchers: Option[Map[String, Map[String, String]]]): List[BodyMismatch] = {
    (expected, actual) match {
      case (a: JObject, b: JObject) => compareMaps(a.values, b.values, a, b, path, diffConfig, matchers)
      case (a: Map[String, Any], b: Map[String, Any]) => compareMaps(a, b, a, b, path, diffConfig, matchers)
      case (a: JArray, b: JArray) => compareLists(a.values, b.values, a, b, path, diffConfig, matchers)
      case (a: List[Any], b: List[Any]) => compareLists(a, b, a, b, path, diffConfig, matchers)
      case (_, _) =>
        if ((expected.isInstanceOf[JObject] && !actual.isInstanceOf[JObject]) ||
          (expected.isInstanceOf[JArray] && !actual.isInstanceOf[JArray])) {
          List(BodyMismatch(expected, actual, Some(s"Type mismatch: Expected ${typeOf(expected)} ${valueOf(expected)} but received ${typeOf(actual)} ${valueOf(actual)}"), path))
        } else {
          compareValues(path, expected, actual, matchers)
        }
    }
  }

  def compareLists(expectedValues: List[Any], actualValues: List[Any], a: Any, b: Any, path: String,
                   diffConfig: DiffConfig, matchers: Option[Map[String, Map[String, String]]]): List[BodyMismatch] = {
    def compareListContent = {
      var result = List[BodyMismatch]()
      for ((value, index) <- expectedValues.view.zipWithIndex) {
        val s = path + "." + (index + 1)
        if (index < actualValues.size) {
          result = result ++: compare(s, value, actualValues(index), diffConfig, matchers)
        } else {
          result = result :+ BodyMismatch(a, b, Some(s"Expected ${valueOf(value)} but was missing"), path)
        }
      }
      result
    }

    var result = List[BodyMismatch]()
    if (Matchers.matcherDefined(path, matchers)) {
      result = Matchers.domatch[BodyMismatch](matchers.get(path), path, expectedValues, actualValues,
        BodyMismatchFactory) ++ compareListContent
    } else {
      if (expectedValues.isEmpty && actualValues.nonEmpty) {
        result = List(BodyMismatch(a, b, Some(s"Expected an empty List but received ${valueOf(actualValues)}"), path))
      } else {
        if (expectedValues.size != actualValues.size) {
          result = result :+ BodyMismatch(a, b, Some(s"Expected a List with ${expectedValues.size} elements but received ${actualValues.size} elements"), path)
        }
        result = result ++ compareListContent
      }
    }
    result
  }

  def compareMaps(expectedValues: Map[String, Any], actualValues: Map[String, Any], a: Any, b: Any, path: String,
                  diffConfig: DiffConfig, matchers: Option[Map[String, Map[String, String]]]): List[BodyMismatch] = {
    if (expectedValues.isEmpty && actualValues.nonEmpty) {
      List(BodyMismatch(a, b, Some(s"Expected an empty Map but received ${valueOf(actualValues)}"), path))
    } else {
      var result = List[BodyMismatch]()
      if ((diffConfig.allowUnexpectedKeys && expectedValues.size > actualValues.size) ||
        (!diffConfig.allowUnexpectedKeys && expectedValues.size != actualValues.size)) {
        result = result :+ BodyMismatch(a, b, Some(s"Expected a Map with at least ${expectedValues.size} elements but received ${actualValues.size} elements"), path)
      }
      expectedValues.foreach(entry => {
        val s = path + "." + entry._1
        if (actualValues.contains(entry._1)) {
          result = result ++: compare(s, entry._2, actualValues(entry._1), diffConfig, matchers)
        } else {
          result = result :+ BodyMismatch(a, b, Some(s"Expected ${entry._1}=${valueOf(entry._2)} but was missing"), path)
        }
      })
      result
    }
  }

  def compareValues(path: String, expected: Any, actual: Any, matchers: Option[Map[String, Map[String, String]]]): List[BodyMismatch] = {
    if (Matchers.matcherDefined(path, matchers)) {
      Matchers.domatch[BodyMismatch](matchers.get(path), path, expected, actual, BodyMismatchFactory)
    } else {
      if (expected == actual) {
        List[BodyMismatch]()
      } else {
        List(BodyMismatch(expected, actual, Some(s"Expected ${valueOf(expected)} but received ${valueOf(actual)}"), path))
      }
    }
  }*/

}
