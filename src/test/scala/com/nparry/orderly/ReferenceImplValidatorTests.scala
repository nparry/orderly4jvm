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

import java.io._
import java.net.URI

/**
 * Make sure our validation produces the same results as the RI.
 */
class ReferenceImplValidatorTests extends Specification {

  "Our validator" should {
    "pass the same JSON as the RI" in {
      processTests("pass", { (errors, orderlyInput, orderly, testInput) =>
        errors.isEmpty match {
          case true => 0
          case false => {
            System.err.println("\nOrderly from " + orderlyInput + ", schema is:")
            System.err.println(orderly.toString())
            System.err.println("Input failed to validate: " + testInput + ", json is:")
            System.err.println(Json.prettyPrint(testInput))
            System.err.println("Validation errors are:")
            System.err.println(errors)
            1
          }
        }
      }) mustEqual 0
    }

    "reject the same JSON as the RI" in {
      processTests("fail", { (errors, orderlyInput, orderly, testInput) =>
        errors.isEmpty match {
          case false => 0
          case true => {
            System.err.println("\nOrderly from " + orderlyInput + ", schema is:")
            System.err.println(orderly.toString())
            System.err.println("Input should have failed validation but did not: " + testInput + ", json is:")
            System.err.println(Json.prettyPrint(testInput))
            1
          }
        }
      }) mustEqual 0
    }
  }

  def processTests(testName: String, processor: (List[Violation], File, Orderly, File) => Int): Int = {
    ((locateOrderlyInput("referenceImplValidator") map {
      f=> (f, getValidatorInputs(f, testName)) }).foldLeft(0) { (errorCount, pair) =>
        val orderlyInput = pair._1
        val testCases = pair._2
        try {
          val orderly = makeOrderly(orderlyInput)
          errorCount + testCases.foldLeft(0) { (failCount, testInput) =>
            try {
              val errors = orderly.validate(testInput)
              failCount + processor(errors, orderlyInput, orderly, testInput)
            } catch {
              case e:SchemaProblem => {
                System.err.println("\nOrderly from " + orderlyInput + ", schema is:")
                System.err.println(orderly.toString())
                System.err.println("Validator encountered internal schema problem validating " + testInput + ", json is:")
                System.err.println(Json.prettyPrint(testInput))
                System.err.println("Errors is: " + e)
                failCount + 1
              }
              case e:Exception => {
                System.err.println("Fatal exception processing " + testInput)
                throw e
              }
            }
          }
        } catch {
          case e:InvalidOrderly => {
            System.err.println("\nOrderly from: " + orderlyInput)
            System.err.println("Parsing failed!")
            errorCount + testCases.size
          }
          case e:Exception => throw e
        }
      }
    ) 
  }

  def makeOrderly(f: File): Orderly = try {
    Orderly(f)
  } catch {
    case e:InvalidOrderly => try { 
      // Some of the test input is actually in json schema format
      import net.liftweb.json.JsonAST.JObject
      Json.parse(f) match {
        case o @ JObject(_) => new Orderly(o)
        case _ => throw e
      }
    } catch {
      // If the fallback fails, throw the original exception
      case _:Throwable => throw e
    }
  }

  def locateOrderlyInput(s: String): Array[File] = {
    val a = filesForUri(uriForResourceDir(s)) filter { f => f.getAbsolutePath().endsWith(".orderly") }
    a.length match {
      case 0 => throw new Exception("No test input found in " + s)
      case _ => a
    }
  }

  def getValidatorInputs(f: File, suffix: String): Array[File] = {
    val dir = new File(f.getAbsolutePath.replace(".orderly", "." + suffix))
    if (!dir.exists()) {
      Array()
    }
    else {
      val inputs = dir.listFiles() filter { i => i.getAbsolutePath().endsWith(".test") }
      inputs.length match {
        case 0 => throw new Exception("No test cases found in " + dir)
        case _ => inputs
      }
    }
  }

  def uriForResourceDir(s: String): URI = Thread.currentThread().getContextClassLoader().getResources(s).nextElement().toURI()
  def filesForUri(uri: URI): Array[File] = new File(uri).listFiles()
}

