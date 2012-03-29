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

import org.renjin.appengine.repl.shared.InterpreterException;
import org.renjin.appengine.repl.shared.InterpreterType;
import org.renjin.appengine.repl.shared.LotREPLsApi;
import org.renjin.appengine.repl.shared.LotREPLsApiAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point class for the UI. This sets up the command prompt area and sets
 * up the event handlers.
 */
public class LotREPLs implements EntryPoint {

  private final LotREPLsApiAsync api = GWT.create(LotREPLsApi.class);

  private final FlowPanel content = new FlowPanel();

  private CommandPrompt commandPrompt;

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    doWarmUpRequest();

    commandPrompt = new CommandPrompt(new CommandPrompt.CommandEnteredCallback() {
      /**
       * The command entered handler does a little switch-a-roo. It removes the
       * input area and prompt, replacing them with immutable copies, and then
       * waits for the response. Once the script results (or error) are ready,
       * it inserts a result area and then re-adds the command prompt.
       */
      public void onCommandEntered(InterpreterType type, String script) {
        content.remove(commandPrompt.panel());

        Widget enteredScript = commandPrompt.createImmutablePanel();
        content.add(enteredScript);

        commandPrompt.clearInputArea();

        Window.scrollTo(0, 100000);

        api.eval(script, new ScriptCallback());
      }
    });

    String script = Location.getParameter("script");
    if (script != null && !script.equals("")) {
      commandPrompt.setScript(script);
    }

    content.add(commandPrompt.panel());

    content.setWidth("100%");
    RootPanel.get("root").add(content);
    commandPrompt.claimFocus();
  }

  private void doWarmUpRequest() {
    api.eval("1", new AsyncCallback<String>() {
      @Override
      public void onFailure(Throwable throwable) {

      }

      @Override
      public void onSuccess(String s) {

      }
    });
  }

  private class ScriptCallback implements AsyncCallback<String> {
    public void onFailure(Throwable caught) {
      if (caught instanceof InterpreterException) {
        setResult("Error: " + caught.getMessage(), false);
      } else {
        setResult("Something bad happened - click your heels three times and try again", false);
      }
    }

    public void onSuccess(String result) {
      setResult(result, true);
    }
  }

  private void setResult(String result, boolean succeeded) {
    Element e = Document.get().createPreElement();
    e.setInnerText(result);
    e.setClassName(succeeded ? "result" : "error");
    e.setAttribute("tabIndex", "-1");
    content.getElement().appendChild(e);
    content.add(commandPrompt.panel());
    commandPrompt.claimFocus();
    Window.scrollTo(0, 100000);
  }
}
