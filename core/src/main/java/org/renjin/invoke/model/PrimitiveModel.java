package org.renjin.invoke.model;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.renjin.invoke.codegen.GeneratorDefinitionException;
import org.renjin.invoke.codegen.WrapperGenerator2;
import org.renjin.invoke.model.JvmMethod;
import org.renjin.primitives.Primitives;
import org.renjin.invoke.annotations.ArgumentList;

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

}
