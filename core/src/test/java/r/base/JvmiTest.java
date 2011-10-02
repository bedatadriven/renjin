package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import r.EvalTestCase;

public class JvmiTest extends EvalTestCase {

  @Test
  public void newInstance() {
    eval("import(r.base.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat(eval("x$name"), equalTo(c("fred")));
    assertThat(eval("x$membershipStatus"), equalTo(c("PENDING")));
    assertThat(eval("x$children"), equalTo(list("Bob","Sue")));
     
    eval("x$name <- 'tom'");
    eval("x$count <- 44");
    eval("x$membershipStatus <- 'ACTIVE'");
    
    assertThat(eval("x$name"), equalTo(c("tom")));
    assertThat(eval("x$count"), equalTo(c_i(44)));
    assertThat(eval("x$membershipStatus"), equalTo(c("ACTIVE")));
   
    //eval("x$children[[3]] <- 'Rick'");
    //assertThat(eval("x$children"), equalTo(list("Bob", "Sue", "Rick")));
  }
  
  @Test
  public void newInstanceWithPropertyInit() {
    eval("import(r.base.MyBean)");
    eval("x <- MyBean$new(count=92)");
    
    assertThat( eval("x$name"), equalTo(c("fred")));
    assertThat( eval("x$count"), equalTo(c_i(92)));
  }
  
  @Test
  public void overloadedMethodCall() {
    eval("import(r.base.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat( eval("x$sayHello('fred')"), equalTo(c("Hello fred")));
    assertThat( eval("x$sayHello(3)"), equalTo(c("HelloHelloHello")));
    
  }
}
