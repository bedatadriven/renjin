/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.Serialization.SerializationType;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.sexp.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.renjin.primitives.io.serialization.SerializationFormat.*;

public class RDataWriter implements AutoCloseable {


  /**
   * Interfaces that allows R developers and Renjin containers to provide
   * custom serialization for certain types of sexps, like Java objects that
   * are stored in an R Environment.
   */
  public interface PersistenceHook {

    /**
     *
     * @param exp the S-expression to serialize
     * @return {@code Null.INSTANCE} if the container provide custom serialization
     * for this sexp, or a {@code StringVector} if it does.
     */
    Vector apply(SEXP exp);
  }
  
  private WriteContext context;
  private PersistenceHook hook;
  private DataOutputStream conn;
  private StreamWriter out;
  private SerializationType serializationType;

  private Map<SEXP, Integer> references = Maps.newIdentityHashMap();

  public RDataWriter(WriteContext context, PersistenceHook hook, OutputStream out,
                     SerializationType type) {
    this.context = context;
    this.hook = hook;
    this.conn = new DataOutputStream(out);
    this.serializationType = type;
    switch(this.serializationType) {
      case ASCII: this.out = new AsciiWriter(this.conn); break;
      default: this.out = new XdrWriter(this.conn); break;
    }
  }
  
  public RDataWriter(Context context, PersistenceHook hook, OutputStream out) throws IOException {
    this(new SessionWriteContext(context), hook, out, SerializationType.XDR);
  }

  public RDataWriter(Context context, OutputStream out, SerializationType st) throws IOException {
    this(new SessionWriteContext(context), null, out, st);
  }
  
  public RDataWriter(Context context, OutputStream out) throws IOException {
    this(context, null, out);
  }

  public RDataWriter(WriteContext writeContext, OutputStream os) {
    this(writeContext, null, os, SerializationType.XDR);
  }


  /**
   * @deprecated Call save() explicitly
   * @param sexp
   * @throws IOException 
   */
  @Deprecated
  public void writeFile(Context contextEnv, SEXP sexp) throws IOException {
    save(contextEnv, sexp);
  }

  /**
   * Serializes the given {@code sexp}, prefixed by the 
   * magic bytes 'RDX\n'
   * @throws IOException
   */
  public void save(Context contextEnv, SEXP sexp) throws IOException {
    if(serializationType == SerializationType.ASCII) {
      conn.writeBytes(ASCII_MAGIC_HEADER);
    } else {
      conn.writeBytes(XDR_MAGIC_HEADER);
    }
    
    serialize(contextEnv, sexp);
  }

  public void serialize(Context contextEnv, SEXP exp) throws IOException {
    if(serializationType == SerializationType.ASCII) {
      conn.writeByte(ASCII_FORMAT);
    } else {
      conn.writeByte(XDR_FORMAT);
    }
    
    conn.writeByte('\n');
    writeVersion();
    writeExp(contextEnv, exp);
  }


  @Override
  public void close() throws IOException {
    out.close();
  }


  private void writeVersion() throws IOException {
    out.writeInt(VERSION2);
    out.writeInt(Version.CURRENT.asPacked());
    out.writeInt(new Version(2,3,0).asPacked());
  }

  private void writeExp(Context contextEnv, SEXP exp) throws IOException {
    if(tryWriteRef(exp)) {
      return;
    }
    
    if(tryWritePersistent(exp)) {
      return;
    }
     
    if(exp instanceof Null) {
      writeNull();
    } else if(exp instanceof LogicalVector) {
      writeLogical(contextEnv, (LogicalVector) exp);
    } else if(exp instanceof IntVector) {
      writeIntVector(contextEnv, (IntVector) exp);
    } else if(exp instanceof DoubleVector) {
      writeDoubleVector(contextEnv, (DoubleVector) exp);
    } else if(exp instanceof StringVector) {
      writeStringVector(contextEnv, (StringVector) exp);
    } else if(exp instanceof ComplexVector) {
      writeComplexVector(contextEnv, (ComplexVector)exp);
    } else if(exp instanceof Promise) {
      writePromise(contextEnv, (Promise)exp);
    } else if(exp instanceof ListVector) {
      writeList(contextEnv, (ListVector) exp);
    } else if(exp instanceof FunctionCall) {
      writeFunctionCall(contextEnv, (FunctionCall)exp);
    } else if(exp instanceof PairList.Node) {
      writePairList(contextEnv, (PairList.Node) exp);
    } else if(exp instanceof Symbol) {
      writeSymbol((Symbol) exp);
    } else if(exp instanceof Closure) {
      writeClosure(contextEnv, (Closure)exp);
    } else if(exp instanceof RawVector) {
      writeRawVector(contextEnv, (RawVector) exp);
    } else if(exp instanceof Environment) {
      writeEnvironment(contextEnv, (Environment)exp);
    } else if(exp instanceof PrimitiveFunction) {
      writePrimitive((PrimitiveFunction)exp);
    } else if(exp instanceof S4Object) {
      writeS4(contextEnv, (S4Object)exp);
    } else if(exp instanceof ExternalPtr) {
      writeExternalPtr(contextEnv, (ExternalPtr)exp);
    } else if(exp instanceof CHARSEXP) {
      writeCharExp(((CHARSEXP)exp).getValue());
    } else {
      throw new UnsupportedOperationException("serialization of " + exp.getClass().getName() + " not implemented: ["
         + exp.toString() + "]");
    }
  }


  private boolean tryWritePersistent(SEXP exp) throws IOException {
    if(hook == null) {
      return false;
    }
    if(exp == Null.INSTANCE || isSpecialEnvironment(exp)) {
      return false;
    }
    Vector name = hook.apply(exp);
    if(name == Null.INSTANCE) {
      return false;
    }
    
    out.writeInt(SerializationFormat.PERSISTSXP);
    writePersistentNameVector((StringVector) name);
    addRef(exp);
    return true;
  } 

  private void writePersistentNameVector(StringVector name) throws IOException {
    // place holder to allow names attribute
    out.writeInt(0);
    out.writeInt(name.length());
    for(int i=0;i!=name.length();++i) {
      writeCharExp(name.getElementAsString(i));
    }
  }

  private boolean isSpecialEnvironment(SEXP exp) {
    if(! (exp instanceof Environment)) {
      return false;
    }
    if( exp == Environment.EMPTY) {
      return true;
    }
    if( context.isBaseEnvironment((Environment) exp) ) {
      return true;
    }
    if(context.isNamespaceEnvironment((Environment) exp)) {
      return true;
    }
    if(isPackageEnvironment(exp)) {
      return true;
    }
    return false;
  }

  private boolean isPackageEnvironment(SEXP exp) {
    // TODO 
    return false;
  }

  private void writeNull() throws IOException {
    out.writeInt(NILVALUE_SXP);
  }

  private void writeLogical(Context contextEnv, LogicalVector vector) throws IOException {
    writeFlags(SexpType.LGLSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      out.writeInt(vector.getElementAsRawLogical(i));
    }
    writeAttributes(contextEnv, vector);
  }

  private void writeIntVector(Context contextEnv, IntVector vector) throws IOException {
    writeFlags(SexpType.INTSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          conn.writeBytes("NA\n");
        } else {
          out.writeInt(vector.getElementAsInt(i));
        }
      }
    } else {
      for(int i=0;i!=vector.length();++i) {
        out.writeInt(vector.getElementAsInt(i));
      }
    }
    
    writeAttributes(contextEnv, vector);
  }

  private void writeDoubleVector(Context contextEnv, DoubleVector vector) throws IOException {
    writeFlags(SexpType.REALSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      for(int i=0;i!=vector.length();++i) {
        double d = vector.getElementAsDouble(i);
        if(!DoubleVector.isFinite(d)) {
          if(DoubleVector.isNaN(d)) {
            conn.writeBytes("NA\n");
          } else if (d < 0) {
            conn.writeBytes("-Inf\n");
          } else {
            conn.writeBytes("Inf\n");
          }
        } else {
          out.writeDouble(vector.getElementAsDouble(i));
        }  
      }
    } else {
      for(int i=0;i!=vector.length();++i) { 
        if(vector.isElementNA(i)) {
          out.writeLong(DoubleVector.NA_BITS);
        } else {
          out.writeDouble(vector.getElementAsDouble(i));
        }
      }
    }
    
    writeAttributes(contextEnv, vector);
  }


  private void writeS4(Context contextEnv, S4Object exp) throws IOException {
    writeFlags(SexpType.S4SXP, exp);
    writeAttributes(contextEnv, exp);
  }

  private void writeExternalPtr(Context contextEnv, ExternalPtr exp) throws IOException {
    addRef(exp);
    writeFlags(SexpType.EXTPTRSXP, exp);
    writeExp(contextEnv, Null.INSTANCE); // protected value (not currently used)
    writeExp(contextEnv, Null.INSTANCE); // tag (not currently used)
    writeAttributes(contextEnv, exp);
  }

  private void writeComplexVector(Context contextEnv, ComplexVector vector) throws IOException {
    writeFlags(SexpType.CPLXSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      Complex value = vector.getElementAsComplex(i);
      out.writeDouble(value.getReal());
      out.writeDouble(value.getImaginary());
    }
    writeAttributes(contextEnv, vector);
  }

  private void writeRawVector(Context contextEnv, RawVector vector) throws IOException {
    writeFlags(SexpType.RAWSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      byte[] bytes = vector.toByteArray();
      for(int i=0;i!=vector.length();++i) {
        conn.writeBytes(String.format("%02x\n", bytes[i]));
      }
    } else {
      out.writeString(vector.toByteArray());
    }
    writeAttributes(contextEnv, vector);
  }
  
  private void writeStringVector(Context contextEnv, StringVector vector) throws IOException {
    writeFlags(SexpType.STRSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      writeCharExp(vector.getElementAsString(i));
    }
    writeAttributes(contextEnv, vector);
  }

  private void writeList(Context contextEnv, ListVector vector) throws IOException {
    writeFlags(SexpType.VECSXP, vector);
    out.writeInt(vector.length());
    for(SEXP element : vector) {
      writeExp(contextEnv, element);
    }
    writeAttributes(contextEnv, vector);
  }

  private void writePromise(Context contextEnv, Promise exp) throws IOException {
    out.writeInt(Flags.computePromiseFlags(exp));
    writeAttributes(contextEnv, exp);
    if(exp.getEnvironment() != null) {
      writeExp(contextEnv, exp.getEnvironment());
    }
    writeExp(contextEnv, exp.getValue() == null ? Null.INSTANCE : exp.getValue());
    writeExp(contextEnv, exp.getExpression());
  }

  private void writePairList(Context contextEnv, PairList.Node node) throws IOException {

    while(true) {
      writeFlags(SexpType.LISTSXP, node);
      writeAttributes(contextEnv, node);
      writeTag(contextEnv, node);
      writeExp(contextEnv, node.getValue());

      if(node.getNext() == Null.INSTANCE) {
        writeNull();
        break;
      }
      node = node.getNextNode();
    }
  }

  private void writeFunctionCall(Context contextEnv, FunctionCall exp) throws IOException {
    writeFlags(SexpType.LANGSXP, exp);
    writeAttributes(contextEnv, exp);
    writeTag(contextEnv, exp);
    writeExp(contextEnv, exp.getValue());
    if(exp.hasNextNode()) {
      writeExp(contextEnv, exp.getNextNode());
    } else {
      writeNull();
    }
  }

  private void writeClosure(Context contextEnv, Closure exp) throws IOException {
    writeFlags(SexpType.CLOSXP, exp);
    writeAttributes(contextEnv, exp);
    writeExp(contextEnv, exp.getEnclosingEnvironment());
    writeExp(contextEnv, exp.getFormals());
    writeExp(contextEnv, exp.getBody());
  }
  
  private void writeEnvironment(Context contextEnv, Environment env) throws IOException {

    if(context.isGlobalEnvironment(env)) {
      out.writeInt(SerializationFormat.GLOBALENV_SXP);
    } else if(context.isBaseEnvironment(env)) {
      out.writeInt(SerializationFormat.BASEENV_SXP);
    } else if(env == Environment.EMPTY) {
      out.writeInt(SerializationFormat.EMPTYENV_SXP);
    } else {      
      if(context.isNamespaceEnvironment(env)) {
        writeNamespace(env);
      } else {
        addRef(env);
        writeFlags(SexpType.ENVSXP, env);
        out.writeInt(env.isLocked() ? 1 : 0);
        writeExp(contextEnv, env.getParent());
        writeFrame(contextEnv, env);
        writeExp(contextEnv, Null.INSTANCE); // hashtab (unused)
        
        // NB: attributes for an environment are
        // ALWAYS written, even if NULL
        writeExp(contextEnv, env.getAttributes().asPairList());
      }
    }
  }
  
  private void writeFrame(Context contextEnv, Environment exp) throws IOException {
    PairList.Builder frame = new PairList.Builder();
    for(Symbol name : exp.getSymbolNames()) {
      frame.add(name, exp.getVariable(contextEnv, name));
    }
    writeExp(contextEnv, frame.build());
  }

  private void writeNamespace(Environment ns) throws IOException {
    if(context.isBaseNamespaceEnvironment(ns)) {
      out.writeInt(SerializationFormat.BASENAMESPACE_SXP);
    } else {
      addRef(ns);
      writeFlags(SerializationFormat.NAMESPACESXP, ns);
      writePersistentNameVector(getNamespaceName(ns));
    }
  }

  private boolean tryWriteRef(SEXP exp) throws IOException {
    if(references.containsKey(exp)) {
      writeRefIndex(references.get(exp));
      return true;
    } else {
      return false;
    }
  }

  private void writeRefIndex(int index) throws IOException {
    if(index > Flags.MAX_PACKED_INDEX) {
      out.writeInt(SexpType.REFSXP);
      out.writeInt(index);
    } else {
      out.writeInt(SexpType.REFSXP | (index << 8));
    }
  }
 
  private void addRef(SEXP exp) {
    references.put(exp, references.size() + 1);
  }

  private StringVector getNamespaceName(Environment ns) {
    return StringVector.valueOf(context.getNamespaceName(ns));
  }

  private void writeSymbol(Symbol symbol) throws IOException {
    if(symbol == Symbol.UNBOUND_VALUE) {
      out.writeInt(SerializationFormat.UNBOUNDVALUE_SXP);
    } else if(symbol == Symbol.MISSING_ARG) {
      out.writeInt(SerializationFormat.MISSINGARG_SXP);
    } else {
      addRef(symbol);
      writeFlags(SexpType.SYMSXP, symbol);
      writeCharExp(symbol.getPrintName());
    }
  }

  private void writeCharExp(String string) throws IOException {
    
    if(StringVector.isNA(string)) {
      out.writeInt( Flags.computeCharSexpFlags(ASCII_MASK));
      out.writeInt(-1);
    
    } else {

      byte[] bytes = string.getBytes(Charsets.UTF_8);

      // Normally we would just write this out as UTF-8
      // but GNU R seems to only write out string in UTF-8 if 
      // it's really needed. This will lead to bitwise-level
      // differences between Renjin and GNU R that cause problems
      // with digests, etc.
      int encoding = (string.length() == bytes.length) ? ASCII_MASK : UTF8_MASK;

      out.writeInt(Flags.computeCharSexpFlags(encoding));
      out.writeInt(bytes.length);
      out.writeString(bytes);
    }
  }

  private void writeAttributes(Context contextEnv, SEXP exp) throws IOException {
    
    PairList attributes = exp.getAttributes().asPairList();

    if(exp.getAttributes() != AttributeMap.EMPTY && attributes == Null.INSTANCE) {
      throw new IllegalStateException("exp != AttributeMap.EMPTY but has no attributes");
    }
    
    if(attributes != Null.INSTANCE) {
      if(!(attributes instanceof PairList.Node)) {
        throw new AssertionError(attributes.getClass());
      }
      writeExp(contextEnv, attributes);
    }
  }

  private void writeTag(Context contextEnv, PairList.Node node) throws IOException {
    if(node.hasTag()) {
      writeExp(contextEnv, node.getTag());
    }
  }

  private void writePrimitive(PrimitiveFunction exp) throws IOException {
    if(exp instanceof BuiltinFunction) {
      out.writeInt(SexpType.BUILTINSXP);
    } else {
      out.writeInt(SexpType.SPECIALSXP);
    }
    out.writeInt(exp.getName().length());
    conn.writeBytes(exp.getName());
  }

  
  private void writeFlags(int type, SEXP exp) throws IOException {
    out.writeInt(Flags.computeFlags(exp, type));
  }

  private interface StreamWriter extends AutoCloseable {
    void writeInt(int v) throws IOException;
    void writeString(byte[] bytes) throws IOException;
    void writeLong(long l) throws IOException;
    void writeDouble(double d) throws IOException;

    @Override
    void close() throws IOException;
  }

  private static class AsciiWriter implements StreamWriter {
    private DataOutputStream out;
    
    private AsciiWriter(DataOutputStream out) {
      this.out = out;
    }
    
    public void writeInt(int v) throws IOException {
      out.writeBytes(v + "\n");
    }
    
    public void writeDouble(double d) throws IOException {
      out.writeBytes(d + "\n");
    }
    
    public void writeLong(long l) throws IOException {
      out.writeBytes(l + "\n");
    }

    public void writeString(byte[] bytes) throws IOException {
      for(int i = 0; i < bytes.length; i++) {
        String s;
        switch(bytes[i]) {
          case '\n': s = "\\n";  break;
          case '\t': s = "\\t";  break;
          case '\013': s = "\\v";  break;
          case '\b': s = "\\b";  break;
          case '\r': s = "\\r";  break;
          case '\f': s = "\\f";  break;
          case '\007': s = "\\a";  break;
          case '\\': s = "\\\\"; break;
          case '\177': s = "\\?";  break;
          case '\'': s = "\\'";  break;
          case '\"': s = "\\\""; break;
          default  :
        /* cannot print char in octal mode -> cast to unsigned
           char first */
        /* actually, since s is signed char and '\?' == 127
           is handled above, s[i] > 126 can't happen, but
           I'm superstitious...  -pd */
            if (bytes[i] <= 32 || bytes[i] > 126) {
              s = String.format("\\%03o", bytes[i]);
            } else {
              s = new String(new byte[]{bytes[i]});
            }
        }
        out.writeBytes(s);
      }
      out.writeBytes("\n");
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }
  
  private static class XdrWriter implements StreamWriter {
    private DataOutputStream out;
      
    private XdrWriter(DataOutputStream out) {
      this.out = out;
    }
      
    public void writeInt(int v) throws IOException {
      out.writeInt(v);
    }
      
    public void writeDouble(double d) throws IOException {
      out.writeDouble(d);
    }
      
    public void writeLong(long l) throws IOException {
      out.writeLong(l);
    }
      
    public void writeString(byte[] bytes) throws IOException {
      out.write(bytes);
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }
}
