package org.renjin.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.common.io.Files;

/**
 * @goal mavenize
 * @requiresProject false
 */
public class MavenizeMojo extends AbstractMojo {

	/**
	 * Name of the R package located in CRAN
	 * @parameter expression="${renjin.package.dir}"
	 * @required
	 */
	private File legacyPackageDir;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

	  try {
  	  PackageDescription description = PackageDescription.fromFile(new File(legacyPackageDir, "DESCRIPTION"));
      
  	  // create the directory for the new mavenized project
  	  File mavenProjDir= new File(description.getPackage());
  	  mavenProjDir.mkdirs();
  	  
  	  // write the pom
      Model pom = PackagePomBuilder.build(description);
      writePom(mavenProjDir, pom); 
  	
      // copy source files
      File srcDir = new File(mavenProjDir, "src");
      File mainDir = new File(srcDir, "main");
      
      File rDir = new File(mainDir, "R");
      copySources(legacyPackageDir, "R", ".R", rDir);
      
      File cDir = new File(mainDir, "c");
      copySources(legacyPackageDir, "src", ".c", cDir);
      copySources(legacyPackageDir, "src", ".h", cDir);
      
      File fortranDir = new File(mainDir, "fortran");
      copySources(legacyPackageDir, "src", ".f77", fortranDir);
      copySources(legacyPackageDir, "src", ".f", fortranDir);
      
	  } catch(Exception e) {
	    throw new MojoExecutionException("Failure", e);
	  }
	}

  private void copySources(File legacyProjectDir, String subDir, String suffix, File destDir) throws IOException {
    File sources = new File(legacyProjectDir, subDir);
    for(File src : sources.listFiles()) {
      if(src.getName().endsWith(suffix)) {
        destDir.mkdirs();
        Files.copy(src, new File(destDir, src.getName()));
      }
    }
  }

  private void writePom(File mavenProjectDir, Model pom) throws IOException {
    File pomFile = new File(mavenProjectDir, "pom.xml");
    FileWriter fileWriter = new FileWriter(pomFile);
    MavenXpp3Writer writer = new MavenXpp3Writer();
    writer.write(fileWriter, pom);
    fileWriter.close();
  }	
}
