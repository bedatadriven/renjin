package org.renjin.gcc.maven;

import org.apache.maven.artifact.handler.ArtifactHandler;


public class HeaderArtifactHandler implements ArtifactHandler {

  public String getExtension() {
    return "jar";
  }

  public String getType() {
    return "jar";
  }

  public String getClassifier() {
    return "headers";
  }

  public String getDirectory() {
    return "jars";
  }

  public String getPackaging() {
    return "jars";
  }

  public boolean isIncludesDependencies() {
    return false;
  }

  public String getLanguage() {
    return "none";
  }

  public boolean isAddedToClasspath() {
    return false;
  }
 
}
