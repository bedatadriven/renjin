/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.packaging;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;
import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.primitives.packaging.Namespace;
import org.renjin.sexp.Closure;
import org.renjin.sexp.NamedValue;

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
    
    assertThat(f.getEnclosingEnvironment(), equalTo(
        tlContext.getNamespaceRegistry()
                 .getNamespace(ctx, "testns")
                 .getNamespaceEnvironment()));
    
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
