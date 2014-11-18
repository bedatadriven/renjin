package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.ExternalPtr;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;


public class JvmiTest extends EvalTestCase {

  @Test
  public void newInstance() {
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new()"  );
    
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
  public void classObject() {
    eval("import(java.lang.Class)");
    eval("implName <- 'java.util.HashMap'");
    eval("m <- Class$forName(implName)$new()");
    assertThat(eval("m$size()"), equalTo(c_i(0)));
  }

  @Test
  public void javaUtilMap() {
    eval("import(java.util.HashMap)");
    eval("map <- HashMap$new()");
    eval("map$put(1,'foo')");
    
    assertThat(eval("map$get(1)"), equalTo(c("foo")));
  }
  
  @Test
  public void varArgs() {
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat(eval("x$intVarArg('hello')"), equalTo(c_i(0)));
  }
  
  @Test
  public void newInstanceWithPropertyInit() {
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new(count=92)");
    
    assertThat( eval("x$name"), equalTo(c("fred")));
    assertThat( eval("x$count"), equalTo(c_i(92)));
  }
  
  @Test
  public void overloadedMethodCall() {
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat( eval("x$sayHello('fred')"), equalTo(c("Hello fred")));
    //use strong type or week type?
    assertThat( eval("x$sayHello(as.integer(3))"), equalTo(c("HelloHelloHello")));
    
  }
  
  @Test
  public void callToArray() {
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat( eval("x$sayHelloToEveryone(c('Bob', 'Steve', 'Ted'))"), 
        equalTo(c("Hello Bob, Steve, Ted")));
  }
  
  @Test
  public void vectorToVargs() {
    eval("import(org.renjin.primitives.MyBean)");
    assertThat( eval("MyBean$sum(1:5)"), equalTo(c(15)));
  }
  
  private static class MyPrivateImpl implements MyPublicInterface {

    @Override
    public void doSomething() {
    }
  }
  
  @Test
  public void publicMethodCallOnPrivateObject() {
    topLevelContext.getGlobalEnvironment().setVariable("obj", new ExternalPtr(new MyPrivateImpl()));
    eval("obj$doSomething()");
  }
  
  @Test
  public void withContext() {
    eval("import(org.renjin.primitives.MyBean)");

    eval("x <- MyBean$new()");
    eval("x$methodWithContext('hello!')");
  }
  
  @Test
  public void sapplyOnLists() throws IOException {
    assumingBasePackagesLoad();
    
    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$new()");
    
    assertThat( eval("sapply(x$childBeans, function(x) x$count)"), equalTo(c_i(42,42)));
  }

  @Test
  public void longIsNotMangled() {

    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$calculateLong()");
    eval("MyBean$useLongValue(x)");

  }

  
}
