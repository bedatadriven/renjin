package r.jvmi.wrapper.generator;

import java.util.List;

import r.jvmi.annotations.ArgumentList;
import r.jvmi.binding.JvmMethod;
import r.jvmi.wrapper.generator.args.ArgConverterStrategies;
import r.jvmi.wrapper.generator.args.ArgConverterStrategy;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class OverloadNode {

  private static final String INDENT = "   ";

  private ArgConverterStrategy argument;
  
  private List<OverloadNode> children = Lists.newArrayList();
  
  private JvmMethod overload;
  
  public int getBranchCount() {
    int count = children.size();
    if(overload != null) {
      count++;
    }
    return count;
  }
  
  public static OverloadNode buildTree(List<JvmMethod> overloads) {
    OverloadNode root = new OverloadNode();
    for(JvmMethod overload : overloads) {
      buildChildren(root, overload, 0);
    }
    return root;
  }

  private static void buildChildren(OverloadNode root, JvmMethod overload, int argIndex) {
    if(overload.getFormals().size() == argIndex) {
      root.overload = overload;
    } else {
      JvmMethod.Argument formal = overload.getFormals().get(argIndex);
      if(formal.isAnnotatedWith(ArgumentList.class)) {
        root.overload = overload;
      } else {
        ArgConverterStrategy strategy = ArgConverterStrategies.findArgConverterStrategy(formal);
        OverloadNode child = root.addChild(strategy);
        buildChildren(child, overload, argIndex+1);
      }
    }
  }

  
  private OverloadNode addChild(ArgConverterStrategy strategy) {
    for(OverloadNode child : children) {
      if(child.argument.getArgType().equals(strategy.getArgType())) {
        return child;
      }
    }
    
    OverloadNode child = new OverloadNode();
    child.argument = strategy;
    children.add(child);
    return child;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    return appendTo(sb, "");
  }

  private String appendTo(StringBuilder sb, String indent) {
    sb.append(indent);
    if(argument != null) {
      sb.append(argument.getArgType().getSimpleName())
        .append("=");
    }
    if(!children.isEmpty()) {
      for(OverloadNode node : children) {
        sb.append("\n");
        node.appendTo(sb, indent + INDENT);

      }
    }
    if(overload != null) {
      if(!children.isEmpty()) {
        sb.append("\n")
          .append(indent+INDENT)
          .append("ELSE=");
      } 
      overload.appendFriendlySignatureTo(sb);
    }
    return sb.toString();
  }

  public ArgConverterStrategy getArgStrategy() {
    return argument;
  }

  public JvmMethod getLeaf() {
    return overload;
  }
  
  public List<OverloadNode> getChildren() {
    return children;
  }

  public boolean hasLeaf() {
    return overload != null;
  }
  
  public boolean isEvaluated() {
    return Iterables.any(children, EVALUATED);
  }
  
  private Predicate<OverloadNode> EVALUATED = new Predicate<OverloadNode>() {
    
    @Override
    public boolean apply(OverloadNode input) {
      return input.getArgStrategy().isEvaluated();
    }
  };

  public boolean hasNextArg() {
    return !children.isEmpty();
  }
    
}
