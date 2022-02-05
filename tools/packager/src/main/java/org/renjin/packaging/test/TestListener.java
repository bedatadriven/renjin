/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */
package org.renjin.packaging.test;

import java.io.File;

public interface TestListener {

  void debug(String message);

  /**
   * Called when a new test script file starts.
   */
  void startFile(File testFile);

  /**
   * Called when a new, individual test starts
   */
  void start(String testName);

  /**
   * Called when a test exceeds its time limit. A call to {@code #fail} will follow if the
   * test can be successfully interrupted.
   */
  void timeout();

  /**
   * Called when the currently-running test passes.
   */
  void pass();

  /**
   * Called when the currently-running test passes.
   */
  void fail();

  /**
   * Called when all tests in the test file have completed.
   */
  void done();

}
