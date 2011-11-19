package r.compiler.ir.tac;

import java.util.List;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TacClassWriter {
  
  
  public void write(List<Node> nodes) {
    ClassWriter writer = new ClassWriter(0);
    writer.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "r/compiled/frag1", null, "java/lang/Object",
        null);
    writer.visitMethod(Opcodes.ACC_PUBLIC, "main", "();void", null, null);
  }

}
