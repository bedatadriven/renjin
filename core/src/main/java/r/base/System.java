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

package r.base;

import com.google.common.collect.Lists;
import r.base.regex.RE;
import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class System {

  public static final String CLASSPATH_PREFIX = "classpath:";

  public static long sysTime() {
    return new Date().getTime();
  }

  public static String getRHome() {
    // hardcode to the R home location to a classpath location
    return CLASSPATH_PREFIX + "/r";
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
  @Primitive("Sys.glob")
  public static StringVector glob(@Current Context context, StringVector paths, boolean markDirectories) {

    List<String> matching = Lists.newArrayList();
    for(String path : paths) {
      if(path != null) {
        if(path.indexOf('*')==-1) {
          matching.add(path);
        } else {
          matching.addAll(FileScanner.scan(context, path, markDirectories));
        }
      }
    }
    return new StringVector(matching);
  }

  @Primitive("path.expand")
  public static String pathExpand(String path) {
    if(path.startsWith("~/")) {
      return java.lang.System.getProperty("user.home") + path.substring(2);
    } else {
      return path;
    }
  }

  @Primitive("file.info")
  public static ListVector fileInfo(@Current Context context, StringVector paths)  {
    EvalException.check(paths.length() > 0, "invalid filename argument");

    DoubleVector.Builder size = new DoubleVector.Builder();
    LogicalVector.Builder isdir = new LogicalVector.Builder();
    IntVector.Builder mode = (IntVector.Builder) new IntVector.Builder()
        .setAttribute(Attributes.CLASS, new StringVector("octmode"));
    DoubleVector.Builder mtime = new DoubleVector.Builder();
    StringVector.Builder exe = new StringVector.Builder();

    for(String path : paths) {
      File file = getFile(path);
      if(file.exists()) {
        if(file.isFile()) {
          size.add((int) file.length());
        } else {
          size.add(0);
        }
        isdir.add(file.isDirectory());
        mode.add(mode(file));
        mtime.add(file.lastModified());
        exe.add(file.getName().endsWith(".exe") ? "yes" : "no");
      } else {
        size.add(IntVector.NA);
        isdir.add(IntVector.NA);
        mode.add(IntVector.NA);
        mtime.add(DoubleVector.NA);
        exe.add(StringVector.NA);
      }

      ListVector list = ListVector.newBuilder()
          .add("size", size)
          .add("isdir", isdir)
          .add("mode", mode)
          .add("mtime", mtime)
          .add("ctime", mtime)
          .add("atime", mtime)
          .add("exe", exe)
          .build();
      return list;
    }

    ListVector.Builder info = ListVector.newBuilder();

    return info.build();
  }

  private static File getFile(String path) {
    if(path.startsWith(CLASSPATH_PREFIX)) {
      URL resource = System.class.getResource(path.substring(CLASSPATH_PREFIX.length()));
      if(resource != null) {
        return new File(resource.getFile());
      } else {
        return new File(path.substring(CLASSPATH_PREFIX.length()));
      }
    }
    return new File(path);
  }

  @Primitive("file.exists")
  public static boolean fileExists(@Current Context context, String path) {
    if(path.startsWith(CLASSPATH_PREFIX)) {
      return System.class.getResource(path.substring(CLASSPATH_PREFIX.length())) != null;
    } else {
      return new File(path).exists();
    }
  }

  /**
   *
   * @param path
   * @return  the part of the path up to but excluding the last path separator, or "." if there is no path separator.
   */
  public static String dirname(String path) {
    for(int i=path.length()-1;i>=0;--i) {
      if(path.charAt(i) == '\\' || path.charAt(i) == '/') {
        return path.substring(0, i);
      }
    }
    return ".";
  }

  /**
   *
   * @param path
   * @return  removes all of the path up to and including the last path separator (if any).
   */
  public static String basename(String path) {
    for(int i=path.length()-1;i>=0;--i) {
      if(path.charAt(i) == '\\' || path.charAt(i) == '/') {
        return path.substring(i+1);
      }
    }
    return path;
  }



  /**
   *
   * @param path  a character vector of full path names; the default corresponds to the working directory getwd(). Missing values will be ignored.
   * @param pattern an optional regular expression. Only file names which match the regular expression will be returned.
   * @param allFiles  If FALSE, only the names of visible files are returned. If TRUE, all file names will be returned.
   * @param fullNames If TRUE, the directory path is prepended to the file names. If FALSE, only the file names are returned.
   * @param recursive Should the listing recurse into directories?
   * @param ignoreCase Should pattern-matching be case-insensitive?
   *
   * If a path does not exist or is not a directory or is unreadable it is skipped, with a warning.
   * The files are sorted in alphabetical order, on the full path if full.names = TRUE. Directories are included only if recursive = FALSE.
   *
   * @return
   */
  @Primitive("list.files")
  public static StringVector listFiles(@Current final Context context,
                                       final StringVector paths,
                                       final String pattern,
                                       final boolean allFiles,
                                       final boolean fullNames,
                                       boolean recursive,
                                       final boolean ignoreCase)  {

    return new Object() {

      private final StringVector.Builder result = new StringVector.Builder();
      private final RE filter = pattern == null ? null : new RE(pattern).ignoreCase(ignoreCase);

      public StringVector list()  {
        for(String path : paths) {
          File folder = getFile(path);
          if(folder.isDirectory()) {
            if(allFiles) {
              add(folder, ".");
              add(folder, "..");
            }
            for(File child : folder.listFiles()) {
              if(filter(child)) {
                add(child);
              }
            }
          }
        }
        return result.build();
      }

      void add(File file) {
        if(fullNames) {
          result.add(file.getPath());
        } else {
          result.add(file.getName());
        }
      }

      void add(File folder, String name)  {
        if(fullNames) {
          result.add(new File(folder, name).getPath());
        } else {
          result.add(name);
        }
      }

      boolean filter(File child)  {
        if(!allFiles && isHidden(child)) {
          return false;
        }
        if(filter!=null && !filter.match(child.getName())) {
          return false;
        }
        return true;
      }

      private boolean isHidden(File file)  {
        return file.isHidden() || file.getName().startsWith(".");
      }
    }.list();
  }


  private static int mode(File file)  {
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

  @Primitive("Version")
  public static ListVector version() {
    // this is just copied from my local R installation
    // we'll have to see later what makes the most sense to put here,
    // whether we need to pretend to be some version of R
    return ListVector.newBuilder()
        .add("platform", "i386-pc-mingw32")
        .add("arch", "i386")
        .add("os", "mingw32")
        .add("system", "i386, mingw32")
        .add("status", "")
        .add("major", "2")
        .add("minor", "10.1")
        .add("year", "2009")
        .add("month", "12")
        .add("day", "14")
        .add("svn rev", "50720")
        .add("version.string", "R version 2.10.1 (2009-12-14)")
        .build();

  }

  @Primitive("Sys.getenv")
  public static StringVector getEnvironment(@Current Context context, StringVector names, String unset) {
    StringVector.Builder result = new StringVector.Builder();

    Map<String, String> map = context.getGlobals().systemEnvironment;
    if(names.length() == 0) {
      for(Map.Entry<String,String> entry : map.entrySet()) {
        result.add(entry.getKey() + "=" + entry.getValue());
      }
    } else {
      for(String name : names) {
        String value = map.get(name);
        result.add(value == null ? unset : value);
      }
    }
    return result.build();
  }

  @Primitive("Sys.setenv")
  public static LogicalVector setEnvironment(@Current Context context, StringVector names, StringVector values) {

    Map<String, String> map = context.getGlobals().systemEnvironment;

    LogicalVector.Builder result = new LogicalVector.Builder();
    for(int i=0;i!=names.length();++i) {
      map.put(names.getElementAsString(i), values.getElementAsString(i));
      result.add(true);
    }
    return result.build();
  }

  private enum LocaleCategory {
    LC_COLLATE,
    LC_MONETARY,
    LC_NUMERIC,
    LC_TIME,
    LC_MESSAGES,
    LC_PAPER,
    LC_MEASUREMENT;

    String value() {
      return "English_United States.1252";
    }
  }

  private static final int LC_ALL = 1;

  @Primitive("Sys.getlocale")
  public static String getLocale(int categoryIndex) {
    if(categoryIndex == LC_ALL) {
      StringBuilder info = new StringBuilder();
      boolean needsSemi = false;
      for(LocaleCategory category : LocaleCategory.values()) {
        if(needsSemi) {
          info.append(';');
        } else {
          needsSemi = true;
        }
        info.append(category.name()).append('=').append(category.value());
      }
      return info.toString();
    } else {
      return LocaleCategory.values()[categoryIndex-2].value();
    }
  }

  public static StringVector commandArgs() {
    // TODO: something reasonable
    return new StringVector("C:\\Program Files\\R\\R-2.10.1\\bin\\Rgui.exe");
  }

  @Primitive("dyn.load")
  public static ListVector dynLoad(String libraryPath, SEXP local, SEXP now, SEXP dllPath) {
    ListVector.Builder result = new ListVector.Builder();
    result.add("name", "dummyName");
    result.add("path", libraryPath);
    result.add("dynamicLookup", LogicalVector.TRUE);
    result.add("handle", Null.INSTANCE);
    result.add("info", "something here");

    return result.build();
    // TODO: maybe warn or something?
  }



}
