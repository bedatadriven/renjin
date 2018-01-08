/**
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
package org.renjin.gcc;

import java.io.File;

/**
 * Static helper methods for dealing with the Java Native Interface and detection of
 * operating systems and architectures. The main purpose of this class is to provide a
 * more portable alternative to the System.loadLibrary method (and horrible syntax of
 * Bundle-NativeCode in MANIFEST files) when bundling binaries as part of a jar (either
 * inside, or in a directory structure nearby).
 * <p>
 * Note on usage: unfortunately it is not possible to provide a loadLibrary(String) method
 * here as it needs to know the calling class, which is lost when wrapped here. To load a
 * native library using this class, call the getPortableLibraryName() method to return a
 * String which may then be passed to System.loadLibrary().
 * <p>
 * Note: due to the nature of this class, a portable JUnit TestCase is not possible.
 *
 * @author Samuel Halliday
 */
public final class PlatformUtils {
  /**
   * Enumeration of common CPU architectures.
   */
  public enum ARCHType {
    PPC, PPC_64, SPARC, UNKNOWN, X86, X86_64
  };

  /**
   * Enumeration of common operating systems, independent of version or architecture.
   */
  public enum OSType {
    APPLE, LINUX, SUN, UNKNOWN, WINDOWS
  };

  /**
   * Stores the CPU Architecture the JVM is currently running on.
   */
  public static final ARCHType ARCH = calculateArch();

  /**
   * Stores the operating system the JVM is currently running on.
   */
  public static final OSType OS = calculateOS();

  /**
   * The System.mapLibraryName method is broken in the sense that it does not give
   * unique names for operating systems and architectures. In order to facilitate
   * cross-platform compatibility allowing bundled binary files, this method will return
   * a unique library name, dependent on the operating system and architecture. The
   * return value of this method should be passed to the System.loadLibrary method
   * instead of the raw library name.
   * <p>
   * The resulting filename will be constructed by forcing the library name to lowercase
   * and appending an extra String that is defined by the operating system (the
   * architecture is only encoded if it is relevant). Some examples are for the
   * parameter 'name':-
   * <ul>
   * <li>Apple (G3, G4): "name-apple-ppc"</li>
   * <li>Apple (G5): "name-apple-ppc_64"</li>
   * <li>Apple (Intel): "name-apple-x86"</li>
   * <li>Apple (Intel 64 Bit mode): "name-apple-x86_64"</li>
   * <li>Linux (i686): "name-linux-x86"</li>
   * <li>Linux (Intel/AMD 64): "name-linux-x86_64"</li>
   * <li>Linux (sparc): "name-linux-sparc"</li>
   * <li>Linux (PPC 32 bit): "name-linux-ppc"</li>
   * <li>Linux (PPC 64 bit): "name-linux-ppc_64"</li>
   * <li>Windows XP/Vista (i686): "name-windows-x86"</li>
   * <li>Windows XP/Vista (Intel/AMD 64): "name-windows-x86_64"</li>
   * <li>Sun Solaris (Blade): "name-sun-sparc"</li>
   * <li>Sun Solaris (Intel 64 bit): "name-sun-x86_64"</li>
   * </ul>
   *
   * @param name
   * @return
   * @throws IllegalArgumentException
   *             if the input name is zero length or contains a directory separator
   *             character.
   */
  public static String getPortableLibraryName(String name) {
    if (name == null) {
      throw new NullPointerException();
    }

    if ((name.length() == 0) || name.contains(File.separator) || name.
            contains("/")) {
      throw new IllegalArgumentException(
          "Directory separator should not appear in library name: " + name);
    }

    return name.toLowerCase() + getSuffix() + getExtension();
  }

  public static String getExtension() {
    switch(OS) {
      case APPLE:
        return ".so"; /* GCC plugins use .so on OSX, too*/
      case WINDOWS:
        return ".dll";
      case LINUX:
      default:
        return ".so";
    }
  }

  public static void main(String[] args) {
    System.out.println(PlatformUtils.OS + ", " + PlatformUtils.ARCH + ", " +
            getPortableLibraryName("NAME"));
  }

  private static ARCHType calculateArch() {
    String osArch = System.getProperty("os.arch").toLowerCase();
    assert osArch != null;
    if (osArch.equals("i386")) {
      return ARCHType.X86;
    }
    if (osArch.startsWith("amd64") || osArch.startsWith("x86_64")) {
      return ARCHType.X86_64;
    }
    if (osArch.equals("ppc")) {
      return ARCHType.PPC;
    }
    if (osArch.startsWith("ppc")) {
      return ARCHType.PPC_64;
    }
    if (osArch.startsWith("sparc")) {
      return ARCHType.SPARC;
    }
    return ARCHType.UNKNOWN;
  }

  private static OSType calculateOS() {
    String osName = System.getProperty("os.name").toLowerCase();
    assert osName != null;
    if (osName.startsWith("mac os x")) {
      return OSType.APPLE;
    }
    if (osName.startsWith("windows")) {
      return OSType.WINDOWS;
    }
    if (osName.startsWith("linux")) {
      return OSType.LINUX;
    }
    if (osName.startsWith("sun")) {
      return OSType.SUN;
    }
    return OSType.UNKNOWN;
  }

  private static String getSuffix() {
    String prefix =
            "-" + OS.toString().toLowerCase() + "-" + ARCH.toString().
                    toLowerCase();

    return prefix;
  }
}