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
package com.nparry.orderly.api;

import java.io.File;
import java.util.List;

/**
 * Java view of the Orderly world.
 */
public interface Orderly {

  /**
   * Parse the string and check it against this Orderly instance.
   *
   * @param json Input to validate.
   * @return A list of problems found.
   */
  List<Violation> getViolations(String json);

  /**
   * Parse the file and check it against this Orderly instance.
   *
   * @param json Input to validate.
   * @return A list of problems found.
   */
  List<Violation> getViolations(File json);

  /**
   * A source for Orderly instances.
   */
  public interface Factory {

    /**
     * Parse a string of Orderly.
     *
     * @param orderly Orderly input to parse.
     * @return Orderly instance
     */
    Orderly getOrderly(String orderly);

    /**
     * Parse a file of Orderly.
     *
     * @param orderly Orderly input to parse.
     * @return Orderly instance
     */
    Orderly getOrderly(File orderly);
  }

}

