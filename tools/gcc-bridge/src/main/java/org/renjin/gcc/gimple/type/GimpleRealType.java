package org.renjin.gcc.gimple.type;

public class GimpleRealType extends GimplePrimitiveType {
  private int precision;

  public GimpleRealType() {
  }

  public GimpleRealType(int precision) {
    this.precision = precision;
  }


  /**
   * 
   * @return The number of bits of precision
   */
  public int getPrecision() {
    return precision;
  }

  public void setPrecision(int precision) {
    this.precision = precision;
  }

  @Override
  public String toString() {
    return "real" + precision;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + precision;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GimpleRealType other = (GimpleRealType) obj;
    return precision == other.precision;
  }

}
