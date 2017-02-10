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
package org.renjin.invoke.model;

import org.renjin.invoke.annotations.ArgumentList;
import org.renjin.invoke.codegen.GeneratorDefinitionException;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.primitives.Primitives;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Describes a given primitive based on the type signature and
 * annotations of the provided implementations
 */
public class PrimitiveModel {

  private final Primitives.Entry entry;
  private final List<JvmMethod> overloads;

  public PrimitiveModel(Primitives.Entry entry, List<JvmMethod> overloads) {
    this.entry = entry;
    this.overloads = overloads;
  }

  public String argumentErrorMessage() {
    StringBuilder message = new StringBuilder();
    message.append("Invalid argument: %s. Expected:");
    for(JvmMethod method : overloads) {
      message.append("\n\t");
      method.appendFriendlySignatureTo(entry.name, message);
    }
    return message.toString();
  }


  /**
   *
   * @return the maximum number of positional arguments specified by any of the
   * overloads
   */
  public int maxPositionalArgs() {
    int max = 0;
    for (JvmMethod overload : overloads) {
      int count = overload.countPositionalFormals();
      if (count > max) {
        max = count;
      }
    }
    return max;
  }

  public List<JvmMethod> overloadsWithPosArgCountOf(int i) {
    List<JvmMethod> matching = Lists.newArrayList();
    for(JvmMethod overload : overloads) {
      if( overload.countPositionalFormals() == i) {
        matching.add(overload);
      }
    }
    Collections.sort(matching);
    return matching;
  }

  public boolean isEvaluated(int argumentIndex) {
    boolean evaluated = false;
    boolean unevaluated = false;
    for (JvmMethod overload : overloads) {
      if (argumentIndex < overload.getFormals().size()) {
        if (overload.getFormals().get(argumentIndex).isEvaluated()) {
          evaluated = true;
        } else {
          unevaluated = true;
        }
      }
    }
    if (evaluated && unevaluated) {
      throw new GeneratorDefinitionException(
              "Mixing evaluated and unevaluated arguments at the same position is not yet supported");
    }
    return evaluated;
  }

  public String getName() {
    return entry.name;
  }

  public String getJavaName() {
    return WrapperGenerator2.toJavaName(getName());
  }

  public String getClassName() {
    return "R$primitive$" + getJavaName();
  }


  public boolean isSpecial() {
    return entry.isSpecial();
  }

  public List<Integer> getArity() {
    Set<Integer> arity = Sets.newHashSet();

    for(JvmMethod overload : overloads) {
      arity.add(overload.countPositionalFormals());
    }

    List<Integer> list = Lists.newArrayList(arity);
    Collections.sort(list);

    return list;
  }

  public int getMaxArity() {
    int max = 0;
    for(JvmMethod overload : overloads) {
      if(overload.countPositionalFormals() > max) {
        max = overload.countPositionalFormals();
      }
    }
    return max;
  }

  public boolean hasVargs() {
    for(JvmMethod overload : overloads) {
      for(JvmMethod.Argument argument : overload.getFormals()) {
        if(argument.isAnnotatedWith(ArgumentList.class)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<JvmMethod> getOverloads() {
    return overloads;
  }

  public boolean isRelationalOperator() {
    if(getName().equals("==") || getName().equals("!=") ||
        getName().equals(">=") || getName().equals("<=") ||
        getName().equals(">") || getName().equals("<")) {
      return true;
    }
    return false;
  }
}
