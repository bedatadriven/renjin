/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur;

import org.renjin.gnur.api.Rinternals;
import org.renjin.gnur.api.annotations.Allocator;
import org.renjin.gnur.api.annotations.Mutee;
import org.renjin.gnur.api.annotations.PotentialMutator;
import org.renjin.repackaged.guava.collect.Sets;
import org.renjin.sexp.SEXP;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides metadata about GNU R C API methods
 */
public class ApiOracle {


  private Set<String> allocators = new HashSet<>();

  private Map<String, Method> potentialMutators = new HashMap<>();

  public ApiOracle() {
    Set<Class> apiClasses = Sets.newHashSet(Rinternals.class);

    for (Class apiClass : apiClasses) {
      for (Method method : apiClass.getMethods()) {
        if(method.getAnnotation(Allocator.class) != null) {
          allocators.add(method.getName());
        }
        if(method.getAnnotation(PotentialMutator.class) != null) {
          checkPotentialMutatorAnnotation(method);
          potentialMutators.put(method.getName(), method);
        }
      }
    }
  }

  private void checkPotentialMutatorAnnotation(Method method) {
    if(!method.getReturnType().equals(SEXP.class)) {
      throw new IllegalStateException("Methods annotated with @" + PotentialMutator.class.getSimpleName() +
          " must return " + SEXP.class.getSimpleName());
    }
  }

  public boolean isAllocator(String functionName) {
    return allocators.contains(functionName);
  }

  public boolean isPotentialMutator(String functionName) {
    return potentialMutators.containsKey(functionName);
  }

  public int getMuteeArgumentIndex(String functionName) {
    Method method = potentialMutators.get(functionName);
    if(method == null) {
      throw new IllegalStateException(functionName + " is not a @PotentialMutator");
    }
    for (int i = 0; i < method.getParameterCount(); i++) {
      Annotation[] annotations = method.getParameterAnnotations()[i];
      for (int j = 0; j < annotations.length; j++) {
        if (annotations[j] instanceof Mutee) {
          return i;
        }
      }
    }
    throw new IllegalStateException("One argument in " + method + " must be annotated with @Mutee");
  }
}
