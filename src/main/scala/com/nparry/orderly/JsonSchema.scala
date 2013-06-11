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

import net.liftweb.json.JsonAST._
import net.liftweb.json.Implicits._

/**
 * A problem detected by the validator for the given input
 */
case class Violation(path: List[String], message: String) extends com.nparry.orderly.api.Violation {
  override def getDescription(): String = message
}

/**
 * Internal errors in our schema
 */
class SchemaProblem(msg: String) extends Exception(msg) { }

/**
 * JSON schema validation, ported from the Dojo implementation.
 * This is woefully incomplete at the moment.
 * 
 * Some hacky bits...
 * - Something typed as 'number' might be represented as a
 *   JInt.  This makes the min/max check ugly for this case.
 */
object JsonSchemaValidator {

  /**
   * Call this with an instance object and an optional schema object.
   * If a schema is provided, it will be used to validate. If the instance
   * object refers to a schema (self-validating), that schema will be used
   * to validate and the schema parameter is not necessary (if both exist, 
   * both validations will occur).
   */
  def validate(instance: JValue, schema: Option[JObject]): List[Violation] = {
    validate(instance, schema, None)
  }

  /**
   * The checkPropertyChange method will check to see if a value can legally be
   * in property with the given schema. This is slightly different than the
   * validate method in that it will fail if the schema is readonly and it will
   * not check for self-validation, it is assumed that the passed in value is
   * already internally valid.
   */
  def checkPropertyChange(instance: JValue, schema: JObject, property: Option[String]): List[Violation] = {
    validate(instance, Some(schema), property)
  }

  def validate(instance: JValue, schema: Option[JObject], changing: Option[String]): List[Violation] = {

    def asObject(x: JValue) = x match {
      case o @ JObject(_) => o
      case _ => throw new SchemaProblem("expected to get an object from the schema")
    }

    // validate a value against a property definition
    def checkProp(instance: JValue, schema: JObject, incomingPath: List[String], i: String): List[Violation] = {
      val path = i :: incomingPath

      def ok(): List[Violation] = List()
      def violation(msg: String): List[Violation] = List(Violation(path, msg))
      def schemaProblem(msg: String): Nothing = throw new SchemaProblem(msg)

      // A bunch of helper functions
      // TODO: Find some better way to create these with less duplicated code

      def value(name: String): JValue = (schema \ name)

      def bool(name: String, dflt: Boolean): Boolean = {
        value(name) match {
          case JNothing => dflt
          case JBool(b) => b
          case _ => schemaProblem("expected a boolean named " + name)
        }
      }

      def int(name: String, dflt: BigInt): BigInt = {
        value(name) match {
          case JNothing => dflt
          case JInt(x) => x
          case _ => schemaProblem("expected an integer named " + name)
        }
      }

      def dub(name: String, dflt: Double): Double = {
        value(name) match {
          case JNothing => dflt
          case JDouble(d) => d
          case JInt(i) => i.doubleValue()
          case _ => schemaProblem("expected a double named " + name)
        }
      }

      def str(name: String): Option[String] = {
        value(name) match {
          case JNothing => None
          case JString(s) => Some(s)
          case _ => schemaProblem("expected a string named " + name)
        }
      }

      def obj(name: String, f: JObject => List[Violation], defaultOpt: Option[JObject] = None): Option[List[Violation]] = {
        (value(name), defaultOpt) match {
          case (JNothing, None) => None
          case (JNothing, Some(default)) => Some(f(default))
          case (o @ JObject(_), _) => Some(f(o))
          case _ => schemaProblem("expected an object named '" + name + "'")
        }
      }

      def arr(name: String, f: List[JValue] => List[Violation]): Option[List[Violation]] = {
        value(name) match {
          case JNothing => None
          case a @ JArray(arr) => Some(f(arr))
          case _ => schemaProblem("expected an array named '" + name + "'")
        }
      }

      // validate a value against a type definition
      def checkType(typeDfn: JValue, v: JValue): List[Violation] = {
        def matchOrError(classes: List[Class[_]]): List[Violation] = {
          if (classes.contains(v.getClass())) ok()
          else violation(v.getClass() + " value found, but one of " + classes + " is required")
        }

        typeDfn match {
          case JNothing => ok()
          case JString(s) => s match {
            case "any" => ok()
            case "null" => matchOrError(List(JNull.getClass()))
            case "string" => matchOrError(List(classOf[JString]))
            case "object" => matchOrError(List(classOf[JObject]))
            case "array" => matchOrError(List(classOf[JArray]))
            case "boolean" => matchOrError(List(classOf[JBool]))
            case "number" => matchOrError(List(classOf[JDouble], classOf[JInt]))
            case "integer" => matchOrError(List(classOf[JInt]))
          }
          case JArray(arr) => {
            var unionErrors = arr map { unionType => checkType(unionType, v) }
            unionErrors exists { e => e.isEmpty } match {
              case true => ok()
              case false => unionErrors flatMap { e => e }
            }
          }
          case o @ JObject(_) => checkProp(v, o, path, i)
          case _ => schemaProblem("unknown type definition in schema")
        }
      }

      /* TODO: Figure out what this is trying to do
      if ((typeof schema != 'object' || schema instanceof Array) && (path || typeof schema != 'function')) {
        if (typeof schema == 'function') {
          if (! (Object(value) instanceof schema)) {
            addError("is not an instance of the class/constructor " + schema.name);
          }
        } else if (schema) {
          addError("Invalid schema/property definition " + schema);
        }
        return null;
      }
      */

      (changing match {
        case None =>
          ok()
        case Some(_) =>
          if (bool("readonly", false)) violation("is a readonly field, it can not be changed") else ok()
      }) ++
      (obj("extends", { ext => checkProp(instance, ext, path, i) }) getOrElse ok()) ++
      (instance match {
        case JNothing =>
          if (bool("optional", false)) ok() else violation("missing and is not optional")
        case JNull =>
          checkType(value("type"), instance)
        case _ =>
          checkType(value("type"), instance) ++
          (obj("disallow", { d => if (checkType(d, instance).isEmpty) violation("disallowed value was matched") else ok() }) getOrElse ok()) ++
          (arr("enum", { a => if (!a.contains(instance)) violation("does not match any enum value") else ok() }) getOrElse ok()) ++
          (instance match {
            case JArray(arr) =>
              (value("items") match {
                case JNothing => ok()
                case JArray(items) => {
                  (if (arr.length < items.length) violation("fewer elements than specified in the schema")
                  else ok()) ++
                  (if ((arr.length > items.length) && !bool("additionalProperties", true)) violation("more elements than specified in the schema")
                  else ok()) ++
                  ((arr zip items) flatMap { pair => checkProp(pair._1, asObject(pair._2), path, i) })
                }
                case o @ JObject(_) => arr flatMap { v => checkProp(v, o, path, i) }
                case _ => schemaProblem("invalid 'items' element in schema")
              }) ++
              (if (BigInt(arr.length) < int("minItems", arr.length)) violation("minimum item count not met") else ok()) ++
              (if (BigInt(arr.length) > int("maxItems", arr.length)) violation("maximum item count exceeded") else ok())
            case o @ JObject(_) => { 
              val defaultOpt = value("type") match { 
                case JString("array") => None
                case _ => Some(JObject(List()))
              }
              (obj("properties", { props => checkObj(o, props, path, bool("additionalProperties", true)) }, defaultOpt) getOrElse ok())
            }
            case JString(s) =>
              str("pattern").map { regex => {
                if(s.matches(regex)) ok()
                else violation(s + " does not match " + regex)
              }}.getOrElse(ok()) ++
              str("format").map { format => {
                if(s.equals(format)) ok()
                else violation(s + " does not match " + format)
              }}.getOrElse(ok()) ++
              (if (BigInt(s.length) < int("minLength", s.length)) violation(s + " is too short") else ok()) ++
              (if (BigInt(s.length) > int("maxLength", s.length)) violation(s + " is too long") else ok())
            case JInt(i) =>
              (if (i.doubleValue() < dub("minimum", i.doubleValue())) violation(i + " is less than minimum allowed value") else ok()) ++
              (if (i.doubleValue() > dub("maximum", i.doubleValue())) violation(i + " is greater than maximum allowed value") else ok())
            case JDouble(d) =>
              (if (d < dub("minimum", d)) violation(d + " is less than minimum allowed value") else ok()) ++
              (if (d > dub("maximum", d)) violation(d + " is greater than maximum allowed value") else ok()) ++
              (if ((int("maxDecimal", -1) > -1) &&
                   (d.toString.indexOf(".") != -1) &&
                   (int("maxDecimal", -1) < d.toString.substring(d.toString.indexOf(".")).length))
                  violation(d + " has too many decimal places") else ok())
            case _ => ok()
          })
      })
    }

    // validate an object against a schema
    def checkObj(instance: JObject, objTypeDef: JObject, path: List[String], additionalProp: Boolean): List[Violation] = {
      def ok(): List[Violation] = List()
      def violation(msg: String): List[Violation] = List(Violation(path, msg))

      def value(obj: JObject, name: String): JValue = (obj \ name)

      def instanceHas(name: String): Boolean = {
        instance \ name match {
          case JNothing => false
          case _ => true
        }
      }

      def fieldsFor(obj: JObject) = obj match {
        // This seems like a dumb way to do this, I need to improve my scala-fu
        case JObject(flds) => flds
      }

      (fieldsFor(objTypeDef) flatMap { fld: JField =>
        checkProp(value(instance, fld.name), asObject(fld.value), path, fld.name)
      }) ++
      (if (additionalProp) ok()
        else fieldsFor(instance) flatMap { fld: JField =>
          objTypeDef \ fld.name match {
            case JNothing => violation("property " + fld.name + " is not defined in the schema")
            case _ => ok()
          }
      }) ++
      (fieldsFor(instance) flatMap { fld: JField =>
        objTypeDef \ fld.name \ "requires" match {
          case JNothing =>
            ok()
          case JString(s) =>
            if (instanceHas(s)) ok() else violation("presence of " + fld.name + " requires " + s + " also be present")
          case JArray(arr) =>
            arr flatMap { elem => elem match {
              case JString(s) => if (instanceHas(s)) ok() else violation("presence of " + fld.name + " requires " + s + " also be present")
              case _ => throw new SchemaProblem("'requires' should only contain strings")
            }}
          case _ => throw new SchemaProblem("invalid value for 'requires'")
        }
      })

      /* TODO
      for (i in instance) {
        value = instance[i];
        if (objTypeDef && typeof objTypeDef == 'object' && !(i in objTypeDef)) {
          checkProp(value, additionalProp, path, i);
        }
        if (!_changing && value && value.$schema) {
          errors = errors.concat(checkProp(value, value.$schema, path, i));
        }
      }
      */
    }

    (schema match {
      case None => List()
      case Some(s) => checkProp(instance, s, Nil, changing getOrElse "")
    }) ++
    (changing match {
      case Some(_) => List()
      case None => (instance \ "$schema" match {
        case JNothing => List()
        case o @ JObject(_) => checkProp(instance, o, Nil, "")
        case _ => List(Violation(Nil, "$schema is not an object"))
      })
    })
  }
}

