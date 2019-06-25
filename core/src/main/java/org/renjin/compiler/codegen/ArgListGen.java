package org.renjin.compiler.codegen;

import org.renjin.compiler.codegen.expr.SexpLoader;
import org.renjin.eval.ArgList;
import org.renjin.eval.Context;
import org.renjin.repackaged.asm.Opcodes;
import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.asm.commons.InstructionAdapter;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgListGen {

  private final EmitContext context;
  private final InstructionAdapter mv;
  private List<String> names;
  private List<SexpLoader> values;


  public ArgListGen(EmitContext context, InstructionAdapter mv) {
    this.context = context;
    this.mv = mv;
  }


  public ArgListGen names(List<String> names) {
    this.names = names;
    return this;
  }

  public ArgListGen values(List<SexpLoader> values) {
    this.values = values;
    return this;
  }

  public ArgListGen names(Stream<String> nameStream) {
    this.names = nameStream.collect(Collectors.toList());
    return this;
  }

  public ArgListGen values(Stream<SexpLoader> stream) {
    this.values = stream.collect(Collectors.toList());
    return this;
  }

  /**
   * Load the provided names and arguments on the stack as an {@link ArgList} instance,
   * without any forwarded (...) arguments.
   */
  public void load() {
    if(names.size() < 5) {
      loadFixedArgListWithHelper();
    } else {
      loadFixedArgListWithArray();
    }
  }

  /**
   * Load the provided names and arguments onto the stack as an {@link ArgList} instance,
   * with the promised, forwarded arguments forced and expanded into the
   * position at {@code forwardedArgIndex}
   *
   * <p><strong>The {@link Context} and {@link Environment} must both be loaded onto the stack already.</strong></p>
   */
  public void forceExpandLoad(int forwardedArgumentIndex) {

    load();

    mv.iconst(forwardedArgumentIndex);

    // Now we have the following on the stack
    // Context
    // Environment
    // ArgList
    // int

    mv.invokestatic(Type.getInternalName(ArgList.class), "forceExpand",
        Type.getMethodDescriptor(Type.getType(ArgList.class),
            Type.getType(Context.class),
            Type.getType(Environment.class),
            Type.getType(ArgList.class),
            Type.INT_TYPE), false);

  }

  /**
   * Load the provided names and arguments on the stack as an {@link ArgList} instance,
   * with the promised arguments expanded into the list at position.
   *
   * <p><strong>The {@link Context}  must both be loaded onto the stack already.</strong></p>
   */
  public void expandLoad(int forwardedArgumentIndex) {

    load();

    mv.iconst(forwardedArgumentIndex);

    // Now we have the following on the stack
    // Environment
    // ArgList
    // int

    mv.invokestatic(Type.getInternalName(ArgList.class), "expand",
        Type.getMethodDescriptor(Type.getType(ArgList.class),
            Type.getType(Environment.class),
            Type.getType(ArgList.class),
            Type.INT_TYPE), false);

  }

  private void loadFixedArgListWithHelper() {
    List<Type> argumentTypes = new ArrayList<>();
    for (int i = 0; i < names.size(); i++) {
      argumentTypes.add(Type.getType(String.class));
      argumentTypes.add(Type.getType(SEXP.class));

      mv.aconst(names.get(i));
      values.get(i).loadSexp(context, mv);
    }

    String helper = Type.getMethodDescriptor(Type.getType(ArgList.class), 
        argumentTypes.toArray(new Type[0]));
    
    mv.invokestatic(Type.getInternalName(ArgList.class), "of", helper, false);
  }


  private void loadFixedArgListWithArray() {
    int numArguments = names.size();
    mv.iconst(numArguments);
    mv.newarray(Type.getType(String.class));
    for (int i = 0; i < numArguments; i++) {
      if(!Strings.isNullOrEmpty(names.get(i))) {
        mv.dup();
        mv.iconst(i);
        mv.aconst(names.get(i));
        mv.visitInsn(Opcodes.AASTORE);
      }
    }

    mv.iconst(numArguments);
    mv.newarray(Type.getType(SEXP.class));

    for (int i = 0; i < numArguments; i++) {
      mv.dup();
      mv.iconst(i);
      values.get(i).loadSexp(context, mv);
      mv.visitInsn(Opcodes.AASTORE);
    }
    mv.invokestatic(Type.getInternalName(ArgList.class), "of",
        Type.getMethodDescriptor(Type.getType(ArgList.class),
            Type.getType(String[].class),
            Type.getType(SEXP[].class)), false);

  }


}
