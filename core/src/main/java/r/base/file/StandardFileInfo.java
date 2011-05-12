/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package r.base.file;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class StandardFileInfo extends FileInfo {
  private File file;

  public StandardFileInfo(String path) {
    this.file = new File(path);
  }

  public StandardFileInfo(File file) {
    this.file = file;
  }

  public int mode()  {
    int access = 0;
    if(file.canRead()) {
      access += 4;
    }
    if(file.canWrite()) {
      access += 2;
    }
    if(file.isDirectory()) {
      access += 1;
    }
    // i know this is braindead but i can't be bothered
    // to do octal math at the moment
    String digit = Integer.toString(access);
    String octalString = digit + digit + digit;

    return Integer.parseInt(octalString, 8);
  }

  @Override
  public boolean exists() {
    return file.exists();
  }

  @Override
  public boolean isFile() {
    return file.isFile();
  }

  @Override
  public boolean isDirectory() {
    return file.isDirectory();
  }

  @Override
  public long length() {
    return file.length();
  }

  @Override
  public long lastModified() {
    return file.lastModified();
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public List<FileInfo> listFiles() {
    List<FileInfo> files = Lists.newArrayList();
    for(File child : file.listFiles()) {
      files.add(new StandardFileInfo(child));
    }
    return files;
  }

  @Override
  public String getPath() {
    return file.getPath();
  }

  @Override
  public boolean isHidden() {
    return file.isHidden();
  }

  @Override
  public FileInfo getChild(String name) {
    return new StandardFileInfo(new File(this.file, name));
  }

  @Override
  public InputStream openInputStream() throws FileNotFoundException {
    return new FileInputStream(this.file);
  }

  @Override
  protected boolean canWrite() {
    return file.canWrite();
  }

  @Override
  protected boolean canRead() {
    return file.canRead();
  }
}
