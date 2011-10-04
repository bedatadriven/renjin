package r.base;

import java.util.Arrays;
import java.util.List;

import r.lang.DoubleVector;

import com.google.common.collect.Lists;

public class MyBean {

  private String name = "fred";
  private int count;
  private List<String> children = Lists.newArrayList();

  public enum Membership {
    PENDING,
    ACTIVE,
    EXPIRED
  }
  
  private Membership membership = Membership.PENDING;
  
  public MyBean() {
    
  }
  
  public MyBean(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }
  
  public Membership getMembershipStatus() {
    return membership;
  }
  
  public void setMembershipStatus(Membership membership) {
    this.membership = membership;
  }
  
  public List<String> getChildren() {
    return Arrays.asList("Bob", "Sue");
  }

  public String sayHello(String name) {
    return "Hello " + name;
  }
  
  public String sayHello(int count) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=count;++i) {
      sb.append("Hello");
    }
    return sb.toString();
  }
  
  // should be mapped as method, not propery
  // because there is no getter
  public void setLocked(boolean locked) {
    
  }
  
  public DoubleVector compute() {
    return new DoubleVector(1,2,3);
  }
}
