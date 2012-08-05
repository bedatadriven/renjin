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

import com.google.common.collect.Maps;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.primitives.Namespaces;
import org.renjin.sexp.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import static org.renjin.primitives.io.serialization.SerializationFormat.*;

public class RDataWriter {

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
  
  private Context context;
  private PersistenceHook hook;
  private DataOutputStream out;

  private Map<SEXP, Integer> references = Maps.newHashMap();

  public RDataWriter(Context context, PersistenceHook hook, OutputStream out) throws IOException {
    this.context = context;
    this.hook = hook;
    this.out = new DataOutputStream(out);
  }

  public RDataWriter(Context context, OutputStream out) throws IOException {
    this(context, null, out);
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
    out.writeByte(XDR_FORMAT);
    out.writeByte('\n');
    writeVersion();
    writeExp(exp);
  }
    
  private void writeVersion() throws IOException {
    out.writeInt(VERSION2);
    out.writeInt(Version.CURRENT.asPacked());
    out.writeInt(new Version(2,3,0).asPacked());
  }

  private void writeExp(SEXP exp) throws IOException {
    
    if(tryWritePersistent(exp)) {
      return;
    }
    
    if(tryWriteRef(exp)) {
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
    if( exp == context.getEnvironment().getBaseEnvironment() ) {
      return true;
    }
    if( Namespaces.isNamespaceEnv(context, exp)) {
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
    out.writeInt(NILVALUE_SXP);
  }

  private void writeLogical(LogicalVector vector) throws IOException {
    writeFlags(LGLSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      out.writeInt(vector.getElementAsRawLogical(i));
    }
    writeAttributes(vector);
  }

  private void writeIntVector(IntVector vector) throws IOException {
    writeFlags(INTSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      out.writeInt(vector.getElementAsInt(i));
    }
    writeAttributes(vector);
  }

  private void writeDoubleVector(DoubleVector vector) throws IOException {
    writeFlags(REALSXP, vector);
    out.writeInt(vector.length());
    for(int i=0;i!=vector.length();++i) {
      if(vector.isElementNA(i)) {
        out.writeLong(SerializationFormat.XDR_NA_BITS);
      } else {
        out.writeDouble(vector.getElementAsDouble(i));
      }
    }
    writeAttributes(vector);
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
    out.writeInt(vector.length());
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

    if(env == context.getGlobalEnvironment()) {
      out.writeInt(SerializationFormat.GLOBALENV_SXP);
    } else if(env == context.getGlobalEnvironment().getBaseEnvironment()) {
      out.writeInt(SerializationFormat.BASEENV_SXP);
    } else if(env == Environment.EMPTY) {
      out.writeInt(SerializationFormat.EMPTYENV_SXP);
    } else {      
      if(Namespaces.isNamespaceEnv(context, env)) {
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
        writeExp(env.getAttributes());
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
    if(ns == context.getGlobals().namespaceRegistry.getVariable(Symbol.get("base"))) {
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
    Environment info = (Environment) ns.getVariable(".__NAMESPACE__.");
    StringVector spec = (StringVector) info.getVariable("spec");
    return (StringVector) spec.getElementAsSEXP(0);
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
    out.writeInt( CHARSXP | UTF8_MASK );
    if(StringVector.isNA(string)) {
      out.writeInt(-1);
    } else {
      byte[] bytes = string.getBytes("UTF8");
      out.writeInt(bytes.length);
      out.write(bytes);
    }
  }

  private void writeAttributes(SEXP exp) throws IOException {
    PairList attributes = exp.getAttributes();
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
    out.writeInt(Flags.computeFlags(exp, type));
  }
}
