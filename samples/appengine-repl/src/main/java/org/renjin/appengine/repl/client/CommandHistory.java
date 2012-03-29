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

package org.renjin.appengine.repl.client;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.renjin.appengine.repl.shared.InterpreterType;

/**
 * Per-language command history is stored on the client side. History is a
 * simple append-only list, not a stack or anything fancy.
 */
public class CommandHistory {
  private Map<InterpreterType, List<String>> history;

  public List<String> historyForLanguage(InterpreterType type) {
    List<String> langHistory = history.get(type);
    if (langHistory == null) {
      langHistory = new ArrayList<String>();
      history.put(type, langHistory);
    }
    return langHistory;
  }

  public void addCommand(InterpreterType type, String command) {
    List<String> langHist = history.get(type);
    if (langHist == null) {
      langHist = new ArrayList<String>();
      history.put(type, langHist);
    }
    langHist.add(0, command);
  }

  public CommandHistory() {
    history = new HashMap<InterpreterType, List<String>>();
  }

}
