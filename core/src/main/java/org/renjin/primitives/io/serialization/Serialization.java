package org.renjin.primitives.io.serialization;

import static org.renjin.util.CDefines.R_NilValue;
import static org.renjin.util.CDefines._;
import static org.renjin.util.CDefines.error;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.invoke.annotations.Builtin;
import org.renjin.invoke.annotations.Current;
import org.renjin.invoke.annotations.DotCall;
import org.renjin.invoke.annotations.Internal;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.OpenSpec;
import org.renjin.primitives.io.serialization.RDataWriter.PersistenceHook;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.HasNamedValues;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.Promise;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;


public class Serialization {


  private static final int DEFAULT_SERIALIZATION_VERSION = 0;

  public enum SERIALIZATION_TYPE { ASCII, XDR, BINARY};

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
    writer.save(object);
    
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
      SEXP value = envir.getVariable(name);
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
      con.close();
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
        Promise promisedExp = Promise.repromise(exp);
        
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

    RDataReader reader = new RDataReader(context,
        Connections.getConnection(context, conn).getInputStream());
    HasNamedValues data = EvalException.checkedCast(reader.readFile());

    StringArrayVector.Builder names = new StringArrayVector.Builder();

    for (NamedValue pair : data.namedValues()) {
      env.setVariable(Symbol.get(pair.getName()), pair.getValue());
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
            ascii? SERIALIZATION_TYPE.ASCII: SERIALIZATION_TYPE.XDR);
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
        error(_("character vectors are no longer accepted by unserialize()"));
        return R_NilValue/* -Wall */;
    } else if(connection instanceof RawVector) {
      RDataReader reader = new RDataReader(context, 
              new ByteArrayInputStream(((RawVector)connection).getAsByteArray()));
      return reader.readFile();
    } else {
      return unserializeFromConn(context, connection, Null.INSTANCE);
    }
  }
}
