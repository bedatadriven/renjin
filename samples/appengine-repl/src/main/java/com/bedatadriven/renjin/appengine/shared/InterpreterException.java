/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.bedatadriven.renjin.appengine.shared;

/**
 * An exception thrown by an interpreter. This message contains information
 * useful to the user to determine what went wrong in their script.
 * 
 */
public class InterpreterException extends Exception {
  private static final long serialVersionUID = 1L;

  // Required for GWT serialization
  InterpreterException() {
  }

  public InterpreterException(String msg) {
    super(msg);
  }
}
