package org.renjin.cran;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.google.common.io.InputSupplier;

public class TarEntryReaderSupplier implements InputSupplier<Reader> {
  private TarArchiveInputStream in;
  private boolean opened = false;
  
  public TarEntryReaderSupplier(TarArchiveInputStream in) {
    super();
    this.in = in;
  }

  @Override
  public Reader getInput() throws IOException {
    if(opened) {
      throw new UnsupportedOperationException("opening multiple times not yet impl");
    }
    opened = true;
    return new InputStreamReader(in);
  } 
}
