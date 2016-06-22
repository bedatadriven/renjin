package org.renjin.gcc.analysis;

import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.renjin.gcc.Gcc;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.*;

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