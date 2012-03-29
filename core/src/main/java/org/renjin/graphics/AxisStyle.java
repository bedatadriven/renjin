package org.renjin.graphics;

public class AxisStyle {

  private AxisIntervalCalculationStyle calculationStyle = AxisIntervalCalculationStyle.REGULAR;
  private int tickmarkCount = 5;
  
  public AxisIntervalCalculationStyle getCalculationStyle() {
    return calculationStyle;
  }
  public void setCalculationStyle(AxisIntervalCalculationStyle calculationStyle) {
    this.calculationStyle = calculationStyle;
  }
  
  public int getTickmarkCount() {
    return tickmarkCount;
  }
  public void setTickmarkCount(int tickmarkCount) {
    this.tickmarkCount = tickmarkCount;
  }
  @Override
  protected AxisStyle clone() {
    AxisStyle clone = new AxisStyle();
    clone.calculationStyle = calculationStyle;
    clone.tickmarkCount = tickmarkCount;
    return clone;
  }
  
  
}
