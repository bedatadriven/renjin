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

package org.renjin.primitives;

import org.renjin.eval.Context;
import org.renjin.eval.Context.Type;
import org.renjin.eval.EvalException;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.sexp.Closure;
import org.renjin.sexp.Environment;
import org.renjin.sexp.Function;
import org.renjin.sexp.FunctionCall;


/**
 * Functions that provide access to the call Context stack.
 */
public class Contexts {

  private Contexts() {}

  /**
   * Returns the index of the current frame.
   *
   * .GlobalEnv is given number 0 in the list of frames.
   * Each subsequent function evaluation increases the frame stack by 1.
   *
   * @param context the current {@code Context}
   * @return the index of the current frame.
   */
  @Primitive("sys.nframe")
  public static int sysFrameCount(@Current Context context) {
    return framedepth(findStartingContext(context));
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

    Context cptr = context;
    Environment t = cptr.getCallingEnvironment();
    while (!cptr.isTopLevel()){
      if (cptr.getType() == Type.FUNCTION ) {
        if (cptr.getEnvironment() == t) {
          if (n == 1) {
            return cptr.getCallingEnvironment();
          }
          n--;
          t = cptr.getCallingEnvironment();
        }
      }
      cptr = cptr.getParent();
    }
    return context.getGlobalEnvironment();
  } 

  
  /** Find the environment that can be returned by sys.frame 
    * (so it needs to be on the cloenv pointer of a context) that matches 
    * the environment where the closure arguments are to be evaluated. 
    * It would be much simpler if sysparent just returned cptr->sysparent
    * but then we wouldn't be compatible with S. 
    **/
  private static int R_sysparent(int n, Context cptr) {
    int j;
    if(n <= 0) {
      throw new EvalException("only positive values of 'n' are allowed");
    }
    while (!cptr.isTopLevel() && n > 1) {
      if (cptr.getType() == Type.FUNCTION ) {
        n--;
      }
      cptr = cptr.getParent();
    }
    /* make sure we're looking at a return context */
    while (!cptr.isTopLevel()  && cptr.getType() != Type.FUNCTION)  { 
      cptr = cptr.getParent();
    }

    Environment s = cptr.getCallingEnvironment();

    if(s == cptr.getGlobalEnvironment()) {
      return 0;
    }
    j = 0;
    while (true ) {
      if (cptr.getType() == Type.FUNCTION) {
        j++;
        if( cptr.getEnvironment() == s )
          n=j;
      }
      if(cptr.isTopLevel()) {
        break;
      }
      cptr = cptr.getParent();
    }
    n = j - n + 1;
    if (n < 0) {
      n = 0;
    }
    return n;
  }
  
  private static Closure R_sysfunction(int n, Context cptr) {
    if (n > 0) {
      n = framedepth(cptr) - n;
    } else {
      n = - n;
    }
    if (n < 0) {
      throw new EvalException("not that many frames on the stack");
    }
    while (!cptr.isTopLevel()) {
      if (cptr.getType() == Type.FUNCTION ) {
        if (n == 0)
          return cptr.getClosure();
        else
          n--;
      }
      cptr = cptr.getParent();
    }
    if (n == 0 && cptr.isTopLevel()) {
      return cptr.getClosure();
    }
    throw new EvalException("not that many frames on the stack");
  }
  

/* R_sysframe - look back up the context stack until the */
/* nth closure context and return that cloenv. */
/* R_sysframe(0) means the R_GlobalEnv environment */
/* negative n counts back from the current frame */
/* positive n counts up from the globalEnv */

  private static Environment R_sysframe(int n, Context cptr) {
    if (n == 0) {
      return cptr.getGlobalEnvironment();
    }

    if (n > 0) {
      n = framedepth(cptr) - n;
    }  else {
      n = -n;
    }

    if(n < 0) {
      throw new EvalException("not that many frames on the stack");
    }

    while (!cptr.isTopLevel()) {
      if (cptr.getType() == Type.FUNCTION ) {
        if (n == 0) {  /* we need to detach the enclosing env */
          return cptr.getEnvironment();
        } else {
          n--;
        }
      }
      cptr = cptr.getParent();
    }
    if(n == 0 && cptr.isTopLevel()) {
      return cptr.getGlobalEnvironment();
    }
    throw new EvalException("not that many frames on the stack");
  }


  private static FunctionCall R_syscall(int n, Context cptr) {
    /* negative n counts back from the current frame */
    /* positive n counts up from the globalEnv */
  
    if (n > 0)
      n = framedepth(cptr) - n;
    else
      n = - n;
    if(n < 0) {
      throw new EvalException("not that many frames on the stack");
    }
    while (!cptr.isTopLevel() ) {
      if (cptr.getType() == Type.FUNCTION) {
        if (n == 0) {
          return cptr.getCall();
        } else {
          n--;
        }
      }
      cptr = cptr.getParent();
    }
    if (n == 0 && cptr.isTopLevel()) {
      return cptr.getCall();
    }
    throw new EvalException("not that many frames on the stack");
  }


  private static int framedepth(Context cptr)
  {
    int nframe = 0;
    while (!cptr.isTopLevel()) {
      if (cptr.getType() == Type.FUNCTION )
        nframe++;
      cptr = cptr.getParent();
    }
    return nframe;
  }
  
  @Primitive("sys.parent")
  public static int sysParent(@Current Context context, int n) {
    
    Context cptr = findStartingContext(context);
    
    int i, nframe;
    i = nframe = framedepth(cptr);
    /* This is a pretty awful kludge, but the alternative would be
       a major redesign of everything... -pd */
    while (n-- > 0)
        i = R_sysparent(nframe - i + 1, cptr);
    return i;
  
  }

  private static Context findStartingContext(Context context) {
    /* first find the context that sys.xxx needs to be evaluated in */
    Context cptr = context;
    Environment t = cptr.getCallingEnvironment();
    while (!cptr.isTopLevel()) {
      if(cptr.getType() == Type.FUNCTION) {
        if(cptr.getEnvironment() == t) {
          break;
        }
      }
      cptr = cptr.getParent();
    }
    return cptr;
  }


  @Primitive("sys.frame")
  public static Environment sysFrame(@Current Context context, int which) {
    return R_sysframe(which, findStartingContext(context));
  }

  @Primitive("sys.call")
  public static FunctionCall sysCall(@Current Context context, int which) {
    return R_syscall(which, findStartingContext(context));
  }

  @Primitive("sys.function")
  public static Function sysFunction(@Current Context context, int which) {
    return R_sysfunction(which, findStartingContext(context));
  }


}
