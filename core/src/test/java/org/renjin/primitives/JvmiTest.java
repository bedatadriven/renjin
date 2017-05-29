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
package org.renjin.primitives;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.ExternalPtr;
import org.renjin.util.DataFrameBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
  public void toDataFrame() {
    
    List<MyBean> beans = Arrays.asList(new MyBean("Huey"), new MyBean("Louey"), new MyBean("Dewey"));
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "df", DataFrameBuilder.build(MyBean.class, beans));
    
    eval("print(df)");
    
    assertThat(eval("df$name"), equalTo(c("Huey", "Louey", "Dewey")));
  }
  
  
  @Test
  public void classObject() {
    eval("import(java.lang.Class)");
    eval("implName <- 'java.util.HashMap'");
    eval("m <- Class$forName(implName)$new()");
    assertThat(eval("m$size()"), equalTo(c_i(0)));
  }

  @Test
  public void staticFields() {
    eval("import(java.lang.Integer)");
    assertThat(eval("Integer$MAX_VALUE"), equalTo(c_i(Integer.MAX_VALUE)));
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

    // Should also be able to use getters explicitly
    assertThat( eval("x$getName()"), equalTo(c("fred")));
    assertThat( eval("x$getCount()"), equalTo(c_i(92)));
    
    // Property notation setting...
    eval("x$name <- 'bob'");
    assertThat( eval("x$name"), equalTo(c("bob")));

    // As well as explicit setters
    eval("x$setCount(433L)");
    assertThat( eval("x$getCount()"), equalTo(c_i(433)));

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

  @Test
  public void floatArguments() {
    eval("import(org.renjin.primitives.MyBean)");
    assertThat( eval("MyBean$sum32(2, 3)"), equalTo(c(5)));
  }

  @Test
  public void floatArrayArguments() {
    eval("import(org.renjin.primitives.MyBean)");
    assertThat( eval("MyBean$sumArray32(c(2, 3, 9))"), equalTo(c(14)));
    assertThat( eval("MyBean$sumArray32(c(2L, 3L, 9L))"), equalTo(c(14)));
    assertThat( eval("MyBean$sumArray32(numeric(0))"), equalTo(c(0)));
  }

  private static class MyPrivateImpl implements MyPublicInterface {

    @Override
    public void doSomething() {
    }
  }
  
  @Test
  public void publicMethodCallOnPrivateObject() {
    topLevelContext.getGlobalEnvironment().setVariable(topLevelContext, "obj", new ExternalPtr(new MyPrivateImpl()));
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
    
    assertThat(eval("sapply(x$childBeans, function(x) x$count)"), equalTo(c_i(42, 42)));
  }

  @Test
  public void longIsNotMangled() {

    eval("import(org.renjin.primitives.MyBean)");
    eval("x <- MyBean$calculateLong()");
    eval("MyBean$useLongValue(x)");

  }

  @Test
  public void classProperty() {
    eval("import(java.util.HashMap)");
    eval("ageMap <- HashMap$new()");

    assertThat(eval("ageMap$class$name"), equalTo(c("java.util.HashMap")));
    assertThat(eval("ageMap$getClass()$getName()"), equalTo(c("java.util.HashMap")));

  }
}
