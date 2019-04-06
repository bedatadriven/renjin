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

public class TestCaseResult {

  public static final String ROOT_TEST_CASE = "(root)";
  
  /**
   * Time in seconds
   */
  private double time;
    
  private String className;
  
  private String name;
  
  private String errorMessage;
  
  private TestOutcome outcome;

  public boolean isRootScript() {
    return name.equals(ROOT_TEST_CASE);
  }
  
  public double getTime() {
    return time;
  }

  public void setTime(double time) {
    this.time = time;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public TestOutcome getOutcome() {
    return outcome;
  }

  public void setOutcome(TestOutcome outcome) {
    this.outcome = outcome;
  }
  
}
