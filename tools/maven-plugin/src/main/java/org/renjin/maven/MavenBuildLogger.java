/*
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
package org.renjin.maven;

import org.apache.maven.plugin.logging.Log;
import org.renjin.packaging.BuildLogger;

public class MavenBuildLogger implements BuildLogger {
  
  private Log log;

  public MavenBuildLogger(Log log) {
    this.log = log;    
  }

  public Log getLog() {
    return log;
  }

  @Override
  public void info(String message) {
    log.info(message);
  }

  @Override
  public void debug(String message) {
    log.debug(message);
  }

  @Override
  public void error(String message) {
    log.error(message);
  }

  @Override
  public void error(String message, Exception e) {
    log.error(message, e);
  }
}
