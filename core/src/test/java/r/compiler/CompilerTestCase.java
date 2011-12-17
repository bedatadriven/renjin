package r.compiler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import r.compiler.cfg.BasicBlock;
import r.compiler.cfg.ControlFlowGraph;
import r.compiler.ir.tac.IRScope;
import r.compiler.ir.tac.IRScopeBuilder;
import r.compiler.ir.tac.IRFunctionTable;
import r.lang.ExpressionVector;
import r.parser.RParser;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class CompilerTestCase {

  protected IRFunctionTable functionTable = new IRFunctionTable();

  protected IRScope buildScope(String rcode) {
    ExpressionVector ast = RParser.parseSource(rcode + "\n");
    return new IRScopeBuilder(functionTable).build(ast);
  }  
  

  protected final IRScope parseCytron() throws IOException {
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
