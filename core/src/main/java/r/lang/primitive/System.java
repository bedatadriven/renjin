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

package r.lang.primitive;

import com.google.common.collect.Lists;
import r.lang.SEXP;
import r.lang.StringExp;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class System {

  public static long sysTime() {
    return new Date().getTime();
  }

  public static String getRHome() {
    return "classpath:/R";
  }

  /**
   * Function to do wildcard expansion (also known as ‘globbing’) on file paths.
   *
   * @param paths character vector of patterns for relative or absolute filepaths.
   *  Missing values will be ignored.
   * @param markDirectories  should matches to directories from patterns that do not
   * already end in / or \ have a slash appended?
   * May not be supported on all platforms.
   * @return
   */
  public static StringExp glob(StringExp paths, boolean markDirectories) {
    List<String> matching = Lists.newArrayList();
    for(String path : paths) {
      if(path != null) {
        matching.addAll(FileScanner.scan(path, markDirectories));
      }
    }
    return new StringExp(matching);
  }

  public static String pathExpand(SEXP pathExp) {
    String path = "a";
    if(path.startsWith("~/")) {
      return java.lang.System.getProperty("user.home") + path.substring(2);
    } else {
      return path;
    }
  }

  private static class FileScanner {
    private List<String> matches = Lists.newArrayList();
    private String[] parts;
    private boolean markDirectories;

    public static List<String> scan(String path, boolean markDirectories) {
      FileScanner scanner=  new FileScanner(path, markDirectories);
      scanner.matchRoots();
      return scanner.matches;
    }

    private FileScanner(String path, boolean markDirectories) {
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
}
