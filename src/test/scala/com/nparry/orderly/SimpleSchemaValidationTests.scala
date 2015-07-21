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

import org.specs2.mutable._

import net.liftweb.json.JsonAST._

class SimpleSchemaValidationTests extends Specification {

  "Schema validation" should {
    "do integer validation" in {
      val o = Orderly("integer;")

      o.validate(JInt(30)).size mustEqual 0

      o.validate(JString("foo")).size mustEqual 1
      o.validate(JDouble(34.3)).size mustEqual 1
      o.validate(JBool(true)).size mustEqual 1
      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 1
      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 1
    }

    "do string validation" in {
      val o = Orderly("string;")

      o.validate(JString("foo")).size mustEqual (0)

      o.validate(JInt(30)).size mustEqual 1
      o.validate(JDouble(34.3)).size mustEqual 1
      o.validate(JBool(true)).size mustEqual 1
      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 1
      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 1
    }

    "do string validation with strings containing newlines" in {
      val o = Orderly("string;")
      o.validate(JString("foo\n")).size mustEqual (0)
    }

    "do number validation" in {
      val o = Orderly("number;")

      o.validate(JDouble(34.3)).size mustEqual 0

      // We say an int is a number - this is probably
      // the right thing to do
      o.validate(JInt(30)).size mustEqual 0

      o.validate(JString("foo")).size mustEqual 1
      o.validate(JBool(true)).size mustEqual 1
      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 1
      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 1
    }

    "do boolean validation" in {
      val o = Orderly("boolean;")

      o.validate(JBool(true)).size mustEqual 0

      o.validate(JInt(30)).size mustEqual 1
      o.validate(JString("foo")).size mustEqual 1
      o.validate(JDouble(34.3)).size mustEqual 1
      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 1
      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 1
    }

    "do object validation" in {
      val o = Orderly("object { string foo; };")

      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 0

      o.validate(JInt(30)).size mustEqual 1
      o.validate(JString("foo")).size mustEqual 1
      o.validate(JDouble(34.3)).size mustEqual 1
      o.validate(JBool(true)).size mustEqual 1
      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 1
    }

    "do array validation" in {
      val o = Orderly("""array [ string ];""")

      o.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 0

      o.validate(JInt(30)).size mustEqual 1
      o.validate(JString("foo")).size mustEqual 1
      o.validate(JDouble(34.3)).size mustEqual 1
      o.validate(JBool(true)).size mustEqual 1
      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 1
    }

    "do empty object validation" in {
      val o = Orderly("object {};")

      o.validate(Json.parse("""{ "foo": 1 }""")).size mustEqual 1
      o.validate(JObject(List[JField]())).size mustEqual 0
    }

    "do object union validation" in {
      val o = Orderly("union { object { string foo; }; object {}; }")

      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 0
      o.validate(Json.parse("""{ "foo": 1 }""")).size mustEqual 2

      o.validate(JObject(List[JField]())).size mustEqual 0
    }

    "do simple comment validation for null" in {

      val orderly = Orderly("null; #comment")
      orderly.validate(JNull).size mustEqual 0
    }

    "do simple comment validation for boolean" in {
      val orderly = Orderly("boolean; #comment")
      orderly.validate(JBool(true)).size mustEqual 0
    }

    "do simple comment validation for integer" in {

      val orderly = Orderly("integer; #comment")
      orderly.validate(JInt(42)).size mustEqual 0
    }

    "do simple comment validation for string" in {
      val orderly = Orderly("string; #comment")
      orderly.validate(JString("foo")).size mustEqual 0
    }

    "do simple comment validation for number" in {
      val orderly = Orderly("number; #comment")
      orderly.validate(JInt(42)).size mustEqual 0
      orderly.validate(JDouble(42.0)).size mustEqual 0
    }


    "do simple comment validation for object" in {
      val orderly = Orderly("object { string foo; }; # comment")
      orderly.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 0
    }

    "do simple comment validation for array" in {
      val orderly = Orderly("""array [ string ]; # comment""")
      orderly.validate(Json.parse("""[ "foo", "bar" ]""")).size mustEqual 0
    }

    "do simple comment validation for object" in {
      val orderly = Orderly("object {}; # comment")
      orderly.validate(Json.parse("{}")).size mustEqual 0
    }

    "do multiple comments validation" in {

      val o = Orderly("""
      | # comment 1
      | object { # in-line comment
      | # comment 2
      | # comment 3
      | string foo; #comment 4
      | string bar?; #comment 5
      | \\ comment 6
      | \\ comment 7
      | integer baz?; ### comment8
      | # comment 9
      | }; # comment 10
      | # comment 11
      """.stripMargin)

      o.validate(Json.parse("""{ "foo": "bar" }""")).size mustEqual 0
      o.validate(Json.parse("""{ "foo": "bar", "baz": 1 }""")).size mustEqual 0
    }
  }
}

