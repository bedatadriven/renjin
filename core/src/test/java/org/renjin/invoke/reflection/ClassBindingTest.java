/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
    assertThat(nameBinding.getValue(instance), elementsIdenticalTo(c("foo")));
  }
}
