package org.renjin.cran;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.maven.model.Build;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.renjin.cran.PackageDescription.Person;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

public class Builder {

  private File outputDir;
  private List<String> modules = Lists.newArrayList();
  
  public static void main(String[] args) throws IOException {
    Builder builder = new Builder();
    builder.outputDir = new File(args[0]);
    builder.run();
  }
  
  private void run() throws IOException {
    
    // read in the list of packages to build, we'll keep it limited to few packages
    // for the moment
    Set<String> pkgs = readPkgList();
    
    for(String pkg : pkgs) {
      String version = CRAN.fetchCurrentVersion(pkg);
      ProjectBuilder projectBuilder = new ProjectBuilder(pkg, version, outputDir);
      projectBuilder.build();
      modules.add(pkg + "_" + version);
    } 
    
    writePom();
  }
  
  private void writePom() throws IOException {

    Model model = new Model();
    model.setModelVersion("4.0.0");
    model.setArtifactId("cran-parent");
    model.setGroupId("org.renjin.cran");
    model.setVersion("1.0");
    model.setPackaging("pom");
    
    for(String module : modules) {
      model.addModule(module);
    }
   
    File pomFile = new File(outputDir, "pom.xml");
    FileWriter fileWriter = new FileWriter(pomFile);
    MavenXpp3Writer writer = new MavenXpp3Writer();
    writer.write(fileWriter, model);
    fileWriter.close();
  }


  private Set<String> readPkgList() throws IOException {
    return Sets.newHashSet(Files.readLines(new File(outputDir, "packages"), Charsets.UTF_8));
  }
 
  
}
