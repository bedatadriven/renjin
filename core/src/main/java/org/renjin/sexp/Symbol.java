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

package org.renjin.sexp;

import java.util.HashMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public final class Symbol extends AbstractSEXP {

  public static final String TYPE_NAME = "symbol";
  public static final String IMPLICIT_CLASS = "name";

  /**
   * The global symbol table. We store symbols here so that
   * we can compare symbols using reference equality (==) rather than
   * the equals() method.
   */
  private static final HashMap<String, Symbol> TABLE;

  public static final Symbol UNBOUND_VALUE = new Symbol();
  
  public static final Symbol MISSING_ARG = new Symbol();
 
  /**
   * The symbol's name 
   */
  private final String printName;
  
  /**
   * A hash of this symbol's name.
   */
  private final int hashBit;
  
  /**
   * Hash bit for very frequently used and very rarely redefined 
   * primitives. 
   */
  private static final int NUM_RESERVED_BITS = 4;
  
  static { 
    TABLE = Maps.newHashMap();
    addReserved(0, 
        "if", 
        ".Internal",   
        "function",
        "while",
        "for",
        "break",
        "continue",
        "repeat",
        "next",
        "{", "(", 
        "!",
        ":",
        "=", "!=", "==",
        ">", "<", ">=", "<=", "&", "|", "&&", "||",
        "+", "-", "*", "/", "^",
        "<-", "<<-",
        "[", "[<-", "[[", "[[<-", "$", "$<-",
        "%*%", "%/%", "%%");
    
    addReserved(1,
        "paste",
        "identical",
        "list",
        "c",
        "as.character",
        "is.character",
        "as.integer",
        "is.integer",
        "is.na",
        "dim",
        "is.null",
        "is.expression",
        "is.call",
        "is.na",
        "is.numeric",
        "is.logical",
        "as.vector",
        "is.factor",
        "is.matrix",
        "is.pairlist",
        "is.object",
        "is.function",
        "is.vector",
        "is.complex",
        "is.double",
        "is.list",
        "switch",
        "typeof",
        "seq_along",
        "lapply",
        "sapply",
        "Encoding<-",
        "class<-",
        "names<-",
        ".subset",
        ".subset2",
        "UseMethod",
        "NextMethod",
        "sys.call"); 
    addReserved(2, 
        "length",
        "mode",
        "length",
        "any",
        "attributes",
        "matrix",
        "missing",
        "names",
        "inherits",
        "attr",
        "match",
        "which");
    addReserved(3, 
        "factor",
        "vector",
        "integer",
        "assign",
        "exists",
        "mean",
        "abs",
        "sum",
        "all",
        "any",
        "oldClass",
        "data.class",
        "attr.all.equal",
        "nargs",
        "unique",
        "formals");
  }
  
  private static void addReserved(int hashBit, String... names) {
    for(String name : names) {
      TABLE.put(name, new Symbol(name, 1<<hashBit));
    }
  }
  
  /**
   * Obtains a reference to a Symbol from the global Symbol table.
   * 
   * @param printName the symbol's name
   * @return a global environment
   */
  public static Symbol get(String printName) {
    Preconditions.checkNotNull(printName);

    synchronized (TABLE) {
      Symbol symbol = TABLE.get(printName);
      if(symbol == null) {
        symbol = new Symbol(printName, calcHashBit(printName));
        TABLE.put(printName, symbol);
      }
      return symbol;
    }
  }

  private Symbol() {
    this.printName = null;
    this.hashBit = NUM_RESERVED_BITS;
  }
  
  private Symbol(String printName, int hashBits) {
    this.printName = printName;
    this.hashBit = hashBits;
  }
  
  private static int calcHashBit(String printName) {
    int firstChar = printName.codePointAt(0);   
  
    // hash by the first char 
    if(firstChar >= 'a' && firstChar <= 'z') {
      return 1 << ((firstChar-'a')+NUM_RESERVED_BITS+1);
    } else {
      return 1 << (NUM_RESERVED_BITS);
    }
  }

  public boolean isReservedWord() {
    return hashBit == 1;
  }
  
  /**
   * Maps this symbol to a single bit in 32-bit hash bitset.
   * 
   * @return
   */
  public int hashBit() {
    return hashBit;
  }
  
  @Override
  public int hashCode() {
    return hashBit;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public final String getImplicitClass() {
    return IMPLICIT_CLASS;
  }
  
  

  @Override
  protected SEXP cloneWithNewAttributes(AttributeMap attributes) {
    // it seems very strange that you can change attributes on a 
    // globally shared object, but that seems to be the case in R
    this.attributes = attributes;
    return this;
  }

  public String getPrintName() {
    return printName;
  }
  
  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    if (this == UNBOUND_VALUE) {
      return "<unbound>";
    } else if (this == MISSING_ARG) {
      return "<missing_arg>";
    } else {
      return getPrintName();
    }
  }
}
