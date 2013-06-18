package org.renjin.invoke.reflection;

public class MyCakeTest{
  private Boolean jam = false;
  public Boolean getJam() {
    return jam;
  }
  public void setJam(Boolean jam) {
    this.jam = jam;
  }
  public MyCakeTest digJam(){
    this.jam = true;
    return this;
  }
  @Override
  public String toString() {
    return "It's MyCakeTest,jam is "+ jam;
  }
}
