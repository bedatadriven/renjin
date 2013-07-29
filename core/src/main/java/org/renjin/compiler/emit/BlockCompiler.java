package org.renjin.compiler.emit;


import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.LabelNode;
import org.renjin.compiler.CompiledBody;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.ssa.VariableMap;
import org.renjin.compiler.ir.tac.statements.Statement;
import org.renjin.compiler.ir.tree.TreeBuilder;
import org.renjin.eval.Context;
import org.renjin.sexp.Environment;
import org.renjin.sexp.SEXP;

import java.util.List;
import java.util.Map;

import static me.qmx.jitescript.util.CodegenUtils.sig;

public class BlockCompiler {

  private VariableMap variableMap;
  private ControlFlowGraph cfg;
  private TreeBuilder treeBuilder;

  public static class DynamicClassLoader extends ClassLoader {
    public Class<?> define(JiteClass jiteClass) {
      byte[] classBytes = jiteClass.toBytes();
      return super.defineClass(jiteClass.getClassName(), classBytes, 0, classBytes.length);
    }
  }

  private final Map<BasicBlock, Label> labels = Maps.newHashMap();

  public CompiledBody compile(final ControlFlowGraph cfg) {
    String className = "compiled" + System.identityHashCode(this);
    String interfaces[] = new String[] { "org/renjin/compiler/CompiledBody" };

    this.cfg = cfg;
    this.variableMap = new VariableMap(cfg);
    this.treeBuilder = new TreeBuilder(cfg, variableMap);

    // define mapping of bb = > labels
    for(BasicBlock bb : cfg.getBasicBlocks()) {
      labels.put(bb, new Label());
    }

    // emit the actual class!
    JiteClass jiteClass = new JiteClass(className, interfaces) {{
      defineDefaultConstructor();
      defineMethod("evaluate", ACC_PUBLIC, sig(SEXP.class, Context.class, Environment.class), new CodeBlock() {{
        for(BasicBlock block : cfg.getBasicBlocks()) {
          emitBasicBlock(this, block);
        }
        aconst_null();
        areturn();
      }});
    }};

    try {
      return (CompiledBody) new DynamicClassLoader().define(jiteClass).newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void emitBasicBlock(CodeBlock codeBlock, BasicBlock block) {
    // define the label that starts this basic block
    codeBlock.label(new LabelNode(labels.get(block)));

    // now compose this bb into a tree
    List<Statement> statements = treeBuilder.build(block);

    System.out.println(Joiner.on("\n").join(statements));
  }
}
