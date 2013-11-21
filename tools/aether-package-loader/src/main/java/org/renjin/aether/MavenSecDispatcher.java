package org.renjin.aether;

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;


public class MavenSecDispatcher extends DefaultSecDispatcher {

  public MavenSecDispatcher() {
    try {
      _cipher = new DefaultPlexusCipher();
    } catch (PlexusCipherException e) {
      throw new RuntimeException(e);
    }
  }
}
