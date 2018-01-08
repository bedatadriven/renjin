/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.primitives.io.connections;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.EvalException;
import org.tukaani.xz.XZInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * A connection to a file.
 */
public class FileConnection extends AbstractConnection {

  private InputStream in;  
  private OutputStream out;
  
  private FileObject file;
  private OpenSpec openSpec = null;
  
  public FileConnection(FileObject file, Charset charset) throws IOException {
    super(charset);
    this.file = file;
  }
  
  @Override
  public void open(OpenSpec spec) throws IOException {
    this.openSpec = spec;
    if(spec.forReading() && spec.forWriting()) {
      throw new EvalException("Read/write file connections not yet implemented");
    }
    if(spec.forReading()) {
      if(spec.isBinary()) {
        assureOpenForInput();
      } else {
        getReader();
      }
    } else if(spec.forWriting()) {
      if(spec.isBinary()) {
        assureOpenForOutput();
      } else {
        getPrintWriter();
      }
    }
  }

  private final InputStream assureOpenForInput() throws IOException {
    if(out != null) {
      throw new EvalException("connection is already opened for output, cannot open for input");
    }
    if(in == null) {
      this.in = doOpenForInput();
    }
    return this.in;
  }

  protected InputStream doOpenForInput() throws IOException {
    // We want to automatically decompress if the underlying file is gz/xz/bzipped
    int header[] = new int[6];
    int pushBackBufferSize = header.length;
    PushbackInputStream in;
    try {
      in = new PushbackInputStream(file.getContent().getInputStream(), pushBackBufferSize);
    } catch (FileNotFoundException e) {
      throw new EvalException(e.getMessage());
    }

    for(int i = 0; i < header.length; ++i) {
      header[i] = in.read();
    }
    for(int i = header.length - 1; i>=0; --i) {
      if (header[i] != -1) {
        in.unread(header[i]);
      }
    }
    if(header[0] == GzFileConnection.GZIP_MAGIC_BYTE1 && header[1] == GzFileConnection.GZIP_MAGIC_BYTE2) {
      return new GZIPInputStream(in);
    } 
    if(header[0] == 'B' && header[1] == 'Z') {
      return new BZip2CompressorInputStream(in);
    }
    
    if(Arrays.equals(header, XzFileConnection.XZ_MAGIC_BYTES)) {
      return new XZInputStream(in);
    }
    
    return in;
  }
  
  private OutputStream assureOpenForOutput() throws IOException {
    if(in != null) {
      throw new EvalException("connection is already opened for input, cannot open for output");
    }
    if(out == null) {
      this.out = doOpenForOutput();
    }
    return this.out;
  }

  protected OutputStream doOpenForOutput() throws FileSystemException, IOException {
    boolean append = (openSpec != null && openSpec.isAppend());
    return file.getContent().getOutputStream(append);
  }
  
  @Override
  public final InputStream getInputStream() throws IOException {
    return assureOpenForInput();
  }

  @Override
  public final OutputStream getOutputStream() throws IOException {
    return assureOpenForOutput();
  }

  @Override
  protected void closeInputIfOpen() throws IOException {
    if(in != null) {
      in.close();
    }
  }

  @Override
  protected void closeOutputIfOpen() throws IOException {
    if(out != null) {
      out.close();
    }
  }

  @Override
  public boolean isOpen() {
    return in!=null || out!=null;
  }

  @Override
  public void flush() throws IOException {
    if(out != null) {
      out.flush();
    }
  }

  @Override
  public String getClassName() {
    return "file";
  }

  @Override
  public String getDescription() {
    return file.getName().getPath();
  }

  @Override
  public Type getType() {
    if(openSpec == null) {
      return Type.TEXT;
    } else {
      return openSpec.getType();
    }
  }

  @Override
  public String getMode() {
    if(openSpec == null) {
      return "r";
    } else {
      return openSpec.toString();
    }
  }

  @Override
  public boolean canRead() {
    return !isOpen() || openSpec == null || openSpec.forReading();
  }

  @Override
  public boolean canWrite() {
    return !isOpen() || openSpec == null || openSpec.forWriting();
  }
  
}
