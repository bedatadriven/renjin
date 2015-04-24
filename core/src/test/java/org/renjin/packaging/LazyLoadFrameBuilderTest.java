package org.renjin.packaging;

import com.google.common.io.Files;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.Closure;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.Symbol;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class LazyLoadFrameBuilderTest {

  @Ignore("todo")
  @Test
  public void test() throws Exception {
    
    Context tlContext = Context.newTopLevelContext();
    Namespace ns = tlContext.getNamespaceRegistry().createNamespace(new TestPackage());
    
    Context ctx = tlContext.beginEvalContext(ns.getNamespaceEnvironment());
    ctx.evaluate(RParser.parseSource("f <- function(x) x*x*42\n"));
    
    File envFile = File.createTempFile("nstest", ".RData");
    envFile.deleteOnExit();
    
    LazyLoadFrameBuilder builder = new LazyLoadFrameBuilder(tlContext);
    builder.outputTo(envFile);
    builder.build(ns.getNamespaceEnvironment());
    
    // now reload into a new context
    tlContext = Context.newTopLevelContext();
    tlContext.getNamespaceRegistry().createNamespace(new TestPackage());
    
    Iterable<NamedValue> namedValues = LazyLoadFrame.load(null, null);
    NamedValue namedValue = namedValues.iterator().next();
    assertThat(namedValue.getName(),equalTo("f"));
    Closure f = (Closure) namedValue.getValue().force(tlContext);
    
    assertThat(f.getEnclosingEnvironment(), equalTo(tlContext.getNamespaceRegistry().getNamespace("testns").getNamespaceEnvironment()));
    
  }
  
  private static class TestPackage extends org.renjin.primitives.packaging.Package {

    protected TestPackage() {
      super(FqPackageName.cranPackage("testns"));
    }

    @Override
    public Class loadClass(String name) {
      throw new UnsupportedOperationException();
    }
  }
}
