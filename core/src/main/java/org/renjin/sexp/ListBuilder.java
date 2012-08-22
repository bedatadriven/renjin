package org.renjin.sexp;


/**
 * Common interface to builders of ListVector and PairLists
 *
 */
public interface ListBuilder {

  ListBuilder add(SEXP value);
  
  ListBuilder add(String name, SEXP value);

  ListBuilder add(Symbol name, SEXP value);
  
  ListBuilder set(int index, SEXP value);
   
  ListBuilder remove(int index);

  int length();
  
  SEXP build();

  int getIndexByName(String name);


}
