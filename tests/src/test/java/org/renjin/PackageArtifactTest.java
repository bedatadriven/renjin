package org.renjin;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.renjin.eval.Context;
import org.renjin.parser.RParser;


public class PackageArtifactTest {

  @Ignore("Commenting test out because other tests verify that we can run library, and cannot reproduce the test failure.")
	@Test
	public void test() throws IOException {
	    Context context = Context.newTopLevelContext();
	    context.init();
	  
	    context.evaluate(RParser.parseSource("library(aspect, verbose=TRUE)\n"));
	}
}
