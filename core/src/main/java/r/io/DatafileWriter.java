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

package r.io;

import static r.io.SerializationFormat.CHARSXP;
import static r.io.SerializationFormat.INTSXP;
import static r.io.SerializationFormat.LGLSXP;
import static r.io.SerializationFormat.LISTSXP;
import static r.io.SerializationFormat.NILVALUE_SXP;
import static r.io.SerializationFormat.REALSXP;
import static r.io.SerializationFormat.STRSXP;
import static r.io.SerializationFormat.SYMSXP;
import static r.io.SerializationFormat.UTF8_MASK;
import static r.io.SerializationFormat.VECSXP;
import static r.io.SerializationFormat.VERSION2;
import static r.io.SerializationFormat.RAWSXP;
import static r.io.SerializationFormat.XDR_FORMAT;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import r.lang.DoubleVector;
import r.lang.IntVector;
import r.lang.ListVector;
import r.lang.LogicalVector;
import r.lang.Null;
import r.lang.PairList;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbol;
import r.lang.RawVector;

public class DatafileWriter {

  private DataOutputStream out;

  public DatafileWriter(OutputStream out) throws IOException {
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
    } else if(exp instanceof PairList.Node){
      writePairList((PairList.Node) exp);
    } else if(exp instanceof Symbol) {
      writeSymbol((Symbol) exp);
    } else if(exp instanceof RawVector) {
      writeRawVector((RawVector) exp);
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
    for(int i=0;i!=vector.length();++i) {
      if(vector.isElementNA(i)) {
        out.writeLong(SerializationFormat.XDR_NA_BITS);
      } else {
        out.write(vector.getElement(i).getAsByte());
      }
    }
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
