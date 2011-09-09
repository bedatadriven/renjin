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

import r.jvmi.annotations.Current;
import r.jvmi.annotations.Primitive;
import r.lang.*;
import r.lang.exception.EvalException;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class System {

  public static long sysTime() {
    return new Date().getTime();
  }

  public static String getRHome(@Current Context context) throws URISyntaxException {
    return context.getGlobals().homeDirectory;
  }


  public static ListVector Version() {
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

    Matcher matcher = Pattern.compile("[/\\\\](\\w+)\\.dll$").matcher(libraryPath);
    if(!matcher.find()) {
      throw new EvalException("libary path not in expected format");
    }

    result.add("name", matcher.group(1));
    result.add("path", libraryPath);
    result.add("dynamicLookup", LogicalVector.TRUE);
    result.add("handle", Null.INSTANCE);
    result.add("info", "something here");
    result.setAttribute(Symbol.CLASS, new StringVector("DLLInfo"));
    return result.build();
    // TODO: maybe warn or something?
  }

  /**
   * Report on the optional features which have been compiled into this build of R.
   *
   * @param what
   * @return
   */
  public static LogicalVector capabilities(StringVector what) {
    LogicalVector.Builder result = new LogicalVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    for(String capability : what) {
      if(Capabilities.NAMES.contains(capability)) {
        names.add(capability);
        result.add(false);
      }
    }
    result.setAttribute(Symbol.NAMES, names.build());
    return result.build();
  }

  public static LogicalVector capabilities() {

    LogicalVector.Builder result = new LogicalVector.Builder();
    StringVector.Builder names = new StringVector.Builder();

    for(String capability : Capabilities.NAMES) {
      names.add(capability);
      result.add(false);
    }
    result.setAttribute(Symbol.NAMES, names.build());
    return result.build();
  }
  
  @Primitive("date")
  public static StringVector date() {
    // R Style Date Format
    // Example in R: Fri Sep  9 12:20:00 2011 
    // Example in Renjin: Fri Sep 09 12:20:00 2011 
    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    StringVector.Builder b = new StringVector.Builder();
    Date d = new Date();
    String parsed = null;
    parsed = sdf.format(d);
    b.add(parsed);
    return (b.build());
  }


}
