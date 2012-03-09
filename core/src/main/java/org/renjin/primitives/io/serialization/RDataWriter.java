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
import static org.renjin.primitives.io.serialization.SerializationFormat.INTSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.LGLSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.LISTSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.NILVALUE_SXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.RAWSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.REALSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.STRSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.SYMSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.UTF8_MASK;
import static org.renjin.primitives.io.serialization.SerializationFormat.VECSXP;
import static org.renjin.primitives.io.serialization.SerializationFormat.VERSION2;
import static org.renjin.primitives.io.serialization.SerializationFormat.XDR_FORMAT;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.renjin.primitives.Namespaces;

import com.google.common.collect.Maps;

import r.lang.Closure;
import r.lang.Context;
import r.lang.DoubleVector;
import r.lang.Environment;
import r.lang.FunctionCall;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.RawVector;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;

public class RDataWriter {

  private Context context;
  private DataOutputStream out;

  private Map<SEXP, Integer> references = Maps.newHashMap();

  public RDataWriter(Context context, OutputStream out) throws IOException {
    this.context = context;
    this.out = new DataOutputStream(out);
    writeHeader();
  }

  private void writeHeader() throws IOException {
    out.writeBytes(XDR_FORMAT);
    out.writeInt(VERSION2);
    out.writeInt(new Version(2,10,1).asPacked());
    out.writeInt(new Version(2,3,0).asPacked());
  }

  public void writeExp(SEXP exp) throws IOException {
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
    } else if(exp instanceof ListVector) {
      writeList((ListVector) exp);
    } else if(exp instanceof FunctionCall) {
      writeFunctionCall((FunctionCall)exp);
    } else if(exp instanceof PairList.Node){
      writePairList((PairList.Node) exp);
    } else if(exp instanceof Symbol) {
      writeSymbol((Symbol) exp);
    } else if(exp instanceof Closure) {
      writeClosure((Closure)exp);
    } else if(exp instanceof RawVector) {
      writeRawVector((RawVector) exp);
    } else if(exp instanceof Environment) {
      writeEnvironment((Environment)exp);
    } else {
      throw new UnsupportedOperationException("serialization of " + exp.getClass().getName() + " not implemented");
    }
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
  
  
  private void writeRawVector(RawVector vector) throws IOException {
    writeFlags(RAWSXP, vector);
    out.writeInt(vector.length());
    out.write(vector.getAsByteArray());    
    writeAttributes(vector);
  }
  
  private void writeStringVector(String element) throws IOException {
    writeStringVector(new StringVector(element));
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
    if(env == context.getGlobalEnvironment()) {
      out.writeInt(SerializationFormat.GLOBALENV_SXP);
    } else if(env == context.getGlobalEnvironment().getBaseEnvironment()) {
      out.writeInt(SerializationFormat.BASEENV_SXP);
    } else if(env == Environment.EMPTY) {
      out.writeInt(SerializationFormat.EMPTYENV_SXP);
    } else if(Namespaces.isNamespaceEnv(context, env)) {
      writeNamespace(env);
    } else {
      
      if(!writeRef(env)) {
        writeFlags(SerializationFormat.ENVSXP, env);
        writeExp(env.getParent());
        writeFrame(env);
        writeExp(Null.INSTANCE); // hashtab (unused)
        writeExp(env.getAttributes());
        addRef(env);
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
      if(!writeRef(ns)) {
        writeFlags(SerializationFormat.NAMESPACESXP, ns);
        writeStringVector(getNamespaceName(ns));
        addRef(ns);
      }
    }
  }

  private boolean writeRef(SEXP ns) throws IOException {
    if(references.containsKey(ns)) {
      writeRefIndex(references.get(ns));
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

  private String getNamespaceName(Environment ns) {
    Environment info = (Environment) ns.getVariable(".__NAMESPACE__.");
    StringVector spec = (StringVector) info.getVariable("spec");
    return spec.getElementAsString(0);
  }

  private void writeSymbol(Symbol symbol) throws IOException {
    writeFlags(SYMSXP, symbol);
    writeCharExp(symbol.getPrintName());
//
//  CHARSEXP printName = (CHARSEXP) readExp();
//    return addReadRef( new Symbol( printName.getValue()) );
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
    SEXP attributes = exp.getAttributes();
    if(attributes != Null.INSTANCE) {
      writeExp(attributes);
    }
  }

  private void writeTag(PairList.Node node) throws IOException {
    if(node.hasTag()) {
      writeExp(node.getTag());
    }
  }

  private void writeFlags(int type, SEXP exp) throws IOException {
    out.writeInt(Flags.computeFlags(exp, type));
  }
}
