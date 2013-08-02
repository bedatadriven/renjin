package org.renjin.gnur;


import org.junit.Ignore;
import org.junit.Test;
import org.renjin.gcc.Gcc;

import java.io.File;

public class MassTest {

  @Test
  @Ignore("WIP")
  public void test() throws Exception {

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    compiler.setGimpleDirectory(new File("target/test-gimple"));
    compiler.setJimpleDirectory(new File("target/test-jimple"));
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setWorkDirectory(new File("target/gnur-work"));
    compiler.setPackageName("org.renjin.gnur.test");
    compiler.setClassName("org.renjin.gnur.test.Mass");
    compiler.setVerbose(true);
    
    File srcRoot = new File("src/test/resources/org/renjin/gnur");
    
    compiler.addSources(new File(srcRoot, "mass"));

    compiler.compile();

  }

  @Ignore("Getting there...")
  @Test
  public void testZoo() throws Exception {

    GnurSourcesCompiler compiler = new GnurSourcesCompiler();
    compiler.setGimpleDirectory(new File("target/test-gimple"));
    compiler.setJimpleDirectory(new File("target/test-jimple"));
    compiler.setOutputDirectory(new File("target/test-classes"));
    compiler.setWorkDirectory(new File("target/gnur-work"));
    compiler.setPackageName("org.renjin.gnur.test");
    compiler.setClassName("org.renjin.gnur.test.Zoo");
    compiler.setVerbose(true);

    File srcRoot = new File("src/test/resources/org/renjin/gnur");

    compiler.addSources(new File(srcRoot, "zoo"));

    compiler.compile();


  }

}
