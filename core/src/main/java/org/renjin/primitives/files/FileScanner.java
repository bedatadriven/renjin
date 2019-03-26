/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.primitives.files;

import org.renjin.eval.Context;
import org.renjin.repackaged.guava.collect.Lists;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implements "globbing" on files, like "c:\test\*\*.jar"
 * TODO: update to support the FileInfo abstraction
 */
public class FileScanner {
  private List<String> matches = Lists.newArrayList();
  private String[] parts;
  private boolean markDirectories;

  public static List<String> scan( Context context, String path, boolean markDirectories) {
    FileScanner scanner =  new FileScanner(path, markDirectories);
    scanner.matchRoots();
    return scanner.matches;
  }

  FileScanner(String path, boolean markDirectories) {
    this.parts = path.split("[\\\\//]");
    this.markDirectories = markDirectories;
  }

  private void matchRoots() {
    for(File root : File.listRoots()) {
      String rootName = root.getPath();
      rootName = rootName.substring(0, rootName.length()-1);

      if(rootName.equalsIgnoreCase(parts[0])) {
        match(root, 1);
      }
    }
  }

  private void match(File dir, int partIndex) {
    File[] matchingFiles = dir.listFiles(new WildcardFilter(parts[partIndex]));
    if(matchingFiles != null) {
      for(File match : matchingFiles) {
        if(partIndex == parts.length-1) {
          if( match.isDirectory() && markDirectories) {
            matches.add(match.getAbsolutePath() + File.separator);
          } else {
            matches.add(match.getAbsolutePath());
          }
        } else if(match.isDirectory()) {
          match(match, partIndex+1);
        }
      }
    }
  }

  private static String wildcardPattern(String pattern) {
    StringBuilder sb = new StringBuilder();
    for(int i=0;i!=pattern.length();++i) {
      switch(pattern.charAt(i)) {
        case '.':
          sb.append("\\.");
          break;
        case '?':
          sb.append(".?");
          break;
        case '*':
          sb.append(".*");
          break;
        default:
          sb.appendCodePoint(pattern.codePointAt(i));
      }
    }
    return sb.toString();
  }

  private static class WildcardFilter implements FilenameFilter {
    private final boolean explicitDot;
    private final Pattern regex;

    private WildcardFilter(String wildcard) {
      this.regex = Pattern.compile(wildcardPattern(wildcard));
      this.explicitDot = wildcard.startsWith(".");
    }

    @Override
    public boolean accept(File dir, String name) {
      if(name.startsWith(".") && !explicitDot) {
        return false;
      }
      return regex.matcher(name).matches();
    }
  }
}
