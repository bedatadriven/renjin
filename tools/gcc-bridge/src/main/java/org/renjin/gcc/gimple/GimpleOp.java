package org.renjin.gcc.gimple;

public enum GimpleOp {
  NOP_EXPR,
	MULT_EXPR,
  RDIV_EXPR,
  ABS_EXPR,
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
   * Real Constant
   */
	REAL_CST,

  /**
   * Integer Constant
   */
  INTEGER_CST,

  NE_EXPR,
  EQ_EXPR,
  LT_EXPR,
  GT_EXPR,
  LE_EXPR,
  GE_EXPR,
  TRUTH_NOT_EXPR,
  POINTER_PLUS_EXPR,
  INDIRECT_REF,
  PLUS_EXPR,
  MINUS_EXPR,
  SSA_NAME,
  VAR_DECL
}
