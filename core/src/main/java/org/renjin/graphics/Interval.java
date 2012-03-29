package org.renjin.graphics;

public final class Interval {
  private final double min;
  private final double max;
  private final int tickCount;
 
  
  public Interval(double min, double max, int tickCount) {
    super();
    this.min = min;
    this.max = max;
    this.tickCount = tickCount;
  }

  public double getMin() {
    return min;
  }

  public double getMax() {
    return max;
  }
  
  public int getCount() {
    return tickCount;
  }
 
  
  public boolean isInfinite() {
    return Double.isInfinite(min) || 
        Double.isInfinite(max) ||
        Double.isInfinite(max-min);
  }
  
}
