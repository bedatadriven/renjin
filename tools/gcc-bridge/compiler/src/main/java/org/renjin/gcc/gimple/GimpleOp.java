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
package org.renjin.gcc.gimple;

/**
 * Enumeration of Gimple operations
 */
public enum GimpleOp {
  NOP_EXPR, MULT_EXPR, RDIV_EXPR,


  ABS_EXPR, 
  
  MIN_EXPR,
  MAX_EXPR,

  ADDR_EXPR,

  /**
   * Converting integer to real
   */
  FLOAT_EXPR,

  /**
   * Truncate float to integer
   */
  FIX_TRUNC_EXPR,

  /**
   * The EXACT_DIV_EXPR code is used to represent integer divisions where the numerator is
   *  known to be an exact multiple of the denominator. This allows the
   *  backend to choose between the faster of TRUNC_DIV_EXPR, CEIL_DIV_EXPR and
   *  FLOOR_DIV_EXPR for the current target
   */
  EXACT_DIV_EXPR,
  
  TRUNC_DIV_EXPR,

  /**
   * Real Constant
   */
  REAL_CST,

  /**
   * Integer Constant
   */
  INTEGER_CST,

  STRING_CST,
  
  NE_EXPR, EQ_EXPR, LT_EXPR, GT_EXPR, LE_EXPR, GE_EXPR,

  TRUTH_NOT_EXPR,
  TRUTH_XOR_EXPR,
  TRUTH_OR_EXPR, 
  TRUTH_AND_EXPR,

  POINTER_PLUS_EXPR, INDIRECT_REF, PLUS_EXPR, MINUS_EXPR, SSA_NAME,

  PARM_DECL,

  VAR_DECL,

  COMPONENT_REF, ARRAY_REF, MEM_REF,

  BIT_NOT_EXPR,
  BIT_AND_EXPR,
  
  BIT_IOR_EXPR,
  BIT_XOR_EXPR,
  LSHIFT_EXPR,
  RSHIFT_EXPR,
  LROTATE_EXPR,
  
  NEGATE_EXPR,

  /**
   * From GCC source code: Represents a re-association barrier for floating
   * point expressions like explicit parenthesis in fortran. (AB: Huh?)
   */
  PAREN_EXPR,

  ORDERED_EXPR,

  /**
   * From GCC source code: Additional relational operators for floating point
   * unordered.
   */
  UNORDERED_EXPR,

  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is less than the second.
   */
  UNLT_EXPR,

  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is less than or equal to the second.
   */
  UNLE_EXPR,

  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is greater than the second.
   */
  UNGT_EXPR,

  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is greater than or equal to the second.
   */
  UNGE_EXPR,

  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is equal to the second.
   */
  UNEQ_EXPR,


  /**
   *  Returns true if either operand is an IEEE NaN or the first operand is equal to the second.
   */
  LTGT_EXPR,


  CONVERT_EXPR, TRUNC_MOD_EXPR,

  CONSTRUCTOR,

  /**
   * Extracts the real part of complex number expression
   */
  REALPART_EXPR,

  /**
   * Extracts the imaginary part of a complex number expression
   */
  IMAGPART_EXPR,


  /**
   * Complex number constant
   */
  COMPLEX_CST,

  COMPLEX_EXPR,

  /**
   * Complex conjugate expression
   */
  CONJ_EXPR,
  
  BIT_FIELD_REF
  
  
}
