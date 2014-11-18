package org.renjin.primitives.combine;


import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;

/**
 * Common interface to combined vector builders, for which
 * we have different implementations depending on the shape of the data
 */
interface CombinedBuilder {

  /**
   * Enables or disables copying of names to the output vector
   */
  CombinedBuilder useNames(boolean useNames);

  /**
   * Adds an S Expression to the output vector
   * @param prefix the prefix for the name if this object comes from a list that was named.
   * @param sexp an R object to add to the list
   */
  void add(String prefix, SEXP sexp);

  /**
   * Adds all of the elments in the vector {@code vectorElement} to the output vector
   *
   * @param prefix
   * @param vectorElement
   */
  void addElements(String prefix, Vector vectorElement);

  /**
   *
   * @return the vector that combines all of the input elements
   */
  Vector build();

}
