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

package org.renjin.appengine.repl.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.renjin.appengine.AppEngineContextFactory;
import org.renjin.appengine.repl.shared.InterpreterException;
import org.renjin.appengine.repl.shared.LotREPLsApi;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.eval.SecurityManager;
import org.renjin.parser.RParser;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ExpressionVector;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Server-side implementation of LotREPLsApi. This class manages the
 * interpreters for each language and routes commands appropriately.
 *
 */
public class LotREPLsApiImpl extends RemoteServiceServlet implements
    LotREPLsApi {
  private static final String GLOBALS = "GLOBALS";
  private final Logger log = Logger.getLogger(LotREPLsApiImpl.class.getName());

  private Context masterTopLevelContext;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);

    // The default VFS manager uses a Softref file cache that
    // is not allowed on AppEngine
    masterTopLevelContext = AppEngineContextFactory.createTopLevelContext(config.getServletContext());
    
    // disable creation of java objects for the moment
    // (otherwise any would could access the datastore and do something annoying with it)
    // figure out how to apply more narrow gain permissions later.
    masterTopLevelContext.getGlobals().securityManager = new SecurityManager() {

		@Override
		public boolean allowNewInstance(Class clazz) {
			return false;
		}
    	
    };

  }

  public String eval(String script)
      throws InterpreterException {

    ExpressionVector expression = RParser.parseSource(script + "\n");
    if(expression == null) {
      return "";
    }

    Context context = masterTopLevelContext.fork();
    StringWriter writer = new StringWriter();
    context.getGlobals().setStdOut(new PrintWriter(writer));
    restoreGlobals(context);
    SEXP result;
    try {
      result = context.evaluate(expression);
    } catch(EvalException e) {
      log.log(Level.WARNING, "Evaluation failed", e);
      throw new InterpreterException(e.getMessage());
    }
    saveGlobals(context);

    if(!context.getGlobals().isInvisible()) {
    	try {
    	context.evaluate(
    			FunctionCall.newCall(Symbol.get("print"), result));
    	} catch(Exception e) {
    		throw new InterpreterException("Ooops - exception while printing result: " + 
    				e.getMessage());
    	}
    }

    context.getGlobals().stdout.flush();
    return writer.toString();

  }

  private void restoreGlobals(Context context) {
    HttpServletRequest request = getThreadLocalRequest();
    HttpSession session = request.getSession();
    byte[] globals = null;
    try {
      globals = (byte[]) session.getAttribute(GLOBALS);
      if (globals != null) {

    	RDataReader reader = new RDataReader(context,
            new ByteArrayInputStream(globals));
        PairList list = (PairList) reader.readFile();
        for(PairList.Node node : list.nodes()) {
          context.getGlobalEnvironment().setVariable(node.getTag(), node.getValue());
        }
      }
    } catch(Exception e) {
      // If there was a deserialization error, throw the session away
      session.removeAttribute(GLOBALS);
      log.log(Level.WARNING, "Could not deserialize context.", e);
    }
  }

  private void saveGlobals(Context context) {
    try {
      PairList.Builder list = new PairList.Builder();
      Environment globalEnvironment = context.getGlobalEnvironment();

      int count =0;
      for(Symbol symbol : globalEnvironment.getSymbolNames()) {
        SEXP value = globalEnvironment.getVariable(symbol);
        if(value instanceof Vector) {
          list.add(symbol, value);
          count++;
        }
      }
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      RDataWriter writer = new RDataWriter(context, baos);
      writer.writeExp(list.build());

      log.fine(count + " variable saved, " + baos.toByteArray().length + " bytes");

      HttpServletRequest request = getThreadLocalRequest();
      HttpSession session = request.getSession();
      session.setAttribute(GLOBALS, baos.toByteArray());

    } catch(Exception e) {
      log.log(Level.WARNING, "Failed to serialize globals", e);
    }
  }
}
