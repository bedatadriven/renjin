package r.base;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import r.EvalTestCase;
import r.jvmi.r2j.ObjectFrame;
import r.lang.Environment;
import r.lang.Symbol;

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
    assertThat(eval("x$compute()"), equalTo(c(1,2,3)));
    //eval("x$children[[3]] <- 'Rick'");
    //assertThat(eval("x$children"), equalTo(list("Bob", "Sue", "Rick")));
  }
  
  @Test
  public void javaUtilMap() {
    eval("import(java.util.HashMap)");
    eval("map <- HashMap$new()");
    eval("map$put(1,'foo')");
    
    assertThat(eval("map$get(1)"), equalTo(c("foo")));
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
  
  private static class MyPrivateImpl implements MyPublicInterface {

    @Override
    public void doSomething() {
    }
  }
  
  @Test
  public void publicMethodCallOnPrivateObject() {
    topLevelContext.getGlobalEnvironment().setVariable(
        Symbol.get("obj"), Environment.createChildEnvironment(
            Environment.EMPTY, new ObjectFrame(new MyPrivateImpl())));
    
    eval("obj$doSomething()");
  }
  
  @Test
  public void sapplyOnLists() throws IOException {
    topLevelContext.init();
    
    eval("import(r.base.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat( eval("sapply(x$childBeans, function(x) x$count)"), equalTo(c_i(42,42)));
    
  }
  
}
