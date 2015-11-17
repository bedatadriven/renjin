package org.renjin.gcc.gimple;

public enum GimpleOp {
  NOP_EXPR, MULT_EXPR, RDIV_EXPR,


  ABS_EXPR, MAX_EXPR,

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

  NE_EXPR, EQ_EXPR, LT_EXPR, GT_EXPR, LE_EXPR, GE_EXPR,

  TRUTH_NOT_EXPR, TRUTH_OR_EXPR, TRUTH_AND_EXPR,

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

  /**
   * From GCC source code: Additional relational operators for floating point
   * unordered.
   */
  UNORDERED_EXPR,

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
  CONJ_EXPR
  
  
}
