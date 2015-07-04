package org.renjin.primitives.combine;

import com.google.common.base.Strings;
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
    // The resulting name starts with the argument's
    // tag, if any
    StringBuilder name = new StringBuilder(prefix);

    // if this element has itself a name, then append it
    // to the name, delimiting with a '.' if necessary
    String elementName = vector.getName(index);
    if(!Strings.isNullOrEmpty(elementName)) {
      if(name.length() > 0) {
        name.append('.');
      }
      name.append(elementName);
    } else {

      // if this element has no name of its own, but we're
      // inheriting a name from the argument, AND this vector has
      // multiple values, then we distinguish this element's name
      // from the others in the vector by appending the
      // element's (1-based) index

      if(name.length() > 0 && vector.length() > 1) {
        name.append(index + 1);
      }
    }

    addName(name.toString());
  }

  private void addName(String name) {
    if(name.length() > 0) {
      haveNames = true;
    }

    names.add( name );
  }
}
