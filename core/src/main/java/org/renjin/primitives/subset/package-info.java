/**
 * Implementation of builtin subset operators {@code [}, {@code [[}, {@code $} and {@code @}
 * 
 * <p>These operators have complex behavior and are implemented in Renjin as follows:</p>
 * 
 * <ul>
 *   <li>The class {@link org.renjin.primitives.subset.Subsetting} provide the entry point for the builtins and
 *   perform very basic argument matching.</li>
 *   <li>These methods call {@link org.renjin.primitives.subset.Selections#parseSelection(org.renjin.sexp.SEXP, java.util.List)}
 *   to parse the provided arguments into a class provided subscripts are parsed into {@link org.renjin.primitives.subset.SelectionStrategy} objects depending 
 </li>
 * </ul>
 */
package org.renjin.primitives.subset;