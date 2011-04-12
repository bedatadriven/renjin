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

package r.base;

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.Context;
import r.lang.Environment;
import r.lang.exception.EvalException;

public class Contexts {

  private Contexts() {}

  @Primitive("sys.nframe")
  public static int sysFrameCount(@Current Context context) {
    if(context.getEvaluationDepth() == 0) {
      return 0;
    } else {
      // don't count the closure within which .Internal(sys.nframe()) is called
      return context.getEvaluationDepth() - 1;
    }
  }

  /**
   * @param context the current call context
   * @param n generations to ascend the call tree (1=parent, 2=grandparent, etc)
   * @return the Environment of the parent environment
   *
   */
  @Primitive("parent.frame")
  public static Environment parentFrame(@Current Context context,  int n) {
    if(n < 1) {
      throw new EvalException("invalid 'n' value");
    }

    // note that we actually climb n+1 parents in order to
    // skip the closure that makes the .Internal(parent.frame()) call

    Context parent = context;
    while(n>=0 && !parent.isTopLevel()) {
      if(parent.getType() == Context.Type.FUNCTION) {
        --n;
      }
      parent = parent.getParent();
    }
    return parent.getEnvironment();
  }

  @Primitive("sys.frame")
  public static Environment sysFrame(@Current Context context, int which) {
    if(which < 0) {
      which = context.getEvaluationDepth() + which - 1;
    }

    if(which < 0 || which > context.getEvaluationDepth()) {
      throw new EvalException("not that many frames on the stack");
    }

    Context frame = context;
    while(frame.getEvaluationDepth() != which) {
      frame = frame.getParent();
    }

    return frame.getEnvironment();
  }

}
