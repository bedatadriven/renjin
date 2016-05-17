package org.renjin.gcc.gimple.expr;

import com.google.common.base.Predicate;

import java.util.List;
import java.util.Objects;

public class GimplePointerPlus extends GimpleExpr {

  private GimpleExpr pointer;
  private GimpleExpr offset;

  public GimpleExpr getPointer() {
    return pointer;
  }

  public void setPointer(GimpleExpr pointer) {
    this.pointer = pointer;
  }

  public GimpleExpr getOffset() {
    return offset;
  }

  public void setOffset(GimpleExpr offset) {
    this.offset = offset;
  }

  @Override
  public void find(Predicate<? super GimpleExpr> predicate, List<GimpleExpr> results) {
    pointer.find(predicate, results);
    offset.find(predicate, results);
  }

  @Override
  public boolean replace(Predicate<? super GimpleExpr> predicate, GimpleExpr replacement) {
    if(pointer.replace(predicate, replacement)) {
      return true;
    }
    if(offset.replace(predicate, replacement)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GimplePointerPlus that = (GimplePointerPlus) o;

    return Objects.equals(this.pointer, that.pointer) &&
           Objects.equals(this.offset, that.offset);
  }

  @Override
  public int hashCode() {
    int result = pointer != null ? pointer.hashCode() : 0;
    result = 31 * result + (offset != null ? offset.hashCode() : 0);
    return result;
  }
}
