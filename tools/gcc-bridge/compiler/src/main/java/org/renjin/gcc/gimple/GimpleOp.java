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

import org.renjin.gcc.gimple.expr.GimpleAddressOf;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.repackaged.guava.base.Joiner;

import java.util.List;

/**
 * Enumeration of Gimple operations
 */
public enum GimpleOp {
  NOP_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return operands.get(0).toString();
    }
  },

  MULT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("*", operands);
    }
  },

  RDIV_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("/", operands);
    }
  },

  ABS_EXPR,

  MIN_EXPR,
  MAX_EXPR,

  ADDR_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      assert operands.get(0) instanceof GimpleAddressOf;
      return operands.get(0).toString();
    }
  },

  /**
   * Converting integer to real
   */
  FLOAT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "(float)" + operands.get(0);
    }
  },

  /**
   * Truncate float to integer
   */
  FIX_TRUNC_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.call("trunc", operands);
    }
  },

  /**
   * The EXACT_DIV_EXPR code is used to represent integer divisions where the numerator is
   *  known to be an exact multiple of the denominator. This allows the
   *  backend to choose between the faster of TRUNC_DIV_EXPR, CEIL_DIV_EXPR and
   *  FLOOR_DIV_EXPR for the current target
   */
  EXACT_DIV_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("/", operands);
    }
  },

  TRUNC_DIV_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("/", operands);
    }
  },

  /**
   * Real Constant
   */
  REAL_CST {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "(real)" + operands.get(0);
    }
  },

  /**
   * Integer Constant
   */
  INTEGER_CST {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "(int)" + operands.get(0);
    }
  },

  STRING_CST {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "(string)" + operands.get(0);
    }
  },

  NE_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("!=", operands);
    }
  },

  EQ_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("==", operands);
    }
  },
  LT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("<", operands);
    }
  },

  GT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix(">", operands);
    }
  },

  LE_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("<=", operands);
    }
  },

  GE_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix(">=", operands);
    }
  },

  TRUTH_NOT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "!" + operands.get(0);
    }
  },
  TRUTH_XOR_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("^", operands);
    }
  },
  TRUTH_OR_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("||", operands);
    }
  },
  TRUTH_AND_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("&&", operands);
    }
  },

  POINTER_PLUS_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("+", operands);
    }
  },

  INDIRECT_REF,


  PLUS_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("+", operands);
    }
  },

  MINUS_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("-", operands);
    }
  },

  SSA_NAME {
    @Override
    public String format(List<GimpleExpr> operands) {
      return operands.get(0).toString();
    }
  },

  PARM_DECL {
    @Override
    public String format(List<GimpleExpr> operands) {
      return operands.get(0).toString();
    }
  },

  VAR_DECL {
    @Override
    public String format(List<GimpleExpr> operands) {
      return operands.get(0).toString();
    }
  },

  COMPONENT_REF {
    @Override
    public String format(List<GimpleExpr> operands) {
      assert operands.size() == 1;
      return operands.get(0).toString();
    }
  },

  ARRAY_REF {
    @Override
    public String format(List<GimpleExpr> operands) {
      assert operands.size() == 1;
      return operands.get(0).toString();
    }
  },

  MEM_REF {
    @Override
    public String format(List<GimpleExpr> operands) {
      assert operands.size() == 1;
      return operands.get(0).toString();
    }
  },

  BIT_NOT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "~" + operands.get(0);
    }
  },
  BIT_AND_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("&", operands);
    }
  },

  BIT_IOR_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("|", operands);
    }
  },
  BIT_XOR_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("^^", operands);
    }
  },
  LSHIFT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix("<<", operands);
    }
  },
  RSHIFT_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.infix(">>", operands);
    }
  },
  LROTATE_EXPR,

  NEGATE_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return "-" + operands.get(0);
    }
  },

  /**
   * From GCC source code: Represents a re-association barrier for floating
   * point expressions like explicit parenthesis in fortran. (AB: Huh?)
   */
  PAREN_EXPR,

  /**
   * Take two floating point operands and determine whether they are ordered or unordered relative to each other.
   * If b operand is an IEEE NaN, their comparison is defined to be unordered.
   */
  ORDERED_EXPR,

  /**
   * Take two floating point operands and determine whether they are ordered or unordered relative to each other.
   * If either operand is an IEEE NaN, their comparison is defined to be unordered.
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


  CONVERT_EXPR,

  TRUNC_MOD_EXPR,

  CONSTRUCTOR,

  /**
   * Extracts the real part of complex number expression
   */
  REALPART_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.call("Re", operands);
    }
  },

  /**
   * Extracts the imaginary part of a complex number expression
   */
  IMAGPART_EXPR {
    @Override
    public String format(List<GimpleExpr> operands) {
      return GimpleOp.call("Im", operands);
    }
  },


  /**
   * Complex number constant
   */
  COMPLEX_CST,

  COMPLEX_EXPR,

  /**
   * Complex conjugate expression
   */
  CONJ_EXPR,

  BIT_FIELD_REF {
    @Override
    public String format(List<GimpleExpr> operands) {
      return operands.get(0).toString();
    }
  };

  public String format(List<GimpleExpr> operands) {
    return call(name().replace("_EXPR", "").toLowerCase(), operands);
  }

  private static String infix(String op, List<GimpleExpr> operands) {
    return operands.get(0) + " " + op + " " + operands.get(1);
  }

  private static String call(String name, List<GimpleExpr> operands) {
    return name + "(" + Joiner.on(", ").join(operands) + ")";
  }

}
