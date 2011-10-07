package r;

import java.io.IOException;

import org.junit.Test;

import r.lang.Context;
import r.parser.RParser;

public class PackageArtifactTest {

	@Test
	public void test() throws IOException {
	    Context context = Context.newTopLevelContext();
	    context.init();
	  
	    RParser.parseSource("library(aspect, verbose=TRUE)\n")
	    	.evaluate(context, context.getEnvironment());
	}
}
