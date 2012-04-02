/*
 *  Copyright (c) 2010, Nathan Parry
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 * 
 *  3. Neither the name of Nathan Parry nor the names of any
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 *  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *  STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *  IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package com.nparry.orderly

import scala.util.parsing.combinator._
import scala.util.parsing.combinator.syntactical._
import scala.util.parsing.combinator.lexical._

import net.liftweb.json.JsonAST._
import net.liftweb.json.Implicits._
import util.parsing.input.{CharSequenceReader, Reader, StreamReader}

/**
 * An implementation of Orderly JSON (http://orderly-json.org/).
 * This parser produces a JSON object representing the JSON
 * schema of the given Orderly input.
 *
 * This is based on the Orderly grammar at:
 * http://github.com/lloyd/orderly/blob/master/docs.md
 */
object OrderlyParser extends JavaTokenParsers {

  /**
   * Parse the given string and return a JObject of the resuling schema
   */
  def parse(s: String): JObject = parse(new CharSequenceReader(s))

  /**
   * Parse the given file and return a JObject of the resuling schema
   */
  def parse(f: java.io.File): JObject = {
    val r = new java.io.FileReader(f)
    try { parse(StreamReader(r)) } finally { r.close() }
  }

  /**
   * Parse the given reader and return a JObject of the resuling schema
   */
  def parse(input: Reader[Char]): JObject  = try {
    phrase(orderlySchema)(input) match {
      case Success(result, _) => result
      case Failure(msg, _) => throw new InvalidOrderly(msg)
      case Error(msg, _) => throw new InvalidOrderly(msg)
    }
   } catch {
     case e:NumberFormatException => throw new InvalidOrderly(e.getMessage())
     case e:java.util.regex.PatternSyntaxException => throw new InvalidOrderly(e.getMessage())
   }


  // Some helpers to shorten the code below

  def t(name: String) = f("type", name)
  def f(k: String, v: JValue) = JField(k, v)
  def fl(k: String, v: JObject) = v.values.size match {
    case 0 => List()
    case _ => List(f(k, v))
  }

  def l [A] (x: Option[List[A]]): List[A] = l(x, List())
  def l [A] (x: Option[List[A]], y: List[A]): List[A] = x getOrElse y
  def asInt(s: String): JValue = JInt(s.toInt)
  def asDub(s: String): JValue = try {
    asInt(s)
  } catch {
    case e:NumberFormatException => JDouble(s.toDouble)
    case e:Exception => throw e
  }

  // The orderly grammar

  lazy val orderlySchema: Parser[JObject] = unnamedEntry <~ opt(";")
  lazy val namedEntries: Parser[JObject] =
    (rep1sep(namedEntry, ";") <~ opt(";")) ^^ { case x => JObject(x) } |
    success(JObject(List()))
  def unnamedEntries(min: Option[Int]): Parser[JArray] =
    if (min.isDefined)
      repN(min.get - 1, unnamedEntry <~ ";") ~ unnamedEntry ~ opt(";" ~> unnamedEntries(None)) ^^
        { case f ~ m ~ Some(JArray(e)) =>  JArray(f ++ List(m) ++ e)
          case f ~ m ~ None => JArray(f ++ List(m)) }
    else
      (rep1sep(unnamedEntry, ";") <~ opt(";")) ^^ { case x => JArray(x) } |
      success(JArray(List()))
  lazy val namedEntry: Parser[JField] =
    (definitionPrefix ~ propertyName ~ definitionSuffix) ^^ 
      { case p ~ n ~ s => f(n.values, JObject(p ++ s)) } |
    (stringPrefix ~ propertyName ~ stringSuffix) ^^ 
      { case p ~ n ~ s => f(n.values, JObject(p ++ s)) }
  lazy val unnamedEntry: Parser[JObject] =
    (definitionPrefix ~ definitionSuffix) ^^ { case p ~ s => JObject(p ++ s) } |
    (stringPrefix ~ stringSuffix) ^^ { case p ~ s => JObject(p ++ s) }
  lazy val definitionPrefix: Parser[List[JField]] =
    "boolean" ^^^ (List(t("boolean"))) | 
    "null"    ^^^ (List(t("null"))) |
    "any"     ^^^ (List(t("any"))) |
    ("integer" ~> opt(range("minimum", "maximum"))) ^^ { r => t("integer") :: l(r) } |
    ("number"  ~> opt(range("minimum", "maximum"))) ^^ { r => t("number") :: l(r) } |
    ("array" ~> "{" ~> unnamedEntries(None) <~ "}") ~ additionalMarker ~ opt(range("minItems", "maxItems")) ^^
      { case e ~ m ~ r =>  t("array") :: f("items", e) :: (l(r) ++ m) } |
    ("array" ~> "[" ~> unnamedEntry <~ opt(";") <~ "]") ~ additionalMarker ~ opt(range("minItems", "maxItems")) ^^
      { case e ~ m ~ r => t("array") :: f("items", e) :: (l(r) ++ m) } |
    ("object" ~> "{" ~> namedEntries <~ "}") ~ additionalMarker  ^^
      { case e ~ m => t("object") :: (fl("properties", e) ++ m) } |
    ("union" ~> "{" ~> unnamedEntries(Some(2)) <~ "}") ^^ { case e => List(f("type", e)) }
  lazy val stringPrefix: Parser[List[JField]] = "string" ~> opt(range("minLength", "maxLength")) ^^
    { case r => t("string") :: l(r) }
  lazy val stringSuffix: Parser[List[JField]] = opt(perlRegex) ~ definitionSuffix ^^
    { case r ~ s => l(r) ++ s }
  lazy val definitionSuffix: Parser[List[JField]] =
    opt(enumValues) ~ opt(defaultValue) ~ opt(rqires) ~ opt(optionalMarker) ~ opt(extraProperties) ^^
    { case e ~ d ~ r ~ m ~ x => l(e) ++ l(d) ++ l(m) ++ l(r) ++ l(x) }
  lazy val extraProperties: Parser[List[JField]] = "`" ~> jsonObj <~ "`" ^^
    { case JObject(l) => l }
  lazy val rqires: Parser[List[JField]] = "<" ~> repsep(propertyName, ",") <~ ">" ^^
  { n => List(f("requires", n.size match { case 1 => n(0); case _ => JArray(n) })) }
  lazy val optionalMarker: Parser[List[JField]] = "?" ^^^ List(f("optional", true))
  lazy val additionalMarker: Parser[List[JField]] = opt("*") ^^ { m => if (m.isDefined) List() else List(f("additionalProperties", false)) }
  lazy val enumValues: Parser[List[JField]] = jsonArray ^^
    { case a => List(f("enum", a)) }
  lazy val defaultValue: Parser[List[JField]] = "=" ~> jsonValue ^^
    { case d => List(f("default", d)) }
  def range(l:String, h:String): Parser[List[JField]] =
    ("{" ~> jsonNum ~ "," ~ jsonNum <~ "}") ^^
      { case min ~ "," ~ max => List(f(l, min), f(h, max)) } |
    ("{" ~> jsonNum <~ "," <~ "}")  ^^
      { case min => List(f(l, min)) } | 
    ("{" ~> "," ~> jsonNum <~ "}") ^^
      { case max => List(f(h, max)) } |
    ("{" ~ "," ~ "}") ^^^ List()
  lazy val propertyName: Parser[JString] =
    ident ^^ { case s => JString(s) } |
    jsonStr
  lazy val perlRegex: Parser[List[JField]] = ("/" ~> regex("[^/]+".r) <~ "/") ^^
    { case r => List(f("pattern", java.util.regex.Pattern.compile(r).pattern)) }


  // Mini grammar to parse JSON

  lazy val jsonObj: Parser[JObject] = "{" ~> repsep(jsonMember, ",") <~ "}" ^^ { JObject(_) }
  lazy val jsonMember = jsonStr ~ ":" ~ jsonValue ^^ { case k ~ ":" ~ v => f(k.values, v) }
  lazy val jsonArray = ("[" ~> repsep(jsonValue, ",") <~ "]") ^^ { JArray(_) }
  lazy val jsonStr = stringLiteral ^^ { case s => JString(s.substring(1, s.length() - 1)) }
  lazy val jsonNum = floatingPointNumber ^^ { case n => asDub(n) }
  lazy val jsonValue: Parser[JValue] =
    jsonObj |
    jsonArray |
    jsonStr |
    jsonNum |
    "null" ^^^ JNull |
    "true" ^^^ JBool(true) |
    "false" ^^^ JBool(false)
}

/**
 * Signal invalid input
 */
class InvalidOrderly(msg: String) extends Exception(msg)

