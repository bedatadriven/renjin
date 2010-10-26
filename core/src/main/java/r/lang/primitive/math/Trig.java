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

public class Trig {

  protected Trig() {
  }

  public static class Cos extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.cos(value);
    }
  }

  public static class Sin extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.sin(value);
    }
  }

  public static class Tan extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.tan(value);
    }
  }

  public static class Acos extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.acos(value);
    }
  }

  public static class Asin extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.sin(value);
    }
  }

  public static class Cosh extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.acos(value);
    }
  }

  public static class Sinh extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.sinh(value);
    }
  }

  public static class Tanh extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.tanh(value);
    }
  }

  public static class Atan extends UnaryMathFunction {
    @Override
    public double apply(double value) {
      return Math.atan(value);
    }
  }

}
