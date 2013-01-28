package org.renjin.primitives.io.serialization;

import org.renjin.base.Base;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.io.ByteArrayCompression;
import org.renjin.primitives.io.connections.Connection;
import org.renjin.primitives.io.connections.Connections;
import org.renjin.primitives.io.connections.OpenSpec;
import org.renjin.primitives.io.serialization.RDataWriter.PersistenceHook;
import org.renjin.sexp.*;

import java.io.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.zip.DataFormatException;


public class Serialization {


  private static final int DEFAULT_SERIALIZATION_VERSION = 0;



  @Primitive
  public static SEXP unserializeFromConn(@Current Context context,
      SEXP conn, Environment rho) throws IOException {
    
    RDataReader reader = new RDataReader(context, Connections.getConnection(context, conn).getInputStream());
    return reader.readFile();
  }

  @Primitive
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
  @Primitive
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
  @Primitive
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
  
  @Primitive
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
  @Primitive
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
   * Populates a target {@code Environment} with promises to serialized
   * expressions.
   * 
   * @param names
   *          the names of the symbols to be populated
   * @param values
   * @param expr
   * @param eenv
   * @param targetEnvironment
   */
  public static void makeLazy(@Current Context context, StringVector names,
      ListVector values, FunctionCall expr, Environment eenv,
      Environment targetEnvironment) {

    for (int i = 0; i < names.length(); i++) {
      // the name of the symbol
      Symbol name = Symbol.get(names.getElementAsString(i));

      // c(pos, length) of the serialized object
      SEXP value = context.evaluate( values.get(i), (Environment) eenv);
      // create a new call, replacing the first argument with the
      // provided arg
      PairList.Node.Builder newArgs = PairList.Node.newBuilder();
      newArgs.add(value);
      for (int j = 1; j < expr.getArguments().length(); ++j) {
        newArgs.add(expr.<SEXP> getArgument(j));
      }
      FunctionCall newCall = new FunctionCall(expr.getFunction(),
          newArgs.build());
      targetEnvironment.setVariable(name, Promise.repromise(eenv, newCall));
    }
  }

  /**
   * Retrieves a sequence of bytes as specified by a position/length key from a
   * file, optionally decompresses, and unserializes the bytes. If the result is
   * a promise, then the promise is forced.
   * 
   * @param key
   *          c(offset, length)
   * @param file
   *          the path to the file from which to load the value
   * @param compression
   *          0=not compressed, 1=deflate, ...
   * @param restoreFunction
   *          a function called to load persisted objects from the serialized
   *          stream
   */
  public static SEXP lazyLoadDBfetch(@Current final Context context,
      @Current final Environment rho, IntVector key, final String file,
      int compression, final SEXP restoreFunction) throws IOException,
      DataFormatException {
    
    try {

      byte buffer[] = readRawFromFile(context, file, key);

      buffer = ByteArrayCompression.decompress(compression, buffer);

      RDataReader reader = new RDataReader(context, rho,
          new ByteArrayInputStream(buffer),
          new RDataReader.PersistentRestorer() {
  
            @Override
            public SEXP restore(StringVector values) {
              FunctionCall call = FunctionCall.newCall(restoreFunction, values);
              return context.evaluate(call, context.getGlobalEnvironment());
            }
          });
  
      return reader.readFile().force(context);
    } catch(Exception e) {
      throw new EvalException("Exception reading database entry at " + key + " in " +
            file, e);
    }
  }
  
  public static byte[] readRawFromFile(@Current final Context context, final String file,
      IntVector key) throws IOException, ExecutionException, DataFormatException {
    if (key.length() != 2) {
      throw new EvalException("bad offset/length argument");
    }

    int offset = key.getElementAsInt(0);
    int length = key.getElementAsInt(1);

    RDatabase database = context.getSession().getPackageDatabaseCache().get(file, new Callable<RDatabase>() {
      @Override
      public RDatabase call() throws Exception {
        return new RDatabase(context.resolveFile(file));
      }
    });

    return database.getBytes(offset, length);
  }
  

  /**
   * Appends an SEXP to a rdb file, returning an IntVector in the form (offset, length)
   * of the compressed block.
   * 
   * <p>This method is actually called from {@link Base}
   */
  public static SEXP lazyLoadDbInsertValue(Context context, Environment rho, SEXP value,
       String file, Vector compress, SEXP hook) throws IOException, Exception {

    File rdb = new File(file);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, createHook(context, hook), baos);
    writer.save(value);
    
    byte[] bytes = ByteArrayCompression.compress(compress.getElementAsInt(0), baos.toByteArray());
    
    FileOutputStream fos = new FileOutputStream(file, true);
    try { 
      fos.write(bytes);
    } finally {
      fos.close();
    }
    
    int offset = (int) (rdb.length() - bytes.length);
    int length = bytes.length;
    
    return new IntArrayVector(offset, length);
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
  public static SEXP serialize(@Current Context context, SEXP object, SEXP connection, boolean ascii,
      SEXP version, SEXP refhook) throws IOException {
    EvalException.check(!ascii, "ascii = TRUE has not been implemented");
    EvalException.check(refhook == Null.INSTANCE, "refHook != NULL has not been implemented yet.");
    EvalException.check(connection == Null.INSTANCE, "Only connection = NULL has been implemented so far.");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.serialize(object);
    return new RawVector(baos.toByteArray());
  }
}
