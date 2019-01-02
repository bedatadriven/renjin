/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.invoke.reflection;


import org.junit.Test;
import org.renjin.EvalTestCase;
import org.renjin.sexp.Logical;

import static org.junit.Assert.assertThat;
import static org.renjin.ExpMatchers.logicalVectorOf;

public class TypeConverterTest extends EvalTestCase {
  @Test
  public void basicConvertTest() {
    eval("import(org.renjin.invoke.reflection.CallJavaFromRTest)");
    eval("boolValue<-TRUE");
    eval("boolInstance<-CallJavaFromRTest$new(boolValue)");
    assertThat(
        eval("if(boolInstance$boolConverted==boolValue) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("intValue<-as.integer(1)");
    eval("intInstance<-CallJavaFromRTest$new(intValue)");
    assertThat(eval("if(intInstance$intConverted==intValue) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("doubleValue<-2.3");
    eval("doubleInstance<-CallJavaFromRTest$new(doubleValue)");
    assertThat(
        eval("if(doubleInstance$doubleConverted==doubleValue) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("stringValue<-\"testStringValue\"");
    eval("stringInstance<-CallJavaFromRTest$new(stringValue)");
    assertThat(
        eval("if(stringInstance$stringConverted==stringValue) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void arrayConvertTest() {
    eval("import(org.renjin.invoke.reflection.CallJavaFromRTest)");
    eval("inBooleanArray<-c(TRUE,FALSE,FALSE)");
    eval("outRBooleanArray<-(inBooleanArray!=TRUE)");
    eval("outBooleanArray<-CallJavaFromRTest$BooleanArrayConvert(inBooleanArray)");
    assertThat(
        eval("if(!any(outRBooleanArray!=outBooleanArray)) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("inIntArray<-as.integer(c(1,3,5))");
    eval("outRIntArray<-inIntArray+1");
    eval("outIntArray<-CallJavaFromRTest$IntArrayConvert(inIntArray)");
    assertThat(eval("if(!any(outRIntArray!=outIntArray)) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("inDoubleArray<-c(1.2,4.5,7.3)");
    eval("outRDoubleArray<-inDoubleArray+1");
    eval("outDoubleArray<-CallJavaFromRTest$DoubleArrayConvert(inDoubleArray)");
    assertThat(
        eval("if(!any(outRDoubleArray!=outDoubleArray)) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
    eval("inStringArray<-c(\"test\",\"StringArray\",\"ok\")");
    eval("outStringArray<-CallJavaFromRTest$StringArrayConvert(inStringArray)");
    assertThat(eval("if(!any(inStringArray!=outStringArray)) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void specilConvertTest() {
    eval("import(org.renjin.invoke.reflection.CallJavaFromRTest)");
    eval("import(org.renjin.invoke.reflection.MyCakeTest)");
    eval("cake<-MyCakeTest$new()");
    eval("instance<-CallJavaFromRTest$new(cake)");
    eval("cake$digJam()");
    eval("instance$cakeConverted$digJam()");
    assertThat(
        eval("if(instance$cakeConverted$jam==cake$jam) TRUE else FALSE"),
        logicalVectorOf(Logical.TRUE));

    // TODO Special Object Array
    // eval("import(org.renjin.invoke.reflection.CallJavaFromRTest)");
    // eval("import(org.renjin.invoke.reflection.MyCakeTest)");
    // eval("cake<-MyCakeTest$new()");
    // eval("instance<-CallJavaFromRTest$new(cake)");
    // eval("instance$cakeConverted$digJam()");
    // assertThat(eval("if(instance$cakeConverted==cake) TRUE else FALSE"),logicalVectorOf(Logical.TRUE));
  }

  @Test
  public void catchExceptions() {
    eval("import(org.renjin.invoke.reflection.CallJavaFromRTest)");
    eval("x <- tryCatch(CallJavaFromRTest$throwException(), error=function(e) e$message) ");
    eval("print(x)");
  }
}
