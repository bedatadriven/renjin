package org.renjin.aether;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.google.common.io.ByteSource;
import org.eclipse.aether.artifact.Artifact;
import org.renjin.primitives.packaging.FileBasedPackage;

import com.google.common.io.InputSupplier;
import org.renjin.primitives.packaging.FqPackageName;

public class AetherPackage extends FileBasedPackage {

  private JarFile jarFile;
  private Artifact artifact;

  public AetherPackage(Artifact artifact) throws IOException {
    super(new FqPackageName(artifact.getGroupId(), artifact.getArtifactId()));
    this.artifact = artifact;
    this.jarFile = new JarFile(artifact.getFile());
  }

  private ZipEntry entry(String file) {
    return jarFile.getEntry(artifact.getGroupId().replace('.', '/') + "/"
        + artifact.getArtifactId() + "/" + file);
  }

  @Override
  public ByteSource getResource(final String name) throws IOException {
    return new ByteSource() {
      @Override
      public InputStream openStream() throws IOException {
        return jarFile.getInputStream(entry(name));
      }
    };
  }

  @Override
  public Class getClass(String name) {
    try {
      return Class.forName(artifact.getGroupId() + "." + artifact.getArtifactId() + "." + name);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean resourceExists(String name) {
    return entry(name) != null;
  }
}
