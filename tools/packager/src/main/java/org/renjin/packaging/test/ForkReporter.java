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

import org.renjin.repackaged.guava.base.Joiner;

/**
 * Sends coded results of the test execution to standard output, where they can
 * be read by the the listener in {@link Fork}
 */
public class ForkReporter implements TestListener {


  public static final String MESSAGE_PREFIX = "!!@@@@####";
  public static final String PASS_MESSAGE = "PASS";
  public static final String FAIL_MESSAGE = "FAIL";
  public static final String DONE_MESSAGE = "DONE";
  public static final String START_MESSAGE = "START";

  @Override
  public void debug(final String message) {
    if(ForkedTestController.DEBUG_FORKING) {
      System.err.println("[EXECUTOR] " + message);
    }
  }

  @Override
  public void done() {
    sendMessage(DONE_MESSAGE);

  }

  @Override
  public void pass() {
    sendMessage(PASS_MESSAGE);
  }

  @Override
  public void fail() {
    sendMessage(FAIL_MESSAGE);
  }

  @Override
  public void start(String testName) {
    sendMessage(START_MESSAGE, testName);
  }

  private void sendMessage(String message, String... arguments) {
    System.out.println(MESSAGE_PREFIX + message + MESSAGE_PREFIX + Joiner.on(MESSAGE_PREFIX).join(arguments));
    System.out.flush();
  }

}
