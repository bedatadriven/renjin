/*
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
package org.renjin.packaging;

import org.renjin.repackaged.guava.annotations.VisibleForTesting;
import org.renjin.repackaged.guava.base.Charsets;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.repackaged.guava.base.Strings;
import org.renjin.repackaged.guava.collect.ArrayListMultimap;
import org.renjin.repackaged.guava.collect.Iterables;
import org.renjin.repackaged.guava.collect.Lists;
import org.renjin.repackaged.guava.io.ByteSource;
import org.renjin.repackaged.guava.io.CharStreams;
import org.renjin.repackaged.guava.io.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * Package DESCRIPTION file
 */
public class PackageDescription {

  private ArrayListMultimap<String, String> properties = ArrayListMultimap.create();



  public static class PackageDependency {
    private String name;
    private String versionRange;

    public PackageDependency(String spec) {
      int versionSpecStart = spec.indexOf('(');
      if(versionSpecStart==-1) {
        name = spec;
        versionRange = "[0,)";
      } else {
        this.name = spec.substring(0, versionSpecStart).trim();

        int versionSpecEnd = spec.indexOf(')', versionSpecStart);
        if(versionSpecEnd == -1) {
          throw new IllegalArgumentException("Unterminated version specification: " + spec);
        }
        String versionSpec = spec.substring(versionSpecStart+1, versionSpecEnd).trim();
        if(versionSpec.startsWith(">=")) {
          versionRange = "[" + versionSpec.substring(">=".length()).trim() + ",)";
        } else if(versionSpec.startsWith(">")){
          versionRange = "(" + versionSpec.substring(">".length()).trim() + ",)";
        } else {
          versionRange = versionSpec;
        }
      }
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getVersionRange() {
      return versionRange;
    }

    public void setVersionRange(String versionRange) {
      this.versionRange = versionRange;
    }

    @Override
    public String toString() {
      return (name + "  " + versionRange).trim();
    }


  }

  private static class PackageDependencyParser implements Function<String, PackageDependency> {

    @Override
    public PackageDependency apply(String arg0) {
      return new PackageDependency(arg0);
    }
  }

  public static class Person {
    private String name;
    private String email;

    private Person(String spec) {
      int bracketStart = spec.indexOf('<');
      if(bracketStart == -1) {
        this.name = spec.trim();
      } else {
        this.name = spec.substring(0, bracketStart).trim();
        int bracketEnd = spec.indexOf('>', bracketStart);
        this.email = spec.substring(bracketStart+1, bracketEnd);
      }
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }
  }

  private static class PersonParser implements Function<String, Person> {

    @Override
    public Person apply(String arg0) {
      return new Person(arg0);
    }
  }

  public static PackageDescription fromString(String contents) throws IOException {

    PackageDescription d = new PackageDescription();
    d.properties = ArrayListMultimap.create();

    List<String> lines = CharStreams.readLines(new StringReader(contents));
    String key = null;
    StringBuilder value = new StringBuilder();
    for(String line : lines) {
      if(line.length() > 0) {
        if(Character.isWhitespace(line.codePointAt(0))) {
          if(key == null) {
            throw new IllegalArgumentException("Expected key at line '" + line + "'");
          }
          value.append(" ").append(line.trim());
        } else {
          if(key != null) {
            d.properties.put(key, value.toString());
            value.setLength(0);
          }
          int colon = line.indexOf(':');
          if(colon == -1) {
            throw new IllegalArgumentException("Expected line in format key: value, found '" + line + "'");
          }
          key = line.substring(0, colon);
          value.append(line.substring(colon+1).trim());
        }
      }
    }
    if(key != null) {
      d.properties.put(key, value.toString());
    }
    return d;
  }

  public static PackageDescription fromInputStream(ByteSource in) throws IOException {
    return fromString(in.asCharSource(Charsets.UTF_8).read());
  }

  public static PackageDescription fromFile(File file) throws IOException {
    return fromInputStream(Files.asByteSource(file));
  }

  public String getFirstProperty(String key) {
    if(properties.containsKey(key)) {
      return properties.get(key).iterator().next();
    } else {
      return null;
    }
  }

  
  public List<String> getProperty(String key) {
    return properties.get(key);
  }


  public List<String> getCollate() {
    List<String> files = Lists.newArrayList();
    List<String> propertyValues = getProperty("Collate");
    for (String propertyValue : propertyValues) {
      files.addAll(parseFileList(propertyValue));
    }
    return files;
  }

  @VisibleForTesting
  static List<String> parseFileList(String propertyValue) {
    List<String> names = new ArrayList<>();
    StringBuilder name = new StringBuilder();
    boolean quoted = false;
    boolean squoted = false;
    for (int i = 0; i < propertyValue.length(); i++) {
      char c = propertyValue.charAt(i);
      if(!quoted && c == '\'') {
        squoted = !squoted;
      } else if(!squoted && c == '"') {
        quoted = !quoted;
      } else if(!quoted && !squoted && Character.isWhitespace(c)) {
        if(name.length() > 0) {
          names.add(name.toString());
          name.setLength(0);
        }
      } else {
        name.append(c);
      }
    }
    if(name.length() > 0) {
      names.add(name.toString());
    }
    return names;
  }

  public boolean hasProperty(String key) {
    return properties.containsKey(key);
  }

  public String getPackage() {
    return getFirstProperty("Package");
  }

  public void setProperty(String name, String value) {
    properties.replaceValues(name, Collections.singleton(value));
  }

  public void setPackage(String packageName) {
    setProperty("Package", packageName);
  }

  public String getTitle() {
    return getFirstProperty("Title");
  }

  public void setTitle(String title) {
    setProperty("Title", title);
  }

  public String getVersion() {
    return getFirstProperty("Version");
  }

  public void setVersion(String version) {
    setProperty("Version", version);
  }

  public Iterable<Person> getAuthors() {
    String authors = getFirstProperty("Author");
    if(Strings.isNullOrEmpty(authors)) {
      return Collections.emptySet();
    } else {
      return Iterables.transform(Arrays.asList(authors.split("\\s*,\\s*")), new PersonParser());
    }
  }

  public Person getMaintainer() {
    return new Person(getFirstProperty("Maintainer"));
  }

  public String getDescription() {
    return getFirstProperty("Description");
  }

  public Iterable<PackageDependency> getImports() {
    return getPackageDependencyList("Imports");
  }

  public Iterable<PackageDependency> getDepends() {
    return getPackageDependencyList("Depends");
  }

  public Iterable<PackageDependency> getPackageDependencyList(String property) {
    String list = getFirstProperty(property);
    if(Strings.isNullOrEmpty(list)) {
      return Collections.emptySet();
    } else {
      return Iterables.transform(Arrays.asList(list.split("\\s*,\\s*")), new PackageDependencyParser());
    }
  }

  public String getLicense() {
    return getFirstProperty("License");
  }

  public String getUrl() {
    return getFirstProperty("URL");
  }

  public Iterable<String> getProperties() {
    return properties.keySet();
  }
  
  public boolean isCompilationNeeded() {
    String needed = Strings.nullToEmpty(getFirstProperty("NeedsCompilation")).trim();
    
    return "yes".equalsIgnoreCase(needed);
  }


  public void writeTo(File description) throws IOException {
    try(FileWriter writer = new FileWriter(description)) {
      for (Map.Entry<String, String> entry : properties.entries()) {
        writer.append(entry.getKey());
        writer.append(": ");
        writer.append(entry.getValue());
        writer.append("\n");
      }
    }
  }
}