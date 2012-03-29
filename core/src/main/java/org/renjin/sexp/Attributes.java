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

import org.renjin.eval.EvalException;

/**
 * 
 * Attributes are an R language feature that allow metadata to be attached to 
 * R-language expressions.
 * 
 * Certain attributes have special meaning and their content is needs to be consistently enforced.
 * 
 * 
 * @author alex
 *
 */
public class Attributes {


  private Attributes() {}

  /**
   * Validates and possibly reformats special attributes such as {@code class}, {@code names},
   * {@code row.names}.
   * 
   * @param expression the expression on which the attribute is to be set
   * @param name the name of the attribute
   * @param attributeValue the value of the attribute to validate
   * @return {@code attributeValue}, possibly coerced/reformatted 
   * @throws EvalException if the attribute is special, and does not meet exceptions
   */
  public static SEXP validateAttribute(SEXP expression, Symbol name, SEXP attributeValue) {
    if(name.equals(Symbols.CLASS)) {
      return Attributes.validateClassAttributes(attributeValue);
       
    } else if(name.equals(Symbols.NAMES)) {
      return Attributes.validateNamesAttributes(expression, attributeValue);
  
    } else if(name.equals(Symbols.ROW_NAMES)) {
      return Attributes.validateRowNames(attributeValue);
    
    } else {
      return attributeValue;
    }
  }

  /**
   * Validates a {@code names} attribute value 
   * @param expression the expression whose {@code names} attribute is to be updated
   * @param names the proposed {@code names} attribute value
   * @return the {@code names} vector, coerced to a {@link StringVector}
   * @throws EvalException if the given {@code names} vector cannot be coerced to a {@link StringVector} or if it is not 
   * the same length as {@code expression} 
   */
  public static StringVector validateNamesAttributes(SEXP expression, SEXP names) {
    if(names.length() > expression.length()) {
      throw new EvalException("'names' attribute [%d] must be the same length as the vector [%d]",
          names.length(), expression.length());
    }
    return StringVector.coerceFrom(names).setLength(expression.length());
  }

  /**
   * Validates a {@code class} attribute value
   * 
   * @param classNames the proposed {@code class} attribute
   * @return the {@code classNames} vector, coerced to {@link StringVector} if not null.
   */
  public static SEXP validateClassAttributes(SEXP classNames) {
    return classNames.length() == 0 ? Null.INSTANCE : StringVector.coerceFrom(classNames);
  }
  
  /**
   * Validates the {@code row.names} attribute
   * 
   * @param rowNames the {@code row.names} vector to validate
   * @return the given {@code rowNames} vector, possibly in compact form. 
   * @throws EvalException if {@code rowNames} is not a {@link StringVector} or a {@link IntVector}
   */
  public static Vector validateRowNames(SEXP rowNames) {
    
    if(rowNames == Null.INSTANCE) {
      return Null.INSTANCE;
    
    
    // R uses a special "compact format" for row.names that are an integer sequence 1..n
    // in the format c(NA, -n)
    
    } else if(rowNames instanceof DoubleVector && rowNames.length() == 2 && ((DoubleVector) rowNames).isElementNaN(0)) {
      // this is the correct compact format, but we need to store as integer, not double
      int n = -((DoubleVector)rowNames).getElementAsInt(1);
      return Attributes.compactRowNames(n);
    
    } else if(rowNames instanceof IntVector) {
      IntVector vector = (IntVector)rowNames;
      
      if(isCompactRowName(vector)) {
        // this is the compact format, return as OK
        return vector;
        
      } else if(isRowNameSequence(vector)){
        // compact 
        return compactRowNames(vector.length());
        
      } else {
        // arbitrary integer vector is also ok
        return vector;
      }
      
    } else if(rowNames instanceof StringVector) {
      return (StringVector)rowNames;
    }
    
    throw new EvalException("row names must be 'character' or 'integer', not '%s'", rowNames.getTypeName());
  }

  
  
  /**
   * Compact {@code row.names} values represent a row names vector of 1..n and takes the 
   * internal storage form of {@code c(NA, -n)}.
   *  
   * @param vector the {@link IntVector} to test
   * @return true if the given vector is in the internal compact form of c(NA, -n) 
   */
  public static boolean isCompactRowName(IntVector vector) {
    return vector.length() == 2 && vector.isElementNA(0);
  }
  
  /**
   * Compact {@code row.names} values represent a row names vector of 1..n and takes the 
   * internal storage form of {@code c(NA, -n)}.
   *  
   * @param vector the {@link IntVector} to test
   * @return true if the given vector is in the internal compact form of c(NA, -n) 
   */  
  public static boolean isCompactRowName(SEXP exp) {
    return exp instanceof IntVector && isCompactRowName((IntVector)exp);
  }

  /**
   * Determines whether a {@code row.names} vector can be internally stored
   * in compact form.
   * 
   * @param vector the {@code row.names} vector to test.
   * @return true if the {@code row.names} vector is in the form 1..n
   */
  public static boolean isRowNameSequence(IntVector vector) {
    for(int i=0;i!=vector.length();++i) {
      if(vector.getElementAsInt(i) != i+1) {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a compact internal form for {@code row.names} c(NA, -n)
   * @param n the length of the row name vector
   * @return an {@link IntVector} in the form c(NA, -n)
   */
  public static IntVector compactRowNames(int n) {
    return new IntVector(IntVector.NA, -n);
  }

  /**
   * Expands attributes for 'public' consumption. 
   * 
   * Some attributes (only {@code row.names} as far as I know at this point) are stored in 
   * internal compact forms and need to be expanded before being handed to the user.
   * 
   * @param attributes internal attributes pairlist.
   * @return an expanded attributes pairlist.
   */
  public static PairList expandAttributes(PairList attributes) {
    PairList.Builder result = new PairList.Builder();
    for(PairList.Node node : attributes.nodes()) {
      result.add(node.getTag(), postProcessAttributeValue(node));
    }
    return result.build();
  }
  
  public static SEXP postProcessAttributeValue(PairList.Node node) {
    if(node.getTag().equals(Symbols.ROW_NAMES)) {
      return postProcessRowNames((Vector)node.getValue());
    }
    return node.getValue();
  }

  private static SEXP postProcessRowNames(Vector names) {
    if(isCompactRowName(names)) {
      return expandCompactRowNames(names);
    } else {
      return names;
    }
  }

  private static SEXP expandCompactRowNames(Vector names) {
    int n = -names.getElementAsInt(1);
    int result[] = new int[n];
    for(int i=0;i!=n;++i) {
      result[i] = i+1;
    }
    return new IntVector(result);
  }

}
