package org.renjin.primitives.combine.view;


import org.renjin.primitives.combine.CombinedNames;
import org.renjin.sexp.AtomicVector;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.Null;
import org.renjin.sexp.StringVector;

public class PrefixedNameVector extends StringVector {

  private String prefix;
  private AtomicVector namesVector;
  private int length;
  private boolean numberUnnamedElements;

  public PrefixedNameVector(String prefix, AtomicVector namesVector, boolean numberUnnamedElements, AttributeMap attributes) {
    super(attributes);
    this.numberUnnamedElements = numberUnnamedElements;
    this.prefix = CombinedNames.toString(prefix);
    this.namesVector = namesVector;
  }

  @Override
  public int length() {
    return namesVector.length();
  }

  @Override
  protected StringVector cloneWithNewAttributes(AttributeMap attributes) {
    return new PrefixedNameVector(prefix, namesVector, numberUnnamedElements, attributes);
  }

  @Override
  public String getElementAsString(int index) {
    String name = CombinedNames.EMPTY;
    if(namesVector != Null.INSTANCE) {
      name = namesVector.getElementAsString(index);
    }
    if(CombinedNames.isPresent(name)) {
      return prefix + "." + CombinedNames.toString(name);
    } else if(numberUnnamedElements) {
      return prefix + Integer.toString(index+1);
    } else {
      return prefix;
    }
  }

  @Override
  public boolean isConstantAccessTime() {
    return true;
  }
}
