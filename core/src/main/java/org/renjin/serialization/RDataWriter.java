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
package org.renjin.serialization;

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.collect.Maps;
import org.renjin.repackaged.guava.io.LittleEndianDataOutputStream;
import org.renjin.serialization.Serialization.SerializationType;
import org.renjin.sexp.*;

import java.io.*;
import java.nio.ByteOrder;
import java.util.Map;

import static org.renjin.serialization.SerializationFormat.*;

/**
 * Writes R data object to an {@code OutputStream}.
 */
public class RDataWriter implements AutoCloseable {

  public static final int SERIALIZATION_VERSION = VERSION2;

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
  private StreamWriter out;
  private SerializationType serializationType;

  private Map<SEXP, Integer> references = Maps.newIdentityHashMap();

  public RDataWriter(WriteContext context, PersistenceHook hook, OutputStream out,
                     SerializationType type) {
    this.context = context;
    this.hook = hook;
    this.serializationType = type;
    switch(this.serializationType) {
      case ASCII:
        this.out = new AsciiWriter(out);
        break;
      case BINARY:
        this.out = new BinaryWriter(out, ByteOrder.nativeOrder());
        break;
      case XDR:
        this.out = new BinaryWriter(out, ByteOrder.BIG_ENDIAN);
        break;
      default:
        throw new UnsupportedOperationException("Unsupported format: " + this.serializationType);
    }
  }
  
  public RDataWriter(Context context, PersistenceHook hook, OutputStream out) {
    this(new SessionWriteContext(context), hook, out, SerializationType.XDR);
  }

  public RDataWriter(Context context, OutputStream out, SerializationType st) {
    this(new SessionWriteContext(context), null, out, st);
  }
  
  public RDataWriter(Context context, OutputStream out) throws IOException {
    this(context, null, out);
  }

  public RDataWriter(WriteContext writeContext, OutputStream os) {
    this(writeContext, null, os, SerializationType.XDR);
  }

  public RDataWriter(OutputStream os) {
    this(HeadlessWriteContext.INSTANCE, os);
  }


  /**
   * @deprecated Call save() explicitly
   * @param sexp
   * @throws IOException 
   */
  @Deprecated
  public void writeFile(SEXP sexp) throws IOException {
    save(sexp);
  }

  /**
   * Serializes the given {@code sexp}, prefixed by the 
   * magic bytes 'RDX\n'
   * @throws IOException
   */
  public void save(SEXP sexp) throws IOException {
    if(serializationType == SerializationType.ASCII) {
      out.writeAsciiBytes(ASCII_MAGIC_HEADER);
    } else if(serializationType == SerializationType.BINARY) {
      out.writeAsciiBytes(BINARY_MAGIC_HEADER);
    } else {
      out.writeAsciiBytes(XDR_MAGIC_HEADER);
    }
    
    serialize(sexp);
  }

  public void serialize(SEXP exp) throws IOException {
    if(serializationType == SerializationType.ASCII) {
      out.writeAsciiByte((char) ASCII_FORMAT);
    } else if(serializationType == SerializationType.BINARY) {
      out.writeAsciiByte((char)BINARY_FORMAT);
    } else {
      out.writeAsciiByte((char)XDR_FORMAT);
    }
    out.writeAsciiByte('\n');
    writeVersion();
    writeExp(exp);
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

  private void writeExp(SEXP exp) throws IOException {
    if(tryWriteRef(exp)) {
      return;
    }
    
    if(tryWritePersistent(exp)) {
      return;
    }
     
    if(exp instanceof Null) {
      writeNull();
    } else if(exp instanceof LogicalVector) {
      writeLogical((LogicalVector) exp);
    } else if(exp instanceof IntVector) {
      writeIntVector((IntVector) exp);
    } else if(exp instanceof DoubleVector) {
      writeDoubleVector((DoubleVector) exp);
    } else if(exp instanceof StringVector) {
      writeStringVector((StringVector) exp);
    } else if(exp instanceof ComplexVector) {
      writeComplexVector((ComplexVector)exp);
    } else if(exp instanceof Promise) {
      writePromise((Promise)exp);
    } else if(exp instanceof ListVector) {
      writeList((ListVector) exp);
    } else if(exp instanceof FunctionCall) {
      writeFunctionCall((FunctionCall)exp);
    } else if(exp instanceof PairList.Node) {
      writePairList((PairList.Node) exp);
    } else if(exp instanceof Symbol) {
      writeSymbol((Symbol) exp);
    } else if(exp instanceof Closure) {
      writeClosure((Closure)exp);
    } else if(exp instanceof RawVector) {
      writeRawVector((RawVector) exp);
    } else if(exp instanceof Environment) {
      writeEnvironment((Environment)exp);
    } else if(exp instanceof PrimitiveFunction) {
      writePrimitive((PrimitiveFunction)exp);
    } else if(exp instanceof S4Object) {
      writeS4((S4Object)exp);
    } else if(exp instanceof ExternalPtr) {
      writeExternalPtr((ExternalPtr)exp);
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

  private void writeLogical(LogicalVector vector) throws IOException {
    writeFlags(SexpType.LGLSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      out.writeInt(vector.getElementAsRawLogical(i));
    }
    writeAttributes(vector);
  }

  private void writeIntVector(IntVector vector) throws IOException {
    writeFlags(SexpType.INTSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      for(int i=0;i!=vector.length();++i) {
        if(vector.isElementNA(i)) {
          out.writeAsciiBytes("NA\n");
        } else {
          out.writeInt(vector.getElementAsInt(i));
        }
      }
    } else {
      for(int i=0;i!=vector.length();++i) {
        out.writeInt(vector.getElementAsInt(i));
      }
    }
    
    writeAttributes(vector);
  }

  private void writeDoubleVector(DoubleVector vector) throws IOException {
    writeFlags(SexpType.REALSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      for(int i=0;i!=vector.length();++i) {
        double d = vector.getElementAsDouble(i);
        if(!Double.isFinite(d)) {
          if(DoubleVector.isNaN(d)) {
            out.writeAsciiBytes("NA\n");
          } else if (d < 0) {
            out.writeAsciiBytes("-Inf\n");
          } else {
            out.writeAsciiBytes("Inf\n");
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
    
    writeAttributes(vector);
  }


  private void writeS4(S4Object exp) throws IOException {
    writeFlags(SexpType.S4SXP, exp);
    writeAttributes(exp);
  }

  private void writeExternalPtr(ExternalPtr exp) throws IOException {
    addRef(exp);
    writeFlags(SexpType.EXTPTRSXP, exp);
    writeExp(Null.INSTANCE); // protected value (not currently used)
    writeExp(Null.INSTANCE); // tag (not currently used)
    writeAttributes(exp);
  }

  private void writeComplexVector(ComplexVector vector) throws IOException {
    writeFlags(SexpType.CPLXSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      Complex value = vector.getElementAsComplex(i);
      out.writeDouble(value.getReal());
      out.writeDouble(value.getImaginary());
    }
    writeAttributes(vector);
  }

  private void writeRawVector(RawVector vector) throws IOException {
    writeFlags(SexpType.RAWSXP, vector);
    out.writeInt(vector.length());
    if(serializationType == SerializationType.ASCII) {
      byte[] bytes = vector.toByteArray();
      for(int i=0;i!=vector.length();++i) {
        out.writeAsciiBytes(String.format("%02x\n", bytes[i]));
      }
    } else {
      out.writeString(vector.toByteArray());
    }
    writeAttributes(vector);
  }
  
  private void writeStringVector(StringVector vector) throws IOException {
    writeFlags(SexpType.STRSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      writeCharExp(vector.getElementAsString(i));
    }
    writeAttributes(vector);
  }

  private void writeList(ListVector vector) throws IOException {
    writeFlags(SexpType.VECSXP, vector);
    out.writeInt(vector.length());
    for(SEXP element : vector) {
      writeExp(element);
    }
    writeAttributes(vector);
  }

  private void writePromise(Promise exp) throws IOException {
    out.writeInt(Flags.computePromiseFlags(exp));
    writeAttributes(exp);
    if(exp.getEnvironment() != null) {
      writeExp(exp.getEnvironment());
    }
    writeExp(exp.getValue() == null ? Null.INSTANCE : exp.getValue());
    writeExp(exp.getPromisedExpression());
  }

  private void writePairList(PairList.Node node) throws IOException {

    while(true) {
      writeFlags(SexpType.LISTSXP, node);
      writeAttributes(node);
      writeTag(node);
      writeExp(node.getValue());

      if(node.getNext() == Null.INSTANCE) {
        writeNull();
        break;
      }
      node = node.getNextNode();
    }
  }

  private void writeFunctionCall(FunctionCall exp) throws IOException {
    writeFlags(SexpType.LANGSXP, exp);
    writeAttributes(exp);
    writeTag(exp);
    writeExp(exp.getValue());
    if(exp.hasNextNode()) {
      writeExp(exp.getNextNode());
    } else {
      writeNull();
    }
  }

  private void writeClosure(Closure exp) throws IOException {
    writeFlags(SexpType.CLOSXP, exp);
    writeAttributes(exp);
    writeExp(exp.getEnclosingEnvironment());
    writeExp(exp.getFormals());
    writeExp(exp.getBody());
  }
  
  private void writeEnvironment(Environment env) throws IOException {

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
        writeExp(env.getParent());
        writeFrame(env);
        writeExp(Null.INSTANCE); // hashtab (unused)
        
        // NB: attributes for an environment are
        // ALWAYS written, even if NULL
        writeExp(env.getAttributes().asPairList());
      }
    }
  }
  
  private void writeFrame(Environment exp) throws IOException {
    for(Symbol name : exp.getSymbolNames()) {
      if(exp.isActiveBinding(name)) {
        out.writeInt(Flags.computeBindingFlag(true));
        writeExp(name);
        writeExp(exp.getActiveBinding(name));
      } else {
        out.writeInt(Flags.computeBindingFlag(false));
        writeExp(name);
        writeExp(exp.getVariableUnsafe(name));
      }
    }
    writeNull();
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

  private void writeAttributes(SEXP exp) throws IOException {

    if(Flags.hasAttributesToWrite(exp)) {

      PairList.Builder pairList = new PairList.Builder();
      exp.getAttributes().forEach((name, value) -> {

        // To preserve byte-for-byte compatibility with GNU R, we must mimic
        // the row.names hack, where row.names can be stored as c(NA_integer_, -rows)

        if (name == Symbols.ROW_NAMES && value instanceof RowNamesVector) {
          RowNamesVector rn = (RowNamesVector) value;
          pairList.add(Symbols.ROW_NAMES, new IntArrayVector(IntVector.NA, -rn.length()));
        } else {
          pairList.add(name, value);
        }
      });

      writeExp(pairList.build());
    }
  }

  private void writeTag(PairList.Node node) throws IOException {
    if(node.hasTag()) {
      writeExp(node.getTag());
    }
  }

  private void writePrimitive(PrimitiveFunction exp) throws IOException {
    if(exp instanceof BuiltinFunction) {
      out.writeInt(SexpType.BUILTINSXP);
    } else {
      out.writeInt(SexpType.SPECIALSXP);
    }
    out.writeInt(exp.getName().length());
    out.writeAsciiBytes(exp.getName());
  }

  
  private void writeFlags(int type, SEXP exp) throws IOException {
    out.writeInt(Flags.computeFlags(exp, type));
  }

  private interface StreamWriter extends AutoCloseable {
    void writeInt(int v) throws IOException;
    void writeString(byte[] bytes) throws IOException;
    void writeLong(long l) throws IOException;
    void writeDouble(double d) throws IOException;
    void writeAsciiBytes(String s) throws IOException;
    void writeAsciiByte(char c) throws IOException;

    @Override
    void close() throws IOException;

  }

  private static class AsciiWriter implements StreamWriter {
    private OutputStream out;

    private AsciiWriter(OutputStream out) {
      this.out = out;
    }

    public void writeAsciiBytes(String s) throws IOException {
      out.write(s.getBytes(Charsets.US_ASCII));
    }

    @Override
    public void writeAsciiByte(char c) throws IOException {
      writeAsciiBytes("" + c);
    }

    public void writeInt(int v) throws IOException {
      writeAsciiBytes(v + "\n");
    }
    
    public void writeDouble(double d) throws IOException {
      writeAsciiBytes(d + "\n");
    }
    
    public void writeLong(long l) throws IOException {
      writeAsciiBytes(l + "\n");
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
        writeAsciiBytes(s);
      }
      writeAsciiBytes("\n");
    }

    @Override
    public void close() throws IOException {
      out.close();
    }
  }

  private static class BinaryWriter implements StreamWriter {
    private DataOutput out;

    public BinaryWriter(OutputStream output, ByteOrder order) {
      if(order == ByteOrder.BIG_ENDIAN) {
        this.out = new DataOutputStream(output);
      } else {
        this.out = new LittleEndianDataOutputStream(output);
      }
    }

    public void writeInt(int v) throws IOException {
      out.writeInt(v);
    }
      
    public void writeDouble(double d) throws IOException {
      out.writeDouble(d);
    }

    @Override
    public void writeAsciiBytes(String s) throws IOException {
      out.write(s.getBytes(Charsets.US_ASCII));
    }

    @Override
    public void writeAsciiByte(char c) throws IOException {
      writeAsciiBytes("" + c);
    }

    public void writeLong(long l) throws IOException {
      out.writeLong(l);
    }
      
    public void writeString(byte[] bytes) throws IOException {
      out.write(bytes);
    }

    @Override
    public void close() throws IOException {
      ((Closeable)out).close();
    }
  }
}
