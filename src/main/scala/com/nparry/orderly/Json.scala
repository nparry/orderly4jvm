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
import java.io._

/**
 * Some Json utilities
 */
object Json {

  def prettyPrint(f: File): String = prettyPrint(parse(f))
  def prettyPrint(s: String): String = prettyPrint(parse(s))
  def prettyPrint(json: JValue): String = net.liftweb.json.Printer.pretty(render(json))

  def parse(f: File): JValue = parse(new FileReader(f))
  def parse(r: Reader): JValue = try { parse(readerToString(r)) } finally { r.close() }

  def parse(s: String): JValue = {
    var t = s.trim()
    if (isGiantHackNecessary(t)) applyGiantHack(t)
    else parseString(t)
  }

  private def parseString(s: String): JValue = {
    net.liftweb.json.JsonParser.parse(munge(s))
  }

  private def isGiantHackNecessary(s: String): Boolean = {
    !(s.startsWith("{") || s.startsWith("["))
  }

  private def applyGiantHack(s: String): JValue = {
    parseString("[" + munge(s) + "]")(0)
  }

  private def munge(s: String): String = {
    if (s.endsWith(";")) s.substring(0, s.length() - 1) else s
  }

  private def readerToString(r: Reader): String = {
    val sb = new StringBuilder()
    val buf = new BufferedReader(r)
    var done = false

    while (!done) {
      done = buf.readLine() match {
        case null => true
        case s => { sb.append(s).append("\n"); false }
      }
    }

    sb.toString()
  }
}

