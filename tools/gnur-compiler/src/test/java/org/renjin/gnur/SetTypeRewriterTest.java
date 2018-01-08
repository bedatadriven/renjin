/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gnur;

import org.junit.Test;
import org.renjin.gcc.NullTreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleFunction;
import org.renjin.gcc.gimple.GimpleParser;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.IOException;

public class SetTypeRewriterTest {

  @Test
  public void test() throws IOException {

    File gimpleFile = new File(Resources.getResource(SetTypeRewriter.class, "typeof.gimple").getFile());

    GimpleParser parser = new GimpleParser();
    GimpleCompilationUnit unit = parser.parse(gimpleFile);
    GimpleFunction function = Iterables.getOnlyElement(unit.getFunctions());

    System.out.println(function);

    SetTypeRewriter rewriter = new SetTypeRewriter();
    rewriter.transform(new NullTreeLogger(), unit, function);

    System.out.println("AFTER TRANSFORM");

    System.out.println(function);
  }

}