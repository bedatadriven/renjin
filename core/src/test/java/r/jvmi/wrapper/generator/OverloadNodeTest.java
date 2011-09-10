package r.jvmi.wrapper.generator;

import org.junit.Test;

import r.base.Evaluation;
import r.base.Ops;
import r.base.Types;
import r.jvmi.binding.JvmMethod;

public class OverloadNodeTest {
  
  @Test
  public void test() {

    dumpTree(Ops.class, "+");
    dumpTree(Ops.class, "==");
    dumpTree(Evaluation.class, "stop");
    dumpTree(Types.class, "environment");
  }

  private void dumpTree(Class clazz, String name) {
    OverloadNode tree = OverloadNode.buildTree(JvmMethod.findOverloads(clazz, name, name));
    System.out.println(name + ":");
    System.out.println(tree);
  }

}
