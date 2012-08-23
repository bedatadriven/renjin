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

package org.renjin.primitives.files;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import org.apache.commons.vfs2.*;
import org.renjin.eval.Context;
import org.renjin.eval.EvalException;
import org.renjin.primitives.Warning;
import org.renjin.primitives.annotations.Current;
import org.renjin.primitives.annotations.Primitive;
import org.renjin.primitives.annotations.Recycle;
import org.renjin.primitives.annotations.Visible;
import org.renjin.primitives.text.regex.ExtendedRE;
import org.renjin.primitives.text.regex.RE;
import org.renjin.sexp.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Function for manipulating files and paths.
 */
public class Files {

  public static final int CHECK_ACCESS_EXISTENCE = 0;
  public static final int CHECK_ACCESS_EXECUTE = 1;
  public static final int CHECK_ACCESS_WRITE = 2;
  public static final int CHECK_ACCESS_READ = 3;
   
  private Files() {}

  /**
   * Expand a path name, for example by replacing a leading tilde
   *  by the user's home directory (if defined on that platform).
   * @param path
   * @return the expanded path
   */
  @Primitive("path.expand")
  public static String pathExpand(String path) {
    if(path.startsWith("~/")) {
      return java.lang.System.getProperty("user.home") + path.substring(2);
    } else {
      return path;
    }
  }
  
  @Primitive("file.access")
  public static IntVector fileAccess(@Current Context context, StringVector names, int mode ) throws FileSystemException {
    IntArrayVector.Builder result = new IntArrayVector.Builder();
    for(String name : names) {
      FileObject file = context.resolveFile(pathExpand(name));
      result.add(checkAccess(file, mode));
    }
    result.setAttribute(Symbols.NAMES, new StringArrayVector(names.toArray()));
    return result.build();
  }

  private static int checkAccess(FileObject file, int mode)
      throws FileSystemException {
    switch(mode) {
    case CHECK_ACCESS_EXISTENCE:
      return file.exists() ? 0 : -1;
    case CHECK_ACCESS_READ:
      return file.isReadable() ? 0 : -1;
    case CHECK_ACCESS_WRITE:
      return file.isWriteable() ? 0 : -1;
    case CHECK_ACCESS_EXECUTE:
      return -1; // don't know if this is possible to check with VFS
    }
    throw new EvalException("Invalid 'mode' argument");
  }

  /**
   * Utility function to extract information about files on the user's file systems.
   *
   * @param context  current call Context
   * @param paths the list of files for which to return information
   * @return list column-oriented table of file information
   * @throws FileSystemException
   */
  @Primitive("file.info")
  public static ListVector fileInfo(@Current Context context, StringVector paths) throws FileSystemException {

    DoubleArrayVector.Builder size = new DoubleArrayVector.Builder();
    LogicalArrayVector.Builder isdir = new LogicalArrayVector.Builder();
    IntArrayVector.Builder mode = (IntArrayVector.Builder) new IntArrayVector.Builder()
        .setAttribute(Symbols.CLASS, StringVector.valueOf("octmode"));
    DoubleArrayVector.Builder mtime = new DoubleArrayVector.Builder();
    StringVector.Builder exe = new StringVector.Builder();

    for(String path : paths) {
      if(StringVector.isNA(path)) {
        throw new EvalException("invalid filename argument");
      }
      FileObject file =  context.resolveFile(path);
      if(file.exists()) {
        if(file.getType() == FileType.FILE) {
          size.add((int) file.getContent().getSize());
        } else {
          size.add(0);
        }
        isdir.add(file.getType() == FileType.FOLDER);
        mode.add(mode(file));
        try {
          mtime.add(file.getContent().getLastModifiedTime());
        } catch(Exception e) {
          mtime.add(0);
        }
        exe.add(file.getName().getBaseName().endsWith(".exe") ? "yes" : "no");
      } else {
        size.addNA();
        isdir.addNA();
        mode.addNA();
        mtime.addNA();
        exe.addNA();
      }
    }

    return ListVector.newNamedBuilder()
        .add("size", size)
        .add("isdir", isdir)
        .add("mode", mode)
        .add("mtime", mtime)
        .add("ctime", mtime)
        .add("atime", mtime)
        .add("exe", exe)
        .build();
  }
  
  /**
   * Convert file paths to canonical form for the platform, to display
   * them in a user-understandable form.
   *
   * <p>If a path is not a real path the result is undefined.  This
   * implementation will return the input.
   *
   */
  @Primitive
  @Recycle
  public static String normalizePath(@Current Context context, String path, @Recycle(false) String winSlash, @Recycle(false) SEXP mustWork) {
    try {
      return context.resolveFile(path).getName().getURI();
    } catch(FileSystemException e) {
      return path;
    }
  }
  
  /**
    * Gets the type or storage mode of an object.

    * @return  unix-style file mode integer
    */
   private static int mode(FileObject file) throws FileSystemException {
     int access = 0;
     if(file.isReadable()) {
       access += 4;
     }
     if(file.isWriteable()) {
       access += 2;
     }
     if(file.getType()==FileType.FOLDER) {
       access += 1;
     }
     // i know this is braindead but i can't be bothered
     // to do octal math at the moment
     String digit = Integer.toString(access);
     String octalString = digit + digit + digit;

     return Integer.parseInt(octalString, 8);
   }

  /**
   * Returns true if the file exists.
   *
   * @param context the current call Context
   * @param path the path
   * @return true if the file exists
   * @throws FileSystemException
   */
  @Primitive("file.exists")
  public static boolean fileExists(@Current Context context, String path) throws FileSystemException {
    return context.resolveFile(path).exists();
  }

  /**
   * basename removes all of the path up to and including the last path separator (if any).
   *
   * Trailing path separators are removed before dissecting the path.
   *
   * On Windows this will accept either \ or / as the path separator.
   * Only expect these to be able to handle complete paths, and not for example just a share or a drive.
   *
   * @param path the file path
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
   * Function to do wildcard expansion (also known as ‘globbing’) on file paths.
   *
   * Globbing is implemented by {@link FileScanner}
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
    return new StringArrayVector(matching);
  }

  /**
   * Returns the part of the path up to but excluding the last path separator,
   * or "." if there is no path separator.
   *
   * @param path the path
   * @return  the part of the path up to but excluding the last path separator, or "."
   * if there is no path separator.
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
   * Creates the last element of the path, unless recursive = TRUE. Trailing path separators are
   * removed.

   * @param context the current call Context
   * @param path the path
   * @param showWarnings should the warnings on failure be shown?
   * @param recursive Should elements of the path other than the last be created? If true, like Unix's mkdir -p
   * @param mode the file mode to be used on Unix-alikes: it will be coerced by as.octmode.
   * (currently ignored by renjin)
   * @return true if the operation succeeded for each of the files attempted.
   *  Using a missing value for a path name will always be regarded as a failure.
   *  returns false if the directory already exists
   * @throws FileSystemException
   */
  @Primitive("dir.create")
  public static SEXP dirCreate(@Current Context context, String path, boolean showWarnings, boolean recursive, int mode) throws FileSystemException {
    FileObject dir = context.resolveFile(path);
    dir.createFolder();

    // TODO: return correct value and implement warnings documented above

    context.setInvisibleFlag();
    return new LogicalArrayVector(true);
  }

  /**
   * {@code list.files} produce a character vector of the names of files in the named directory.
   *
   * @param paths  a character vector of full path names; the default corresponds to the working
   *  directory getwd(). Missing values will be ignored.
   * @param pattern an optional regular expression. Only file names which match the regular
   * expression will be returned.
   * @param allFiles  If FALSE, only the names of visible files are returned. If TRUE, all
   * file names will be returned.
   * @param fullNames If TRUE, the directory path is prepended to the file names. If FALSE,
   * only the file names are returned.
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
                                       final boolean recursive,
                                       final boolean ignoreCase,
                                       final boolean includeDirs) throws IOException {

    return new Object() {

      private final StringVector.Builder result = new StringVector.Builder();
      private final RE filter = pattern == null ? null : new ExtendedRE(pattern).ignoreCase(ignoreCase);

      public StringVector list() throws IOException {
        for(String path : paths) {
          FileObject folder = context.resolveFile(path);
          if(folder.getType() == FileType.FOLDER) {
            if(allFiles & !recursive) {
              add(path, ".");
              add(path, "..");
            }
            for(FileObject child : folder.getChildren()) {
              if(filter(child)) {
                add(path, child);
              }
            }
          }
        }
        return result.build();
      }

      void add(String path, FileObject file) {
        if(fullNames) {
          result.add(path + "/" + file.getName().getBaseName());
        } else {
          result.add(file.getName().getBaseName());
        }
      }

      void add(String path, String name) throws FileSystemException {
        if(fullNames) {
          result.add(path + "/" + name);
        } else {
          result.add(name);
        }
      }

      boolean filter(FileObject child) throws FileSystemException {
        if(!allFiles && isHidden(child)) {
          return false;
        }
        if(recursive && !includeDirs && child.getType() == FileType.FOLDER) {
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

  /**
   * <strong>According to the R docs:</strong>
   * a subdirectory of the temporary directory found by the following rule.
   * The environment variables TMPDIR, TMP and TEMP are checked in turn and the first
   * found which points to a writable directory is used: if none succeeds the value of
   * R_USER (see Rconsole) is used. If the path to the directory contains a space
   * in any of the components, the path returned will use the shortnames version of the path.
   *
   * <p>This implementation also returns the value of {@code System.getProperty(java.io.tmpdir) }
   *
   * @return temporary sub directory
   */
  public static String tempdir() {
    return java.lang.System.getProperty("java.io.tmpdir");
  }

  /**
   *
   * Returns a path that can be used as names for temporary files
   *
   * @param pattern a non-empty character vector giving the initial part of the name
   * @param tempdir a non-empty character vector giving the directory name
   *
   * @return path that can be used as names for temporary files
   */
  public static String tempfile(String pattern, String tempdir, String fileExt) {
    return tempdir + "/" + pattern + fileExt;
  }

  /**
   * Returns an absolute filename representing the current working directory of the R process;
   * setwd(dir) is used to set the working directory to dir.
   *
   * <p>
   * Renjin maintains its own internal pointer to the working directory which lives in
   * {@link org.renjin.eval.Context.Globals}
   *
   * @param context the current call Context
   * @return an absolute filename representing the current working directory
   */
  public static String getwd(@Current Context context) {
    return context.getGlobals().workingDirectory.getName().getURI();
  }
  
  @Visible(false)
  public static String setwd(@Current Context context, String workingDirectoryName) throws FileSystemException {
    FileObject newWorkingDirectory = context.resolveFile(workingDirectoryName);
    if(!newWorkingDirectory.exists() ||
        newWorkingDirectory.getType() != FileType.FOLDER) {
      throw new EvalException("cannot change working directory");
    }
   
    String previous = context.getGlobals().workingDirectory.getName().getURI();
    
    context.getGlobals().workingDirectory = newWorkingDirectory;
    return previous;
  } 

  /**
   * Unlink deletes the file(s) or directories specified by {@code paths}.
   * @param context the current call Context
   * @param paths list of paths to delete
   * @param recursive  Should directories be deleted recursively?
   * @return  0 for success, 1 for failure. Not deleting a non-existent file is not a failure,
   * nor is being unable to delete a directory if recursive = FALSE. However, missing values in x are
   * regarded as failures.
   * @throws FileSystemException
   */
  public static IntVector unlink(@Current Context context, StringVector paths, boolean recursive, boolean force) throws FileSystemException {
    IntArrayVector.Builder result = new IntArrayVector.Builder();
    for(String path : paths) {
      if(StringVector.isNA(path)) {
        result.add(0);
      } else {
        FileObject file = context.resolveFile(path);
        delete(file, recursive);
        result.add(1);
      }
    }
    return result.build();
  }

  private static void delete(FileObject file, boolean recursive) throws FileSystemException {
    if(file.exists()) {
      if(file.getType() == FileType.FILE) {
        file.delete();
      } else if(file.getType() == FileType.FOLDER) {
          if(file.getChildren().length == 0) {
            file.delete();
          } else if(recursive) {
            file.delete();
          }
      }
    }
  }
  
  @Primitive("file.copy")
  public static LogicalVector fileCopy(@Current Context context, StringVector fromFiles, String to, boolean overwrite, final boolean recursive) throws FileSystemException {
    LogicalArrayVector.Builder result = new LogicalArrayVector.Builder();
    FileObject toFile = context.resolveFile(to);
    for(String from : fromFiles) {
      try {
        toFile.copyFrom(context.resolveFile(from), new FileSelector() {
          
          @Override
          public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
            return true;
          }
          
          @Override
          public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
            return recursive;
          }
        });
        result.add(true);
      } catch(FileSystemException e) {
        result.add(false);
      }
    }
    return result.build();
  }

  /**
   * Extract files from or list a zip archive.
   *
   * @param context the current call Context
   * @param zipFile  The pathname of the zip file: tilde expansion (see path.expand) will be performed.
   * @param files A character vector of recorded filepaths to be extracted: the default is to extract all files.
   * @param exdirUri  The directory to extract files to (the equivalent of unzip -d). It will be created if necessary.
   * @param list If TRUE, list the files and extract none. The equivalent of unzip -l.
   * @param overwrite If TRUE, overwrite existing files, otherwise ignore such files. The equivalent of unzip -o.
   * @param junkpaths If TRUE, use only the basename of the stored filepath when extracting. The equivalent of unzip -j.
   * @return  If list = TRUE, a data frame with columns Name, Length (the size of the uncompressed file) and Date (of class "POSIXct").
      Otherwise, a character vector of the filepaths extracted to, invisibly.
   * @throws IOException
   */
  public static SEXP unzip(@Current Context context, String zipFile, Vector files, String exdirUri,
                                 boolean list, boolean overwrite, boolean junkpaths) throws IOException {

    ZipInputStream zin = new ZipInputStream(context.resolveFile(pathExpand(zipFile)).getContent().getInputStream());
    try {
      FileObject exdir = context.resolveFile(exdirUri);
  
      if(list) {
        throw new EvalException("unzip(list=true) not yet implemented");
      }
  
      ZipEntry entry;
      while ( (entry=zin.getNextEntry()) != null ) {
        if( unzipMatches(entry, files))  {
           unzipExtract(zin, entry, exdir, junkpaths, overwrite);
        }
      }
      context.setInvisibleFlag();
  
      return new IntArrayVector(0);
    } finally {
      zin.close();
    }
  }

  /**
   * Helper function to extract a zip entry to the given folder.
   */
  private static void unzipExtract(ZipInputStream zin, ZipEntry entry, FileObject exdir,
                                   boolean junkpaths, boolean overwrite) throws IOException {
    if(junkpaths) {
      throw new EvalException("unzip(junpaths=false) not yet implemented");
    }

    FileObject exfile = exdir.resolveFile(entry.getName());
    if(exfile.exists() && !overwrite) {
      throw new EvalException("file to be extracted '%s' already exists", exfile.getName().getURI());
    }
    OutputStream out = exfile.getContent().getOutputStream();
    try {

      byte buffer[] = new byte[64 * 1024];
      int bytesRead;
      while( (bytesRead=zin.read(buffer)) != -1 ) {
        out.write(buffer, 0, bytesRead);
      }
    } finally {
      out.close();
    }
  }

  private static boolean unzipMatches(ZipEntry entry, Vector files) {
    if(files == Null.INSTANCE) {
      return true;
    } else {
      for(int i=0;i!=files.length();++i) {
        if(entry.getName().equals(files.getElementAsString(i))) {
          return true;
        }
      }
      return false;
    }
  }
  
  @Primitive("file.create")
  public static boolean fileCreate(@Current Context context, @Recycle String fileName, @Recycle(false) boolean showWarnings) throws IOException {
    try {
      FileObject file = context.resolveFile(fileName);
      // VFS will create the parent folder if it doesn't exist, 
      // which the R method is not supposed to do
      if(!file.getParent().exists()) {
        throw new IOException("No such file or directory");
      }
      file.getContent().getOutputStream().close();
      return true;
      
    } catch (Exception e) {
      if(showWarnings) {
        Warning.invokeWarning(context, "cannot create file '%s', reason '%s'", fileName, e.getMessage());
      }
      return false;
    }
  }
  
  /**
   *  ‘file.append’ attempts to append the files named by its second
   * argument to those named by its first.  The R subscript recycling
   * rule is used to align names given in vectors of different lengths.
   */
  @Primitive("file.append")
  public static boolean fileAppend(@Current Context context, @Recycle String destFileName, @Recycle String sourceFileName) {
    try {
      FileObject sourceFile = context.resolveFile(sourceFileName);
      if(!sourceFile.exists()) {
        return false;
      }
      FileObject destFile = context.resolveFile(destFileName);
      OutputStream out = destFile.getContent().getOutputStream(true);
      try {
        InputStream in = sourceFile.getContent().getInputStream();
        try {
          ByteStreams.copy(in, out);
        } finally {
          try { in.close(); } catch(Exception ignored) {}
        }
      } finally {
        try { out.close(); } catch(Exception ignored) {}
      }
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}
