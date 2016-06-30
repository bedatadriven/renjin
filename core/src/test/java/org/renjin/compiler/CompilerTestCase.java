package org.renjin.compiler;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Resources;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.renjin.compiler.cfg.BasicBlock;
import org.renjin.compiler.cfg.ControlFlowGraph;
import org.renjin.compiler.ir.tac.IRBody;
import org.renjin.compiler.ir.tac.IRBodyBuilder;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.sexp.ExpressionVector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class CompilerTestCase {


  public static void print(int i, int j, int k, int l) {
    System.out.println(Joiner.on(", ").join(Arrays.asList(i, j, k, l)));
  }


  protected IRBody buildScope(String rcode) {
    Session session = new SessionBuilder().build();
    
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    return new IRBodyBuilder(session.getTopLevelContext(), session.getGlobalEnvironment()).build(ast);
  }
  

  protected final IRBody parseCytron() throws IOException {
    return buildScope(Resources.toString(Resources.getResource(ControlFlowGraph.class, "cytron.R"), Charsets.UTF_8));
  }
  
  protected final Matcher<Collection<BasicBlock>> itemsEqualTo(final BasicBlock... blocks) {
    return new TypeSafeMatcher<Collection<BasicBlock>>() {

      @Override
      public void describeTo(Description d) {
        d.appendValue(Arrays.toString(blocks));
      }

      @Override
      public boolean matchesSafely(Collection<BasicBlock> collection) {
        if(collection.size() != blocks.length) {
          return false;
        }
        for(BasicBlock bb : blocks) {
          if(!collection.contains(bb)) {
            return false;
          }
        }
        return true;
      }
    };
  }
}
