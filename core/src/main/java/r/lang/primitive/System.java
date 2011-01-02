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
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import r.lang.*;
import r.lang.exception.EvalException;
import r.lang.primitive.annotations.Current;
import r.lang.primitive.annotations.Primitive;
import r.lang.primitive.regex.RE;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class System {

  public static long sysTime() {
    return new Date().getTime();
  }

  public static String getRHome() {
    // hardcode to the R home location to a classpath location
    return "res:r";
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
  public static ListVector fileInfo(@Current Context context, StringVector paths) throws FileSystemException {
    EvalException.check(paths.length() > 0, "invalid filename argument");

    DoubleVector.Builder size = new DoubleVector.Builder();
    LogicalVector.Builder isdir = new LogicalVector.Builder();
    IntVector.Builder mode = (IntVector.Builder) new IntVector.Builder()
        .setAttribute(Attributes.CLASS, new StringVector("octmode"));
    DoubleVector.Builder mtime = new DoubleVector.Builder();
    StringVector.Builder exe = new StringVector.Builder();

    for(String path : paths) {
      FileObject file = context.resolveFile(path);
      if(file.exists()) {
        FileContent content = file.getContent();
        if(file.getType() == FileType.FILE) {
          size.add((int) content.getSize());
        } else {
          size.add(0);
        }
        isdir.add(file.getType() == FileType.FOLDER);
        mode.add(mode(file));
        mtime.add(content.getLastModifiedTime());
        exe.add(file.getName().getPath().endsWith(".exe") ? "yes" : "no");
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

  @Primitive("file.exists")
  public static boolean fileExists(@Current Context context, String path) {
    try {
      return context.resolveFile(path).exists();
    } catch (FileSystemException e) {
      return false;
    }
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
                                       final boolean ignoreCase) throws FileSystemException {

    return new Object() {

      private final StringVector.Builder result = new StringVector.Builder();
      private final RE filter = pattern == null ? null : new RE(pattern).ignoreCase(ignoreCase);

      public StringVector list() throws FileSystemException {
        for(String path : paths) {
          FileObject folder = context.resolveFile(path);
          if(folder.getType() == FileType.FOLDER) {
            if(allFiles) {
              add(folder, ".");
              add(folder, "..");
            }
            for(FileObject child : folder.getChildren()) {
              if(filter(child)) {
                add(child);
              }
            }
          }
        }
        return result.build();
      }

      void add(FileObject file) {
        if(fullNames) {
          result.add(file.getName().getURI());
        } else {
          result.add(file.getName().getBaseName());
        }
      }

      void add(FileObject folder, String name) throws FileSystemException {
        if(fullNames) {
          result.add(folder.getURL() + "/" + name);
        } else {
          result.add(name);
        }
      }

      boolean filter(FileObject child) throws FileSystemException {
        if(!allFiles && isHidden(child)) {
          return false;
        }
        if(filter!=null && !filter.match(child.getName().getBaseName())) {
          return false;
        }
        return true;
      }

      private boolean isHidden(FileObject file) throws FileSystemException {
        return file.isHidden() || file.getName().getBaseName().startsWith(".");
      }
    }.list();
  }


  private static int mode(FileObject file) throws FileSystemException {
    int access = 0;
    if(file.isReadable()) {
      access += 4;
    }
    if(file.isWriteable()) {
      access += 2;
    }
    if(file.getType() == FileType.FOLDER) {
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

}
