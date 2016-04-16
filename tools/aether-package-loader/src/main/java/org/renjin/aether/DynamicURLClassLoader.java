package org.renjin.aether;

import org.eclipse.aether.resolution.ArtifactResult;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * {@code URLClassLoader} subclass which allows URLs to be added at runtime
 */
public class DynamicURLClassLoader extends URLClassLoader {
  public DynamicURLClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }

  public void addArtifact(ArtifactResult artifactResult) {
    try {
      addURL(artifactResult.getArtifact().getFile().toURI().toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Malformed url from " + artifactResult.getArtifact(), e);
    }
  }
}
