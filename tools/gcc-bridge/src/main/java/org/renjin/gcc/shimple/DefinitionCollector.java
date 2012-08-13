package org.renjin.gcc.shimple;


import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.GimpleAssign;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParameter;
import org.renjin.gcc.gimple.GimpleVisitor;

import java.util.Set;

/**
 * Collects all the (versioned) variables that are defined in
 * this function.
 */
public class DefinitionCollector extends GimpleVisitor {

  private Set<String> names = Sets.newHashSet();

  public DefinitionCollector(GimpleFunction fn) {
    for(GimpleParameter param : fn.getParameters()) {
      names.add(param.getName() + "_1");
    }
    fn.visitIns(this);
  }

  @Override
  public void visitAssignment(GimpleAssign assignment) {
    names.add(assignment.getRHS().getName());
  }

  public Set<String> getNames() {
    return names;
  }
}
