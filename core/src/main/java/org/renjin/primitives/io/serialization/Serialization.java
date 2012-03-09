package org.renjin.primitives.io.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.apache.commons.vfs.FileContent;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.io.ByteArrayCompression;
import org.renjin.primitives.io.connections.Connection;

import r.lang.Context;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.HasNamedValues;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.NamedValue;
import r.lang.Null;
import r.lang.PairList;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.Vector;
import r.lang.exception.EvalException;

public class Serialization {


  @Primitive
  public static SEXP unserializeFromConn(@Current Context context,
      Connection conn, Environment rho) throws IOException {
    
    RDataReader reader = new RDataReader(context, conn.getInputStream());
    return reader.readFile();
  }

  @Primitive
  public static SEXP unserializeFromConn(@Current Context context,
      Connection conn, Null nz) throws IOException {
    
    RDataReader reader = new RDataReader(context, 
        conn.getInputStream());
    return reader.readFile();
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
  public static SEXP loadFromConn2(@Current Context context, Connection conn,
      Environment env) throws IOException {

    RDataReader reader = new RDataReader(context,
        conn.getInputStream());
    HasNamedValues data = EvalException.checkedCast(reader.readFile());

    StringVector.Builder names = new StringVector.Builder();

    for (NamedValue pair : data.namedValues()) {
      env.setVariable(Symbol.get(pair.getName()), pair.getValue());
      names.add(pair.getName());
    }

    return names.build();
  }
  
  @Primitive
  public static SEXP serializeToConn(@Current Context context, SEXP object, Connection con, SEXP ascii, 
      SEXP version, SEXP refhook) {
    throw new UnsupportedOperationException();
    
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
      Symbol name = Symbol.get(names.getElement(i));

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
      targetEnvironment.setVariable(name, new Promise(context, eenv, newCall));
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
      @Current final Environment rho, IntVector key, String file,
      int compression, final SEXP restoreFunction) throws IOException,
      DataFormatException {
    byte buffer[] = readRawFromFile(context, file, key);

    buffer = ByteArrayCompression.decompress(compression, buffer);

    RDataReader reader = new RDataReader(context, rho,
        new ByteArrayInputStream(buffer),
        new RDataReader.PersistentRestorer() {

          @Override
          public SEXP restore(SEXP values) {
            FunctionCall call = FunctionCall.newCall(restoreFunction, values);
            return context.evaluate(call, context.getGlobalEnvironment());
          }
        });

    SEXP exp = reader.readFile();
    if (exp instanceof Promise) {
      exp = ((Promise) exp).force();
    }
    return exp;
  }
  
  public static byte[] readRawFromFile(@Current Context context, String file,
      IntVector key) throws IOException {
    if (key.length() != 2) {
      throw new EvalException("bad offset/length argument");
    }
    int offset = key.getElementAsInt(0);
    int length = key.getElementAsInt(1);

    byte buffer[] = new byte[length];

    FileContent content = context.resolveFile(file).getContent();
    if(content.isOpen()) {
      throw new EvalException(file + " is already open!");
    }
    DataInputStream in = new DataInputStream(content.getInputStream());
    try {
      in.skipBytes(offset);
      in.readFully(buffer);
    } finally {
      in.close();
    }

    return buffer;
  }
  

  public static SEXP lazyLoadDbInsertValue(Context context, SEXP value,
      SEXP file, Vector compress) throws IOException, Exception {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.writeExp(value);
    
    byte[] bytes = ByteArrayCompression.compress(compress.getElementAsInt(0), baos.toByteArray());
    
    IntVector key = appendRawToFile(file, bytes);
 //   return key;
    return Null.INSTANCE;
  }

  private static IntVector appendRawToFile(SEXP file, byte[] bytes) {

    
    
    return null;
  }

}
