package org.renjin.gcc.translate.struct;


import com.google.common.collect.Maps;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.jimple.*;
import org.renjin.gcc.translate.FunctionContext;
import org.renjin.gcc.translate.TranslationContext;

import java.util.Map;

/**
 * Represents a "struct" that is constructed from
 * Gcc output.
 */
public class GccStruct extends Struct {
  private TranslationContext context;
  private String name;
  private JimpleClassBuilder structClass;

  private Map<String, JimpleType> types = Maps.newHashMap();

  public GccStruct(TranslationContext context, String name) {
    this.context = context;
    this.name = name;
    this.structClass = context.getJimpleOutput().newClass();
    this.structClass.setPackageName(context.getMainClass().getPackageName());
    this.structClass.setClassName(context.getMainClass().getClassName() + "$" + name);

    inferMembers();
  }

  private void inferMembers() {
    // the tree dump we get from GCC doesn't include struct layout.
    // it may be possible to get dumps on this, or to write a plugin
    // to dump the needed information, but we can also basically deduce
    // the members and types from the Gimple

    MemberFinder finder = new MemberFinder(name);
    for(GimpleFunction function : context.getFunctions()) {
      finder.visit(function);
    }

    for(Map.Entry<String, GimpleType> member : finder.getMembers().entrySet()) {
      JimpleType type = context.resolveType(member.getValue()).paramType();
      types.put(member.getKey(), type);

      JimpleFieldBuilder field = structClass.newField();
      field.setName(member.getKey());
      field.setType(type);
      field.setModifiers(JimpleModifiers.PUBLIC);
    }
  }

  @Override
  public JimpleExpr memberRef(JimpleExpr instanceExpr, String member, JimpleType jimpleType) {
    return new JimpleExpr(instanceExpr + ".<" + structClass.getFqcn() + ": " + types.get(member) +
            " " + member + ">");
  }

  @Override
  public void assignMember(FunctionContext context, JimpleExpr instance, String member, JimpleExpr jimpleExpr) {
    context.getBuilder().addStatement(instance + ".<" + structClass.getFqcn() + ": " +
            types.get(member) + " " + member + "> = " + jimpleExpr);
  }

  @Override
  public JimpleType getJimpleType() {
    return new StructJimpleType(structClass.getFqcn());
  }
}
