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

import org.scalatest.FunSuite 
import org.scalatest.matchers.ShouldMatchers

import net.liftweb.json.JsonParser._
import net.liftweb.json.JsonAST._

class SimpleSchemaValidationTests extends FunSuite with ShouldMatchers {

  test("integer validation works") {
    val o = Orderly("integer;")

    o.validate(JInt(30)).size should equal (0)

    o.validate(JString("foo")).size should equal (1)
    o.validate(JDouble(34.3)).size should equal (1)
    o.validate(JBool(true)).size should equal (1)
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(1)
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(1)
  }

  test("string validation works") {
    val o = Orderly("string;")

    o.validate(JString("foo")).size should equal (0)

    o.validate(JInt(30)).size should equal (1)
    o.validate(JDouble(34.3)).size should equal (1)
    o.validate(JBool(true)).size should equal (1)
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(1)
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(1)
  }

  test("number validation works") {
    val o = Orderly("number;")

    o.validate(JDouble(34.3)).size should equal (0)

    o.validate(JInt(30)).size should equal (1)
    o.validate(JString("foo")).size should equal (1)
    o.validate(JBool(true)).size should equal (1)
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(1)
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(1)
  }

  test("boolean validation works") {
    val o = Orderly("boolean;")

    o.validate(JBool(true)).size should equal (0)

    o.validate(JInt(30)).size should equal (1)
    o.validate(JString("foo")).size should equal (1)
    o.validate(JDouble(34.3)).size should equal (1)
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(1)
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(1)
  }

  test("object validation works") {
    val o = Orderly("object { string foo; };")
    
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(0)

    o.validate(JInt(30)).size should equal (1)
    o.validate(JString("foo")).size should equal (1)
    o.validate(JDouble(34.3)).size should equal (1)
    o.validate(JBool(true)).size should equal (1)
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(1)
  }

  test("array validation works") {
    val o = Orderly("""array [ string ];""")
    
    o.validate(parse("""[ "foo", "bar" ]""")).size should equal(0)

    o.validate(JInt(30)).size should equal (1)
    o.validate(JString("foo")).size should equal (1)
    o.validate(JDouble(34.3)).size should equal (1)
    o.validate(JBool(true)).size should equal (1)
    o.validate(parse("""{ "foo": "bar" }""")).size should equal(1)
  }
}
