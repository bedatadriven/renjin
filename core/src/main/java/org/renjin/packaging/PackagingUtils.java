package org.renjin.packaging;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

public class PackagingUtils {

  public static List<File> findSourceFiles(File packageRoot) {
    List<File> srcFiles = Lists.newArrayList();
    for(File file : new File(packageRoot, "R").listFiles()) {
      if(file.getName().toLowerCase().endsWith(".r")) {
        srcFiles.add(file);
      }
    }
    
    Collections.sort(srcFiles, Ordering.natural().onResultOf(new Function<File, Comparable>() {
      @Override
      public Comparable apply(File input) {
        return input.getName();
      }
    }));
   
    return srcFiles;
  }
  
  /**
   * Concantenates a list of source files and writes the resulting file to 
   * {@code outputDir}/R/all.R
   * 
   * @param sources
   * @param outputDir
   * @throws IOException
   */
  public static void concatSources(List<File> sources, File libraryRoot, String packageName) throws IOException {
    File outSource = new File(libraryRoot.getAbsolutePath() + "/" + packageName + "/R/" + packageName);
    Files.createParentDirs(outSource);
    
    PrintWriter writer = new PrintWriter(outSource);
    for(File source : sources) {
      writer.println("#line 1 \"" + source.getName() + "\"");
      writer.println(Files.toString(source, Charsets.UTF_8));
    }
    
    writer.close();
  }
}
