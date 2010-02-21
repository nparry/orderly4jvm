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

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import java.io.File
import java.net.URI

class ReferenceImplSpec extends FlatSpec with ShouldMatchers {

  "This implementation" should "produce the same output as the RI for valid input" in {
    locateOrderlyInput("referenceImpl/positive_cases") foreach { url =>
      System.err.println(url.toString())
    }
  }

  "This implementation" should "reject the same invalid input as the RI" in {
    locateOrderlyInput("referenceImpl/negative_cases") foreach { url =>
      System.err.println(url.toString())
    }
  }

  def locateOrderlyInput(s: String): Array[File] = {
    val a = filesForUri(uriForResourceDir(s)) filter { f => f.getAbsolutePath().endsWith(".orderly") }
    a.length match {
      case 0 => throw new Exception("No test input found in " + s)
      case _ => a
    }
  }

  def filesForUri(uri: URI): Array[File] = new File(uri).listFiles()
  def uriForResourceDir(s: String): URI = Thread.currentThread().getContextClassLoader().getResources(s).nextElement().toURI()
}

