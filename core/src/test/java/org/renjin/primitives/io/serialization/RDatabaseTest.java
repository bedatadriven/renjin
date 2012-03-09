package org.renjin.primitives.io.serialization;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;
import org.renjin.primitives.io.serialization.RDatabase;

import r.EvalTestCase;
import r.lang.Environment;
import r.lang.Symbol;

public class RDatabaseTest extends EvalTestCase {

  @Test
  public void test() throws IOException, DataFormatException {
    
    topLevelContext.getGlobals().namespaceRegistry.setVariable(
        Symbol.get("survey"), Environment.createChildEnvironment(topLevelContext.getGlobalEnvironment()));
    
    topLevelContext.getGlobals().namespaceRegistry.setVariable(
        Symbol.get("graphics"), Environment.createChildEnvironment(topLevelContext.getGlobalEnvironment()));
    
    
    String rdbFile = getClass().getResource("/survey/R/survey.rdb").getFile();
    RDatabase rdb = new RDatabase(topLevelContext, rdbFile);
    for(String name : rdb.keySet()) {
      System.out.println(name);
    }
  }
  
}
