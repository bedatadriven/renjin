/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors
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
package org.renjin.cli.build;

import io.airlift.airline.Command;

import java.io.File;

@Command(name = "mavenize", description = "Create a Maven build configuration file for the package")
public class PackageMavenizeCommand extends PackageCommand {
  @Override
  protected void tryRun() throws Exception {

    PackageBuild build = createBuildContext();

    PomBuilder pomBuilder = new PomBuilder(build);
    pomBuilder.setIncludeBuild(true);
    pomBuilder.writePomFile(new File(build.getSource().getSourceDir(), "pom.xml"));
  }
}
