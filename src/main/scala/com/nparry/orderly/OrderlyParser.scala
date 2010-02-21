package com.nparry.orderly

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._
import scala.util.parsing.input.{Reader,StreamReader,CharArrayReader}
import scala.util.parsing.json
import net.liftweb.json.JsonAST._
import net.liftweb.json.Implicits._
import java.io.{InputStream, InputStreamReader}

object OrderlyParser extends JavaTokenParsers {
  type Tokens = json.Lexer
  //val lexical = new Tokens
        
  //lexical.reserved ++= List("true", "false", "null")
  //lexical.delimiters ++= List("{", "}", "[", "]", ":", ",")

  def t(name: String) = f("type", name)
  def f(k: String, v: JValue) = JField(k, v)
  def l [A] (x: Option[List[A]]): List[A] = x getOrElse List()

  def orderlySchema: Parser[JObject] = unnamedEntry <~ ";"
  def namedEntries: Parser[JObject]   = repsep(namedEntry, ";") ^^ (JObject(_))
  def unnamedEntries: Parser[JArray] = repsep(unnamedEntry, ";") ^^ (JArray(_))
  def namedEntry: Parser[JField] =
    (definitionPrefix ~ propertyName ~ definitionSuffix) ^^ 
      { case p ~ n ~ s => f(n.values, JObject(p ++ s)) } |
    (stringPrefix ~ propertyName ~ stringSuffix) ^^ 
      { case p ~ n ~ s => f(n.values, JObject(p ++ s)) }
  def unnamedEntry: Parser[JObject] =
    (definitionPrefix ~ definitionSuffix) ^^ { case p ~ s => JObject(p ++ s) } |
    (stringPrefix ~ stringSuffix) ^^ { case p ~ s => JObject(p ++ s) }
  def definitionPrefix: Parser[List[JField]] =
    "boolean" ^^^ (List(t("boolean"))) | 
    "null"    ^^^ (List(t("null"))) |
    "any"     ^^^ (List(t("any"))) |
    ("integer" ~> opt(range("minimum", "maximum"))) ^^ { r => t("integer") :: l(r) } |
    ("number"  ~> opt(range("minimum", "maximum"))) ^^ { r => t("number") :: l(r) } |
    ("array" ~> "{" ~> unnamedEntries <~ "}") ~ opt(additionalMarker) ~ opt(range("minItems", "maxItems")) ^^
      { case e ~ m ~ r =>  t("array") :: f("items", e) :: (l(r) ++ l(m)) } |
    ("array" ~> "[" ~> unnamedEntry <~ "]") ~ opt(range("minItems", "maxItems")) ^^
      { case e ~ r => t("array") :: f("items", e) :: l(r) } |
    ("object" ~> "{" ~> namedEntries <~ "}") ~ opt(additionalMarker)  ^^
      { case e ~ m => t("object") :: f("properties", e) :: l(m) } 
    ("union" ~> "{" ~> unnamedEntries <~ "}") ^^ { case e => List(f("type", e)) }
  def stringPrefix: Parser[List[JField]] = "string" ~> opt(range("minLength", "maxLength")) ^^
    { case r => t("string") :: l(r) }
  def stringSuffix: Parser[List[JField]] = opt(perlRegex) ~ definitionSuffix ^^
    { case r ~ s => l(r) ++ s }
  def definitionSuffix: Parser[List[JField]] =
    opt(enumValues) ~ opt(defaultValue) ~ opt(rqires) ~ opt(optionalMarker) ~ opt(extraProperties) ^^
    { case e ~ d ~ r ~ m ~ x => l(e) ++ l(d) ++ l(r) ++ l(m) ++ l(x) }
  def extraProperties: Parser[List[JField]] = "`" ~> jsonObj <~ "`" ^^
    { case JObject(l) => l }
  def rqires: Parser[List[JField]] = "<" ~> repsep(propertyName, ",") <~ ">" ^^
    { n => List(f("requires", JArray(n))) }
  def optionalMarker: Parser[List[JField]] = "?" ^^^ List(f("optional", true))
  def additionalMarker: Parser[List[JField]] = "*" ^^^ List(f("additionalProperties", true))
  def enumValues: Parser[List[JField]] = jsonArray ^^
    { case a => List(f("enum", a)) }
  def defaultValue: Parser[List[JField]] = "=" ~> jsonValue ^^
    { case d => List(f("default", d)) }
  def range(l:String, h:String): Parser[List[JField]] =
    ("{" ~> floatingPointNumber ~ "," ~ floatingPointNumber <~ "}") ^^
      { case min ~ "," ~ max => List(f(l, min), f(h, max)) } |
    ("{" ~> floatingPointNumber <~ "," <~ "}")  ^^
      { case min => List(f(l, min)) } | 
    ("{" ~> "," ~> floatingPointNumber <~ "}") ^^
      { case max => List(f(h, max)) } |
    ("{" ~ "," ~ "}") ^^^ List()
  def propertyName: Parser[JString] =
    stringLiteral ^^ { JString(_) } |
    "[A-Za-z_-]+".r ^^ { JString(_) }
  def perlRegex: Parser[List[JField]] = ("/" ~> "[^/]".r <~ "/") ^^
    { case r => List(f("pattern", r)) }

  def jsonObj: Parser[JObject] = "{" ~> repsep(jsonMember, ",") <~ "}" ^^ { JObject(_) }
  def jsonMember = stringLiteral ~ ":" ~ jsonValue ^^ { case k ~ ":" ~ v => f(k, v) }
  def jsonArray = "[" ~> repsep(jsonValue, ",") <~ "]" ^^ { JArray(_) }
  def jsonValue: Parser[JValue] =
    jsonObj |
    jsonArray |
    stringLiteral ^^ { JString(_) } |
    floatingPointNumber ^^ { x => JDouble(x.toDouble) } |
    "null" ^^^ JNull |
    "true" ^^^ JBool(true) |
    "false" ^^^ JBool(false)
}

