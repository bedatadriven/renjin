/**
 * The package {@code org.renjin.invoke} packages contains classes that support the invocation
 * of JVM methods from R code.
 *
 * <p>The basic challenge is to map a function call from R, with it's list of named arguments,
 * to a method on a JVM class. This mechanism is used in three scenarios:</p>
 * <ul>
 *   <li>R Builtin functions, including operators, {@code c}, {@code rbind}, etc</li>
 *   <li>Supporting implementation in packages</li>
 *   <li>Ad-hoc interaction with JVM libraries</li>
 * </ul>
 *
 * <p>There are several things that we need to do:</p>
 *
 * <ul>
 *   <li>Argument matching</li>
 *   <li>@DataParallel methods that operate on a all elements of an array</li>
 * </ul>
 * There are three principal ways to do this:</p>
 * <ul>
 *   <li><strong>Reflection</strong> - using the </li>
 *   <li><strong>Code generation</strong></li>
 *   <li><strong>Method Handles</strong></li>
 * </ul>
 *
 * <p>In order to be compatible with GNU R, and to offer flexible options to users to
 * play easily with the JVM, there are several ways to interact with JVM objects
 * from Renjin:</p>
 *
 * <h3>Compatibility:</h3>
 * <ul>
 *   <li>.Internal(methodName) - see {@link org.renjin.primitives.special.InternalFunction}</li>
 *   <li>.Call(methodName, CLASS=XX, PACKAGE=XX) - See </li>
 *   <li>.C / .Fortran - provided by the legacy-runtime library</li>
 *   <li>rJava - TODO - write an rJava replacement for Renjin
 *   </li>
 * </ul>
 *
 * <h3>Renjin-specific methods</h3>
 *
 *
 *
 *
 */
package org.renjin.invoke;