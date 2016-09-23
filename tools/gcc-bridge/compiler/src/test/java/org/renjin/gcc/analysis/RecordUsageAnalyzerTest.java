/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.gcc.analysis;

import org.junit.Test;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.io.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RecordUsageAnalyzerTest {

  
  @Test
  public void simpleStruct() throws IOException {
    
    GimpleCompilationUnit unit = compile("simple_record.c");
    
    RecordUsageAnalyzer analyzer = new RecordUsageAnalyzer(unit.getRecordTypes());
    analyzer.analyze(Collections.singletonList(unit));

    GimpleRecordTypeDef simple_t = Iterables.getOnlyElement(unit.getRecordTypes());
    
    assertTrue(analyzer.unitPointerAssumptionsHold.contains(simple_t.getId()));
  }

  @Test
  public void mallocRecord() throws IOException {

    GimpleCompilationUnit unit = compile("malloc_record.c");

    RecordUsageAnalyzer analyzer = new RecordUsageAnalyzer(unit.getRecordTypes());
    analyzer.analyze(Collections.singletonList(unit));

    GimpleRecordTypeDef simple_t = Iterables.getOnlyElement(unit.getRecordTypes());

    assertFalse(analyzer.unitPointerAssumptionsHold.contains(simple_t.getId()));
  }
  


  private GimpleCompilationUnit compile(String sourceName) throws IOException {
    URL sourceResource = Resources.getResource(RecordUsageAnalyzerTest.class, sourceName);
    String sourceFile = sourceResource.getFile();

    Gcc gcc = new Gcc();
    gcc.extractPlugin();
    return gcc.compileToGimple(new File(sourceFile));
  }


}