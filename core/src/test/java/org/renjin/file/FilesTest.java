package org.renjin.file;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.renjin.EvalTestCase;


public class FilesTest extends EvalTestCase {
	
	 @Before
	  public void setUpTests() {
	    assumingBasePackagesLoad();
	  }
	  

	@Test
	@Ignore("setwd should throw error if path doesn't exist")
	public void getSetWd(){
		eval("older<-getwd()");
		eval("setwd('/path/to/file')");
		assertThat(eval("getwd()"), equalTo(c("file:///path/to/file")));
		eval("setwd(older)");
	}
	
}
