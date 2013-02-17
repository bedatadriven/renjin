package org.renjin.cran;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.renjin.cran.PackageDescription.PackageDependency;
import org.renjin.cran.PackageDescription.Person;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

public class ProjectBuilder {
  private static final String RENJIN_VERSION = "0.7.0-SNAPSHOT";
  private String pkg;
  private String version;
  private File baseDir;
  private File rSourcesDir;
  private File rTestsDir;
  private File resourcesDir;
  private Properties datasets = new Properties();
  
  private Set<String> corePackages = Sets.newHashSet("stats", "stats4", "graphics", "grDevices", "utils", "methods", "datasets", "splines");

  public ProjectBuilder(String pkgName, String version, File outputDir) throws IOException {
    this.pkg = pkgName;
    this.version = version;
    this.baseDir = new File(outputDir, pkg + "_" + version);
    this.rSourcesDir = new File(baseDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "R");
    this.resourcesDir = new File(baseDir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "resources" +
          File.separator + ("org.renjin.cran." + pkgName).replace('.', File.separatorChar));

    this.rTestsDir = new File(baseDir.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "R");

    this.baseDir.mkdirs();
  }
  
  public void build() throws IOException {
    File sourceArchive = downloadSourceArchive();
    unpackSources(sourceArchive);
    writeDatasetIndex();
    writePom(buildPom());
  }

  public File downloadSourceArchive() throws IOException {

    File userHome = new File(System.getProperty("user.home"));
    File sourcesDir = new File(userHome, "cranSources");
    sourcesDir.mkdir();

    String archiveName = pkg + "_" + version + ".tar.gz";
    File archiveFile = new File(sourcesDir, archiveName);
    if(archiveFile.exists()) {
      return archiveFile;
    }

    CRAN.downloadSrc(pkg, version, sourcesDir);

    return archiveFile;
  }

  private void unpackSources(File sourceArchive) throws IOException {
    FileInputStream in = new FileInputStream(sourceArchive);
    GZIPInputStream gzipIn = new GZIPInputStream(in);
    TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn);
           
    TarArchiveEntry entry;
    while((entry=tarIn.getNextTarEntry()) != null) {
      if(entry.getName().endsWith(".Rd")) {
        
      } else if(entry.getName().startsWith(pkg + "/src/") &&
          entry.getSize() != 0) {
        
        
      } else if(entry.getName().startsWith(pkg + "/R/") && entry.getSize() != 0) {
        extractTo(entry, tarIn, rSourcesDir);
        
      } else if(entry.getName().equals(pkg + "/DESCRIPTION")) {
        extractTo(entry, tarIn, baseDir);
        
      } else if(entry.getName().equals(pkg + "/NAMESPACE")) {
        extractTo(entry, tarIn, baseDir);
      
      } else if(entry.getName().startsWith(pkg + "/tests/") && entry.getSize() != 0) {
        extractTo(entry, tarIn, rTestsDir);
        
      } else if(entry.getName().startsWith(pkg + "/data/") && entry.getSize() != 0) {
        extractTo(entry, tarIn, resourcesDir);
        addDataset(entry);
      }
    } 
  }

  private void addDataset(TarArchiveEntry entry) {
    String path = entry.getName();
    int lastSlash = path.lastIndexOf('/');
    String name = path.substring(lastSlash+1);
    datasets.setProperty(stripExt(name), name);
  }


  private String stripExt(String name) {
    int dot = name.lastIndexOf('.');
    if(dot == -1) {
      return name;
    }
    return name.substring(0, dot);
  }

  private void writeDatasetIndex() throws IOException {
    if(!datasets.isEmpty()) {
      FileOutputStream fos = new FileOutputStream(new File(resourcesDir, "datasets"));
      datasets.store(fos, "");
      fos.close();
    }
  }

  
  private void extractTo(TarArchiveEntry entry, InputStream in, File targetDir) throws IOException {
    targetDir.mkdirs();
    int slash = entry.getName().lastIndexOf('/');
    String name = entry.getName().substring(slash+1);
    File targetFile = new File(targetDir, name);
    FileOutputStream fos = new FileOutputStream(targetFile);
    ByteStreams.copy(in, fos);
    fos.close();
  }
 

  private Model buildPom() throws IOException {
    
    PackageDescription description = readDescription();
    
    Model model = new Model();
    model.setModelVersion("4.0.0");
    model.setArtifactId(pkg);
    model.setGroupId("org.renjin.cran");
    model.setVersion(version + "-SNAPSHOT");
    model.setDescription(description.getDescription());
    model.setUrl(description.getUrl());
    
    Parent parent = new Parent();
    parent.setGroupId("org.renjin.cran");
    parent.setArtifactId("cran-parent");
    parent.setVersion("0.7.0-SNAPSHOT");
    model.setParent(parent);
    
    if(!Strings.isNullOrEmpty(description.getLicense())) {
      License license = new License();
      license.setName(description.getLicense());
      model.addLicense(license);
    }
    
    for(Person author : description.getAuthors()) {
      Developer developer = new Developer();
      developer.setName(author.getName());
      developer.setEmail(author.getEmail());
      model.addDeveloper(developer);
    }
    
    addCoreModule(model, "graphics");
    addCoreModule(model, "methods");
    
    for(PackageDependency packageDep : description.getDepends()) {
      if(!packageDep.getName().equals("R")) {
        model.addDependency(toMavenDependency(packageDep.getName()));
      }
    }
    
//    for(PackageDependency packageDep : description.getSuggests()) {
//      Dependency dep = toMavenDependency(packageDep.getName());
//      dep.setScope("optional");
//      model.addDependency(dep);
//    }
    
    Plugin renjinPlugin = new Plugin();
    renjinPlugin.setGroupId("org.renjin");
    renjinPlugin.setArtifactId("renjin-maven-plugin");
    renjinPlugin.setVersion(RENJIN_VERSION);
    
    PluginExecution compileExecution = new PluginExecution();
    compileExecution.setId("renjin-compile");
    compileExecution.addGoal("namespace-compile");
    renjinPlugin.addExecution(compileExecution);
    
    PluginExecution testExecution = new PluginExecution();
    testExecution.setId("renjin-test");
    testExecution.addGoal("test");
    renjinPlugin.addExecution(testExecution);
    
    Xpp3Dom defaultPackages = new Xpp3Dom("defaultPackages");
    for(String defaultPackage : new String[] { "methods" , "stats", "utils", "grDevices", "graphics" }) {
      Xpp3Dom pkg = new Xpp3Dom("package");
      pkg.setValue(defaultPackage);
      defaultPackages.addChild(pkg);
    }
    
    Xpp3Dom configuration = new Xpp3Dom("configuration");
    configuration.addChild(defaultPackages);
    
    testExecution.setConfiguration(configuration);
    
    Build build = new Build();
    build.addPlugin(renjinPlugin);
    
    model.setBuild(build);
    
    Repository bddRepo = new Repository();
    bddRepo.setId("bedatadriven-public");
    bddRepo.setUrl("http://nexus.bedatadriven.com/content/groups/public");
    bddRepo.setName("bedatadriven Public Repo");
    model.addRepository(bddRepo);
    
    
    return model;
  }

  private Dependency toMavenDependency(String pkgName)
      throws MalformedURLException, IOException {
    Dependency mavenDep = new Dependency();
    mavenDep.setArtifactId(pkgName);
    if(corePackages.contains(pkgName)) {
      mavenDep.setGroupId("org.renjin");
      mavenDep.setVersion(RENJIN_VERSION);
    } else {
      mavenDep.setGroupId("org.renjin.cran");
      mavenDep.setVersion(CRAN.fetchCurrentVersion(pkgName));
    }
    return mavenDep;
  }

  private void addCoreModule(Model model, String name) {
    Dependency mavenDep = new Dependency();
    mavenDep.setGroupId("org.renjin");
    mavenDep.setArtifactId(name);
    mavenDep.setVersion(RENJIN_VERSION);
    model.addDependency(mavenDep);
  }

  private PackageDescription readDescription() throws IOException {
    File descFile = new File(baseDir, "DESCRIPTION");
    FileReader reader = new FileReader(descFile);
    PackageDescription desc = PackageDescription.fromReader(reader);
    reader.close();
    
    return desc;
  }

  private void writePom(Model pom) throws IOException {
    File pomFile = new File(baseDir, "pom.xml");
    FileWriter fileWriter = new FileWriter(pomFile);
    MavenXpp3Writer writer = new MavenXpp3Writer();
    writer.write(fileWriter, pom);
    fileWriter.close();
  }
} 
