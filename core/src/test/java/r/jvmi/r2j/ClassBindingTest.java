package r.jvmi.r2j;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import r.EvalTestCase;
import r.lang.Symbol;

public class ClassBindingTest extends EvalTestCase {

  private static class MyBean {
    
    public String getName() {
      return "foo";
    }
    public void fooberize() { }
    public void setFoo(int count) { }
  }
  
  @Test
  public void bindingTest() {
    ClassBinding binding = ClassBinding.get(MyBean.class);
    
    System.out.println(binding.getMembers());
    
    assertThat(binding.getMembers(), hasItems(Symbol.get("name"), Symbol.get("fooberize")) );
    assertThat(binding.getMembers(), not(hasItem(Symbol.get("wait"))));
    assertThat(binding.getMembers(), not(hasItem(Symbol.get("notifyAll"))));
    
    assertThat(binding.getMembers(), not(hasItem(Symbol.get("foo"))) );
    assertThat(binding.getMembers(), hasItem(Symbol.get("setFoo")) );
    
    MyBean instance = new MyBean();
    
    MemberBinding nameBinding = binding.getMemberBinding(Symbol.get("name"));
    assertThat(nameBinding.getValue(instance), equalTo(c("foo")));
  }
}
