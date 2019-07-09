/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.io.serialization;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.DotCall;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.OpenSpec;
import org.renjin.primitives.io.serialization.RDataWriter.PersistenceHook;
import org.renjin.sexp.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of serialization builtins and internal functions.
 */
public class Serialization {


  private static final int DEFAULT_SERIALIZATION_VERSION = 0;

  public enum SerializationType { ASCII, XDR, BINARY};

  @Internal
  public static SEXP unserializeFromConn(@Current Context context,
      SEXP conn, Environment rho) throws IOException {
    
    RDataReader reader = new RDataReader(context, Connections.getConnection(context, conn).getInputStream());
    return reader.readFile();
  }

  @Internal
  public static SEXP unserializeFromConn(@Current Context context,
      SEXP conn, Null nz) throws IOException {
    
    RDataReader reader = new RDataReader(context, 
        Connections.getConnection(context, conn).getInputStream());
    return reader.readFile();
  }

  /**
   * 
   * @param context
   * @param object
   * @param con
   * @param ascii
   * @param versionSexp
   * @param refhook  A mechanism is provided to allow special handling of non-system
   reference objects (all weak references and external pointers, and
   all environments other than package environments, name space
   environments, and the global environment).  The hook function
   consists of a function pointer and a data value.  The serialization
   function pointer is called with the reference object and the data
   value as arguments.  It should return R_NilValue for standard
   handling and an STRSXP for special handling.  In an STRSXP is
   returned, then a special handing mark is written followed by the
   strings in the STRSXP (attributes are ignored).  
   * @throws IOException
   */
  @Internal
  public static void serializeToConn(@Current Context context, SEXP object, 
      SEXP con, boolean ascii, SEXP versionSexp, SEXP refhook) throws IOException {
    
    if(ascii) {
      throw new EvalException("ascii format serialization not implemented");
    }
    
    int version = DEFAULT_SERIALIZATION_VERSION;
    if(versionSexp instanceof Vector && versionSexp.length() == 1) {
      version = ((Vector)versionSexp).getElementAsInt(0);
    }
    
    RDataWriter writer = new RDataWriter(context,
        createHook(context, refhook), Connections.getConnection(context, con).getOutputStream());
    writer.serialize(object);
    
  }
  
  
  /**
   * Serializes a list of objects within a given environment to a connnection
   * 
   * @param context 
   * @param names character vector of the names of objects to be serialized
   * @param connHandle the connection handle (int)
   * @param ascii TRUE for ascii (not implemented)
   * @param version the version number 
   * @param envir the environment from which to save objects
   * @param evalPromises TRUE to force promises
   * @throws IOException
   */
  @Internal
  public static void saveToConn(@Current Context context, 
      StringVector names, 
      SEXP connHandle, 
      boolean ascii, 
      SEXP version, 
      Environment envir, 
      boolean evalPromises) throws IOException {
    
    Connection con = Connections.getConnection(context, connHandle);
    boolean wasOpen = con.isOpen();
    if(!wasOpen) {
      con.open(new OpenSpec("wb"));
    }
    
    if(!con.canWrite()) {
      throw new EvalException("connection not open for writing");
    } 
    if(ascii) {
      throw new EvalException("ascii serialization not implemented");
    }
    PairList.Builder list = new PairList.Builder();
    for(String name : names) {
      SEXP value = envir.getVariable(context, name);
      if(value == Symbol.UNBOUND_VALUE) {
        throw new EvalException("object '%s' not found", name);
      }
      if(evalPromises) {
        value = value.force(context);
      }
      list.add(name, value);
    }
    
    RDataWriter writer = new RDataWriter(context, con.getOutputStream());
    writer.save(list.build());
    
    if (!wasOpen) {
      Connections.close(context, connHandle);
    }
  }
  
  @Internal
  public static void save(SEXP list, SEXP file, SEXP ascii, SEXP version, SEXP environment, SEXP evalPromises) {
    throw new EvalException("Serialization version 1 not supported.");
  }
  
  private static PersistenceHook createHook(final Context context, final SEXP hookExp) {
    if(hookExp == Null.INSTANCE) {
      return null; 
    } 
    if(!(hookExp instanceof Closure)) {
      throw new EvalException("Illegal type for refhook"); 
    }  
    return new PersistenceHook() {
      
      @Override
      public Vector apply(SEXP exp) {
        
        // make sure exp doesn't get evaled
        SEXP promisedExp =  exp.repromise();
        
        FunctionCall hookCall = FunctionCall.newCall(hookExp, promisedExp);
        SEXP result = context.evaluate(hookCall);
        if(result == Null.INSTANCE) {
          return Null.INSTANCE;
        } else if(result instanceof StringVector) {
          return (StringVector) result;
        } else {
          throw new EvalException("Unexpected result from hook function: " + result);
        }
      }
    };
  
  }
  
  
  /**
   * Reload datasets written with the function save.
   * 
   * @param context
   * @param conn
   *          a (readable binary) connection or a character string giving the
   *          name of the file to load.
   * @param env
   *          the environment where the data should be loaded.
   * @return A character vector of the names of objects created, invisibly.
   * @throws IOException
   */
  @Internal
  public static SEXP loadFromConn2(@Current Context context, SEXP conn,
      Environment env) throws IOException {

    InputStream inputStream = Connections.getConnection(context, conn).getInputStream();
    return load(context, env, inputStream);
  }

  public static SEXP load(@Current Context context, Environment env, InputStream inputStream) throws IOException {
    RDataReader reader = new RDataReader(context, inputStream);
    HasNamedValues data = EvalException.checkedCast(reader.readFile());

    StringArrayVector.Builder names = new StringArrayVector.Builder();

    for (NamedValue pair : data.namedValues()) {
      env.setVariable(context, Symbol.get(pair.getName()), pair.getValue());
      names.add(pair.getName());
    }

    return names.build();
  }


  /**
   *
   * Writes ‘object’ to the specified connection.  If ‘connection’ is ‘NULL’ then
   * ‘object’ is serialized to a raw vector, which is returned as the result of
   * ‘serialize’.
   *
   * @param object the object to serialize
   * @param connection  an open connection handle OR {@code Null.INSTANCE} if the object is to be serialized
   * to a raw vector
   * @param ascii if 'TRUE' an ASCII representation is written (not implemented)
   * @param version
   * @param refhook  a hook function for handling reference objects.
   * @return a {@code RawVector}
   * @throws IOException
   */
  @DotCall("R_serialize")
  public static SEXP serialize(@Current Context context, SEXP object, SEXP connection, boolean ascii,
      SEXP version, SEXP refhook) throws IOException {
    //EvalException.check(!ascii, "ascii = TRUE has not been implemented");
    EvalException.check(refhook == Null.INSTANCE, "refHook != NULL has not been implemented yet.");
    EvalException.check(connection == Null.INSTANCE, "Only connection = NULL has been implemented so far.");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos, 
            ascii? SerializationType.ASCII: SerializationType.XDR);
    writer.serialize(object);
   
    return new RawVector(baos.toByteArray());
  }
  
  /**
   * 
   * @param connection a {@code RawVector}
   * @param refhook a hook function for handling reference objects.
   * @return a object
   * @throws IOException
   */
  @DotCall("R_unserialize")
  public static SEXP unserialize(@Current Context context, SEXP connection, SEXP refhook) throws IOException {
    EvalException.check(refhook == Null.INSTANCE, "refHook != NULL has not been implemented yet.");

    if(connection instanceof StringVector) {
      throw new EvalException("character vectors are no longer accepted by unserialize()");
    } else if(connection instanceof RawVector) {
      RDataReader reader = new RDataReader(context,
          new ByteArrayInputStream(((RawVector)connection).toByteArray()));
      return reader.readFile();
    } else {
      return unserializeFromConn(context, connection, Null.INSTANCE);
    }
  }
}
