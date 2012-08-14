package org.renjin.gcc.jimple;


import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class JimpleOutput {

  private static final Logger LOGGER = Logger.getLogger(JimpleOutput.class.getName());

  private List<AbstractClassBuilder> classes = Lists.newArrayList();

  public JimpleClassBuilder newClass() {
    JimpleClassBuilder builder = new JimpleClassBuilder(this);
    classes.add(builder);
    return builder;
  }

  public JimpleInterfaceBuilder newInterface() {
    JimpleInterfaceBuilder builder = new JimpleInterfaceBuilder();
    classes.add(builder);
    return builder;
  }

  public void write(File outputDir) throws IOException {
    outputDir.mkdirs();

    for(AbstractClassBuilder clazz : classes) {
      File jimpleSource = new File(outputDir, clazz.getFqcn() + ".jimple");
      LOGGER.info("Writing jimple source to " + jimpleSource);

      JimpleWriter writer = new JimpleWriter(jimpleSource);
      clazz.write(writer);
      writer.close();

      java.lang.System.out.println(Files.toString(jimpleSource, Charsets.UTF_8));

    }
  }

  public Set<String> getClassNames() {
    Set<String> classNames = Sets.newHashSet();
    for(AbstractClassBuilder clazz : classes) {
      classNames.add(clazz.getFqcn());
    }
    return classNames;
  }
}
