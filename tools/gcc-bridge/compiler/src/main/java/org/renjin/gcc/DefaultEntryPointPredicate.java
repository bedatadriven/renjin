package org.renjin.gcc;

import com.google.common.base.Predicate;
import org.renjin.gcc.gimple.GimpleFunction;

import javax.annotation.Nullable;

/**
 * Identifies functions which are considered "entry points" and must definitely
 * be compiled. Other functions may be removed if they are not used.
 */
public class DefaultEntryPointPredicate implements Predicate<GimpleFunction> {
  @Override
  public boolean apply(@Nullable GimpleFunction function) {

    if(!function.isExtern() || function.isWeak() || function.isInline()) {
      return false;
    }
    // This is a bit of hack, but assume that C++ mangled names are NOT entry
    // points
    if(function.getName().startsWith("_Z")) {
      return false;
    }
    return true;
  }
}
