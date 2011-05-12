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
import r.lang.exception.EvalException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFileInfo extends  FileInfo {

  private String jarFileName;
  private JarFile jarFile;
  private String entryName;
  private JarEntry entry;

  public JarFileInfo(String url) {
    int exclamation = url.indexOf("!");
    if(exclamation != -1) {
      this.jarFileName = url.substring("jar:file:".length(), exclamation);
      try {
        this.jarFile = new JarFile(jarFileName);
        this.entryName = url.substring(exclamation+1);
        if(entryName.startsWith("/")) {
          entryName = entryName.substring(1);
        }
        if(entryName.endsWith("/")) {
          entryName = entryName.substring(0, entryName.length()-1);
        }
        this.entry = jarFile.getJarEntry(entryName + "/");
        if(this.entry == null) {
          this.entry = jarFile.getJarEntry(entryName);
        }
      } catch (IOException e) {
        this.jarFile = null;
        this.entry = null;
      }
    }
  }

  public JarFileInfo(String jarFileName, JarFile jarFile, JarEntry entry) {
    this.jarFileName = jarFileName;
    this.jarFile = jarFile;
    this.entry = entry;
    this.entryName = entry.getName();
  }

  public JarFileInfo(String jarFileName, JarFile jarFile, String entryName) {
    this.jarFileName = jarFileName;
    this.jarFile = jarFile;
    this.entryName = entryName;
    this.entry = jarFile.getJarEntry(entryName);

  }

  @Override
  public boolean exists() {
    return entry != null;
  }

  @Override
  public boolean isFile() {
    return entry != null && !entry.isDirectory();
  }

  @Override
  public boolean isDirectory() {
    return entry != null && entry.isDirectory();
  }

  @Override
  protected boolean canWrite() {
    return false;
  }

  @Override
  protected boolean canRead() {
    return true;
  }

  @Override
  public long length() {
    return entry == null ? 0 : entry.getSize();
  }

  @Override
  public long lastModified() {
    return entry == null ? 0 : entry.getTime();
  }

  @Override
  public String getName() {
    return new File(entryName).getName();
  }

  @Override
  public List<FileInfo> listFiles() {
    List<FileInfo> children = Lists.newArrayList();
    if(this.entry != null && this.entry.isDirectory()) {
      Enumeration<JarEntry> entries = jarFile.entries();
      while(entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if(isChild(entry)) {
          children.add(new JarFileInfo(jarFileName, jarFile, entry));
        }
      }
    }
    return children;
  }

  private boolean isChild(JarEntry child) {
    if(!child.getName().startsWith(this.entry.getName()) ||
        child.getName().length() == this.entry.getName().length()) {
      return false;
    }
   int slash = child.getName().indexOf('/', this.entry.getName().length());
   return slash == -1 || slash == child.getName().length()-1;
  }

  @Override
  public String getPath() {
    return "jar:file:" + jarFileName + "!/" + entryName;
  }

  @Override
  public boolean isHidden() {
    return false;
  }

  @Override
  public JarFileInfo getChild(String name) {
    return new JarFileInfo(jarFileName, jarFile, entryName + (isDirectory() ? "" : "/") + name);
  }

  @Override
  public InputStream openInputStream() throws IOException {
    if(entry == null) {
      throw new EvalException("'%s' does not exist", getPath());
    }
    return jarFile.getInputStream(this.entry);
  }
}

