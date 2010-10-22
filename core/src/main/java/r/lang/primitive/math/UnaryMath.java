/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997-2008  The R Development Core Team
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

package r.lang.primitive.math;

import com.google.common.base.Preconditions;
import r.lang.*;
import r.lang.exception.BuiltinException;
import r.lang.primitive.BuiltinFunction;

public abstract class UnaryMath extends BuiltinFunction {

  @Override
  public final SEXP apply(LangExp call, ListExp args, EnvExp rho) {
    Preconditions.checkArgument(args.length() == 1);

    SEXP argument = args.getFirst().evaluate(rho);
    if (!argument.isNumeric()) {
      throw new BuiltinException("Non-numeric argument to mathematical function");
    }

    return applyReal(argument);
  }

  private RealExp applyReal(SEXP argument) {
    RealExp sa = (RealExp) argument;
    RealExp sy = RealExp.ofLength(sa.length());

    boolean naflag = false;
    int i;

    for (i = 0; i < sa.length(); i++) {
      if (Double.isNaN(sa.get(i))) {
        sy.set(i, sa.get(i));
      } else {
        sy.set(i, apply(sa.get(i)));
        if (Double.isNaN(sy.get(i))) {
          naflag = true;
        }
      }
    }
    if (naflag) {
      //warningcall(lcall, R_MSG_NA);
    }

    // todo: attribs DUPLICATE_ATTRIB(sy, sa);
    return sy;
  }

  protected abstract double apply(double value);

  public static class Floor extends UnaryMath {
    @Override
    protected double apply(double value) {
      return Math.floor(value);
    }
  }

  public static class Ceiling extends UnaryMath {
    @Override
    protected double apply(double value) {
      return Math.ceil(value);
    }
  }

  public static class Sqrt extends UnaryMath {
    @Override
    protected double apply(double value) {
      return Math.sqrt(value);
    }
  }

  public static class Sign extends UnaryMath {
    @Override
    protected double apply(double value) {
      return Math.signum(value);
    }
  }


}
