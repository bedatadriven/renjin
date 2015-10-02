package org.renjin.gcc;


import org.junit.Test;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.translate.type.TypeResolver;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class CompilerTest extends AbstractGccTest {

  @Test
  public void sqrtTest() throws Exception {

    // Stage 0: Invoke GCC to compile to Gimple
    GimpleCompilationUnit unit = compileToGimple("sqr.c");


    // Stage 1: Resolve types to intermediate type models
    TypeResolver resolver = new TypeResolver();
    resolver.resolve(Collections.singletonList(unit));

    GimpleCompiler compiler = new GimpleCompiler();
    compiler.setClassName("Sqr");
    compiler.setPackageName("org.renjin");
    byte[] classFile = compiler.compileToBytecode(Collections.singletonList(unit));

    MyClassLoader classLoader = new MyClassLoader();
    Class<?> theClass = classLoader.defineClass("org.renjin.Sqr", classFile);

    Method method = theClass.getMethod("sqr", double.class);
    Double result = (Double) method.invoke(null, 2.0);
    
    assertThat(result.doubleValue(), equalTo(4.0));
  }
  
  private static class MyClassLoader extends ClassLoader {
    public Class defineClass(String name, byte[] b) {
      return super.defineClass(name, b, 0, b.length);
    }
  }
  
  
}
