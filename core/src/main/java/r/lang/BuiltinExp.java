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

package r.lang;

import r.lang.primitive.FunctionTable;

public class BuiltinExp extends PrimitiveSexp {
  public static final int TYPE_CODE = 8;
  public static final String TYPE_NAME = "builtin";

  public BuiltinExp(FunctionTable.Entry functionEntry) {
    super(functionEntry);
  }

  @Override
  public int getTypeCode() {
    return TYPE_CODE;
  }

  @Override
  public String getTypeName() {
    return TYPE_NAME;
  }

  @Override
  public void accept(SexpVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  protected ListExp prepareArguments(ListExp args, EnvExp rho) {
    if (args == null) {
      return null;
    }
    ListExp.Builder builder = new ListExp.Builder();
    for (SEXP arg : args) {
      builder.add(arg.evaluate(rho));
    }
    return builder.list();
  }
}
