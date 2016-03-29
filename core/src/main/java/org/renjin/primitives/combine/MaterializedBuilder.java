package org.renjin.primitives.combine;

import org.renjin.sexp.SEXP;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

class MaterializedBuilder implements CombinedBuilder {

  private Vector.Builder vector;

  private StringVector.Builder names = new StringVector.Builder();
  private boolean haveNames = false;
  private boolean useNames = false;

  MaterializedBuilder(Vector.Type resultType) {
    this.vector = resultType.newBuilder();
  }

  @Override
  public CombinedBuilder useNames(boolean useNames) {
    this.useNames = useNames;
    return this;
  }

  @Override
  public void add(String prefix, SEXP sexp) {
    vector.add(sexp);
    addName(prefix);
  }

  @Override
  public void addElements(String prefix, Vector value) {
    for(int i=0;i!=value.length();++i) {
      vector.addFrom(value, i);
      if(useNames) {
        addNameFrom(prefix, value, i);
      }
    }
  }

  @Override
  public Vector build() {

    if(haveNames) {
      vector.setAttribute(Symbols.NAMES, names.build());
    }
    return vector.build();
  }

  private void addNameFrom(String prefix, SEXP vector, int index) {
    
    boolean hasPrefix = !"".equals(prefix);
    boolean hasElementName = false;
    String elementName = "";
    
    if(vector.getAttributes().hasNames()) {
      elementName = vector.getName(index);
      hasElementName = !"".equals(elementName);
    }

    if(hasPrefix && hasElementName) {
      addName(toName(prefix) + "." + toName(elementName));

    } else if(hasElementName) {
      addName(elementName);
    
    } else if(hasPrefix && vector.length() > 1) {
      addName(toName(prefix) + (index+1));

    } else if(hasPrefix) {
      addName(prefix);

    } else {
      addName("");
    }
  }
  
  private String toName(String name) {
    if(StringVector.isNA(name)) {
      return "NA";
    } else {
      return name;
    }
  }

  private void addName(String name) {
    if(StringVector.isNA(name) || name.length() > 0) {
      haveNames = true;
    }

    names.add( name );
  }
}
