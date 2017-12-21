/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
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
package org.renjin.packaging;


import org.renjin.primitives.packaging.FqPackageName;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.Files;
import org.renjin.repackaged.guava.primitives.UnsignedBytes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Describes the layout and source of an R Package.
 */
public class PackageSource {

  private String groupId;
  private String packageName;
  private File packageDir;
  
  private File namespaceFile;

  private PackageDescription description;

  private File sourceDir;
  private List<File> sourceFiles;
  
  private File dataDir;
  
  private PackageSource() {
  }

  public String getPackageName() {
    return packageName;
  }

  public String getGroupId() {
    return groupId;
  }
  

  public FqPackageName getFqName() {
    return new FqPackageName(getGroupId(), getPackageName());
  }

  public File getNamespaceFile() {
    return namespaceFile;
  }
  
  public File getSourceDir() {
    return sourceDir;
  }
  
  public File getNativeSourceDir() {
    return new File(packageDir, "src");
  }
  
  public File getInstalledFilesDir() {
    return new File(packageDir, "inst");
  }

  public List<File> getSourceFiles() {
    return sourceFiles;
  }

  public String getVersion() {
    return description.getVersion();
  }

  public File getPackageDir() {
    return packageDir;
  }

  public String getJavaPackageName() {
    return groupId + "." + getPackageName();
  }

  public boolean needsCompilation() {
    return "yes".equals(description.getFirstProperty("NeedsCompilation"));
  }

  public boolean isCXX11() {
    String systemRequirements = Strings.nullToEmpty(description.getFirstProperty("SystemRequirements")).trim();
    boolean isCXX11 = "C++11".equals(systemRequirements);
    if(isCXX11) {
      System.out.println("Checking whether in DESCRIPTION 'SystemRequirements' is set to 'C++11'... yes");
    } else {
      System.out.println("Checking whether in DESCRIPTION 'SystemRequirements' is set to 'C++11'... no");
    }
    return isCXX11;
  }

  public PackageDescription getDescription() {
    return description;
  }

  public File getDataDir() {
    return dataDir;
  }

  public File getTestsDir() {
    return new File(packageDir, "tests");
  }

  /**
   *
   * @return the directory containing Java Jars to be deployed with this project.
   */
  public File getJavaDir() {
    return new File(getInstalledFilesDir(), "java");
  }
  
  public static class Builder {
    private PackageSource source = new PackageSource();
    
    private List<String> sourceFiles = null;
    
    public Builder(File baseDir) {
      source.packageDir = baseDir;
      source.namespaceFile = new File(baseDir, "NAMESPACE");
      source.sourceDir = new File(baseDir, "R");
      source.dataDir = new File(baseDir, "data");
    }

    public Builder(String packagePath) {
      this(new File(packagePath));
    }

    public Builder setGroupId(String groupId) {
      source.groupId = groupId;
      return this;
    }

    public Builder setPackageName(String name) {
      source.packageName = name;
      return this;
    }
    
    /**
     * Overrides the location of the NAMESPACE file. (Defaults to $basedir/NAMESPACE)
     */
    public Builder setNamespaceFile(File namespaceFile) {
      source.namespaceFile = namespaceFile;
      return this;
    }


    public Builder setDescription(PackageDescription description) {
      source.description = description;
      return this;
    }

    public Builder setSourceFiles(List<String> sourceFiles) {
      this.sourceFiles = sourceFiles;
      return this;
    }

    /**
     * Overrides the location of the R sources. (Defaults to $basedir/R)
     */
    public Builder setSourceDir(File dir) {
      source.sourceDir = dir;
      return this;
    }

    public Builder setDataDir(File dir) {
      source.dataDir = dir;
      return this;
    }
    
    public PackageSource build() throws IOException {
      checkExists("Package directory", source.packageDir);
      checkExists("NAMESPACE file", source.namespaceFile);

      check(!Strings.isNullOrEmpty(source.groupId), "GroupId must be set.");

      if(source.description == null) {
        source.description = readDescription();
      }
      
      if(source.packageName == null) {
        if(source.description != null) {
          source.packageName = source.description.getPackage();
        } else {
          source.packageName = source.packageDir.getCanonicalFile().getName();
        }
      }
      
      source.sourceFiles = sourceFiles();
      return source;
    }

    private PackageDescription readDescription() {
      PackageDescription description = null;
      try {
        File descriptionFile = new File(source.packageDir, "DESCRIPTION");
        if(!descriptionFile.exists()) {
          throw new FileNotFoundException(descriptionFile.getAbsolutePath() + " does not exist.");
        }

        description = PackageDescription.fromFile(descriptionFile);
      } catch (IOException e) {
        throw new BuildException("Exception reading DESCRIPTION file: " + e.getMessage());
      }
      return description;
    }
    
    private void checkExists(String what, File file) {
      check(file.exists(), what + " does not exist at " + file.getAbsolutePath());
    }

    private void check(boolean condition, String message, Object... args) {
      if(!condition) {
        throw new BuildException(String.format(message, args));
      }
    }

    private List<File> sourceFiles() {
      if(sourceFiles != null) {
        return sourceFiles(sourceFiles);
      }

      if (source.description != null &&
          source.description.hasProperty("Collate")) {
        List<String> collateOrder = source.description.getCollate();
        return sourceFiles(collateOrder);
      }
      
      return findSources();
    }

    private List<File> sourceFiles(List<String> sourceFileNames) {
      List<File> list = Lists.newArrayList();
      for (String sourceFilename : sourceFileNames) {
        File sourceFile = new File(source.sourceDir, sourceFilename);
        
        list.add(sourceFile);
      }
      return list;
    }


    private List<File> findSources() {
      List<File> list = Lists.newArrayList();

      // all .R/.S files in the R sourceDirectory
      File[] files = source.sourceDir.listFiles();
      if (files != null) {
        for (File file : files) {

          String nameUpper = file.getName().toUpperCase();

          if (nameUpper.endsWith(".R") ||
              nameUpper.endsWith(".S") ||
              nameUpper.endsWith(".Q")) {

            list.add(file);

          }
        }
      }

      // Sort by filename, IGNORING extension
      // AND using platform-independent BYTE for BYTE sort order
      Collections.sort(list, new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
          byte[] name1 = Files.getNameWithoutExtension(file1.getName()).getBytes();
          byte[] name2 = Files.getNameWithoutExtension(file2.getName()).getBytes();
          return UnsignedBytes.lexicographicalComparator().compare(name1, name2);
        }
      });

      return list;
    }
  }
}
