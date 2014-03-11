/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.io.serialization;

import static org.renjin.primitives.io.serialization.SerializationFormat.CHARSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.EXTPTRSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.INTSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.LGLSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.LISTSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.NILVALUE_SXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.RAWSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.REALSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.S4SXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.STRSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.SYMSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.UTF8_MASK;
import static org.renjin.primitives.io.serialization.SerializationFormat.VECSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.VERSION2;
import static org.renjin.primitives.io.serialization.SerializationFormat.XDR_FORMAT;
import static org.renjin.primitives.io.serialization.SerializationFormat.XDR_MAGIC_HEADER;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.sexp.BuiltinFunction;
import org.renjin.sexp.Closure;
import org.renjin.sexp.ComplexVector;
import org.renjin.sexp.DoubleVector;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ExternalPtr;
import org.renjin.sexp.FunctionCall;
import org.renjin.sexp.IntVector;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.LogicalVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.PrimitiveFunction;
import org.renjin.sexp.Promise;
import org.renjin.sexp.RawVector;
import org.renjin.sexp.S4Object;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbol;
import org.renjin.sexp.Vector;

import com.google.common.collect.Maps;

public class RASCWriter {


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
  private DataOutputStream out;

  private Map<SEXP, Integer> references = Maps.newHashMap();

  public RASCWriter(WriteContext context, PersistenceHook hook, OutputStream out) {
    this.context = context;
    this.hook = hook;
    this.out = new DataOutputStream(out);
  }

  public RASCWriter(Context context, PersistenceHook hook, OutputStream out) throws IOException {
    this(new SessionWriteContext(context.getSession()), hook, out);
  }

  public RASCWriter(Context context, OutputStream out) throws IOException {
    this(context, null, out);
  }


  public RASCWriter(WriteContext writeContext, OutputStream os) {
    this(writeContext, null, os);
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
    out.writeBytes(XDR_MAGIC_HEADER);
    serialize(sexp);
  }

  public void serialize(SEXP exp) throws IOException {
    out.writeByte(SerializationFormat.ASCII_FORMAT);
    out.writeByte('\n');
    writeVersion();
    writeExp(exp);
  }
    
  private void writeVersion() throws IOException {
    out.writeBytes(VERSION2 + "\n");
    out.writeBytes(Version.CURRENT.asPacked() + "\n");
    out.writeBytes(new Version(2,3,0).asPacked() + "\n");
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
    if( context.isNamespaceEnvironment((Environment) exp)) {
      return true;
    }
    if( isPackageEnvironment(exp)) {
      return true;
    }
    return false;
  }

  private boolean isPackageEnvironment(SEXP exp) {
    // TODO 
    return false;
  }

  private void writeNull() throws IOException {
    out.writeBytes(NILVALUE_SXP + "\n");
  }

  private void writeLogical(LogicalVector vector) throws IOException {
    writeFlags(LGLSXP, vector);
    out.writeBytes(vector.length() + "\n");
    for(int i=0;i!=vector.length();++i) {
      out.writeBytes(vector.getElementAsRawLogical(i) + "\n");
    }
    writeAttributes(vector);
  }

  private void writeIntVector(IntVector vector) throws IOException {
    writeFlags(INTSXP, vector);
    out.writeBytes(vector.length() + "\n");
    for(int i=0;i!=vector.length();++i) {
      if(vector.isElementNA(i)) {
        out.writeBytes("NA\n");
      } else {
        out.writeBytes(vector.getElementAsInt(i) + "\n");
      }
    }
    writeAttributes(vector);
  }

  private void writeDoubleVector(DoubleVector vector) throws IOException {
    writeFlags(REALSXP, vector);
    out.writeBytes(vector.length() + "\n");
    for(int i=0;i!=vector.length();++i) {
      double d = vector.getElementAsDouble(i);
      if(!DoubleVector.isFinite(d)) {
        if(DoubleVector.isNaN(d)) {
          out.writeBytes("NA\n");
        } else if (d < 0) {
          out.writeBytes("-Inf\n");
        } else {
          out.writeBytes("Inf\n");
        }
      } else {
        out.writeBytes(d + "\n");
      }
    }
    writeAttributes(vector);
  }


  private void writeS4(S4Object exp) throws IOException {
    writeFlags(S4SXP, exp);
    writeAttributes(exp);
  }

  private void writeExternalPtr(ExternalPtr exp) throws IOException {
    addRef(exp);
    writeFlags(EXTPTRSXP, exp);
    writeExp(Null.INSTANCE); // protected value (not currently used)
    writeExp(Null.INSTANCE); // tag (not currently used)
    writeAttributes(exp);
  }

  private void writeComplexVector(ComplexVector vector) throws IOException {
    writeFlags(SerializationFormat.CPLXSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      Complex value = vector.getElementAsComplex(i);
      out.writeDouble(value.getReal());
      out.writeDouble(value.getImaginary());
    }
    writeAttributes(vector);
  }

  private void writeRawVector(RawVector vector) throws IOException {
    writeFlags(RAWSXP, vector);
    out.writeInt(vector.length());
    out.write(vector.getAsByteArray());    
    writeAttributes(vector);
  }
  
  private void writeStringVector(StringVector vector) throws IOException {
    writeFlags(STRSXP, vector);
    out.writeBytes(vector.length() + "\n");
    for(int i=0;i!=vector.length();++i) {
      writeCharExp(vector.getElementAsString(i));
    }
    writeAttributes(vector);
  }

  private void writeList(ListVector vector) throws IOException {
    writeFlags(VECSXP, vector);
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
    writeExp(exp.getExpression());  
  }

  private void writePairList(PairList.Node node) throws IOException {
    writeFlags(LISTSXP, node);
    writeAttributes(node);
    writeTag(node);
    writeExp(node.getValue());
    if(node.hasNextNode()) {
      writeExp(node.getNextNode());
    } else {
      writeNull();
    }
  }

  private void writeFunctionCall(FunctionCall exp) throws IOException {
    writeFlags(SerializationFormat.LANGSXP, exp);
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
    writeFlags(SerializationFormat.CLOSXP, exp);
    writeAttributes(exp);
    writeExp(exp.getEnclosingEnvironment());
    writeExp(exp.getFormals());
    writeExp(exp.getBody());
  }
  
  private void writeEnvironment(Environment env) throws IOException {
    // add reference FIRST to avoid infinite loops

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
        writeFlags(SerializationFormat.ENVSXP, env);
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
    PairList.Builder frame = new PairList.Builder();
    for(Symbol name : exp.getSymbolNames()) {
      frame.add(name, exp.getVariable(name));
    }
    writeExp(frame.build());
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
      out.writeInt(SerializationFormat.REFSXP);
      out.writeInt(index);
    } else {
      out.writeInt(SerializationFormat.REFSXP | (index << 8));
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
      writeFlags(SYMSXP, symbol);
      writeCharExp(symbol.getPrintName());
    }
  }


  private void writeCharExp(String string) throws IOException {
    out.writeBytes( (CHARSXP | UTF8_MASK) + "\n");
    if(StringVector.isNA(string)) {
      out.writeBytes(-1 + "\n");
    } else {
      byte[] bytes = string.getBytes("UTF8");
      out.writeBytes(bytes.length + "\n");
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
        if (bytes[i] <= 32 || bytes[i] > 126)
            s = String.format("\\%03o", bytes[i]);
        else
            s = new String(new byte[] {bytes[i]});
        }
        out.writeBytes(s);
      }
      out.writeBytes("\n");
    }
  }

  private void writeAttributes(SEXP exp) throws IOException {
    PairList attributes = exp.getAttributes().asPairList();
    if(attributes != Null.INSTANCE) {
      if(!(attributes instanceof PairList.Node)) {
        throw new AssertionError(attributes.getClass());
      }
      writeExp(attributes);
    }
  }

  private void writeTag(PairList.Node node) throws IOException {
    if(node.hasTag()) {
      writeExp(node.getTag());
    }
  }

  private void writePrimitive(PrimitiveFunction exp) throws IOException {
    if(exp instanceof BuiltinFunction) {
      out.writeInt(SerializationFormat.BUILTINSXP);
    } else {
      out.writeInt(SerializationFormat.SPECIALSXP);
    }
    out.writeInt(exp.getName().length());
    out.writeBytes(exp.getName());
  }

  
  private void writeFlags(int type, SEXP exp) throws IOException {
    out.writeBytes(Flags.computeFlags(exp, type) + "\n");
  }

}
