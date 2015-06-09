package org.renjin.primitives.matrix;

import org.junit.Test;
import org.renjin.EvalTestCase;

public class EvalMatrixTest extends EvalTestCase {
	@Test
	public void bigMatrixAllocTest() {
		eval("matrix(nrow=10^8, ncol=1)");
	}
	
	@Test
	public void matrixSubscriptTest() {
	  eval("df <- data.frame(a=1:10000,b=1:10000)");
	  System.out.println("now");
	  eval("mm <- as.matrix(df)");
	  eval("rs <- mm[,1]");

	}
	
	@Test
	public void vecAssignTest() {
	   eval("df <- matrix(1.0,nrow=10000,ncol=10)");
     eval("df[,3] <- 1:10000");

	}
}
