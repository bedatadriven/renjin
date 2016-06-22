package org.renjin.invoke.reflection;

import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.Symbol;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;


public class ClassBindingTest extends EvalTestCase {

  private static class MyBean {

    public String getName() {
      return "foo";
    }

    public void fooberize() {
    }

    public void setFoo(int count) {
    }
    
    public static void staticTest(){
      
    }
  }

  @Test
  public void bindingTest() {
    ClassBindingImpl binding = ClassBindingImpl.get(MyBean.class);

    System.out.println(binding.getMembers());

    assertThat(binding.getMembers(),
        hasItems(Symbol.get("name"), Symbol.get("fooberize")));


    assertThat(binding.getMembers(), not(hasItem(Symbol.get("foo"))));
    assertThat(binding.getMembers(), hasItem(Symbol.get("setFoo")));
    assertThat(binding.getMembers(),not(hasItem(Symbol.get("staticTest"))));
    assertThat(binding.getStaticMembers(),hasItem(Symbol.get("staticTest")));
    assertThat(binding.getStaticMembers(),not(hasItem(Symbol.get("setFoo"))));
    
    MyBean instance = new MyBean();

    MemberBinding nameBinding = binding.getMemberBinding(Symbol.get("name"));
    assertThat(nameBinding.getValue(instance), equalTo(c("foo")));
  }
}
