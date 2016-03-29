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
    
    StringVector elementNames = CombinedNames.combine(prefix, value);
    if(useNames) {
      if (CombinedNames.hasNames(prefix, value)) {
        haveNames = true;
      }
    }
    
    for(int i=0;i!=value.length();++i) {
      vector.addFrom(value, i);
      if(useNames) {
        names.add(elementNames.getElementAsString(i));
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

  private void addName(String name) {
    if(StringVector.isNA(name) || name.length() > 0) {
      haveNames = true;
    }

    names.add( name );
  }
}
