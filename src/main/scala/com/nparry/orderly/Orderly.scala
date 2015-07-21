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

import net.liftweb.json.JsonAST.{JObject, JValue}

object Orderly {
  def apply(s: String): Orderly = new Orderly(OrderlyParser.parse(s))
  def apply(f: java.io.File): Orderly = new Orderly(OrderlyParser.parse(f))
}

/**
 * Easy Java access to Orderly
 */
class DefaultOrderlyFactory() extends com.nparry.orderly.api.Orderly.Factory {

  override def getOrderly(orderly: String): com.nparry.orderly.api.Orderly = Orderly(orderly)
  override def getOrderly(orderly: java.io.File): com.nparry.orderly.api.Orderly = Orderly(orderly)
}

/**
 * The Scala front door to Orderly
 */
class Orderly(schema: JObject) extends com.nparry.orderly.api.Orderly {

  /**
   * Validate the given JSON against this Orderly schema
   */
  def validate(value: JValue): List[Violation] = {
    JsonSchemaValidator.validate(value, Some(schema))
  }

  /**
   * Parse and validate the given JSON against this Orderly schema
   */
  def validate(s: String): List[Violation] = validate(Json.parse(s))

  /**
   * Parse and validate the given JSON against this Orderly schema
   */
  def validate(f: java.io.File): List[Violation] = validate(Json.parse(f))

  // Java view of the world

  override def getViolations(s: String): java.util.List[com.nparry.orderly.api.Violation] = toJavaSpeak(validate(s))
  override def getViolations(f: java.io.File): java.util.List[com.nparry.orderly.api.Violation] = toJavaSpeak(validate(f))

  def toJavaSpeak(l:List[Violation]): java.util.List[com.nparry.orderly.api.Violation] =
    java.util.Arrays.asList(l.toArray: _*)

  override def toString() = Json.prettyPrint(schema)
}

