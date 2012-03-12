package r.base.file;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import r.EvalTestCase;

public class FilesTest extends EvalTestCase {
	
	 @Before
	  public void setUpTests() {
	    assumingBasePackagesLoad();
	  }
	  

	@Test
	public void getSetWd(){
		eval("older<-getwd()");
		eval("setwd('/path/to/file')");
		assertThat(eval("getwd()"), equalTo(c("file:///path/to/file")));
		eval("setwd(older)");
	}
	
}
