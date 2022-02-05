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

import io.airlift.airline.Arguments;
import io.airlift.airline.Option;
import org.renjin.packaging.PackageSource;

import java.io.IOException;
import java.util.Optional;

public abstract class PackageCommand extends BuildCommand {

  @Arguments(description = "Path to package sourceX2X")
  public String packageSource = ".";

  @Option(name = "--ignore-gimple-errors", description = "Ignore errors compiling native sources and continue building the package")
  public boolean ignoreGimpleErrors;

  protected final PackageBuild createBuildContext() throws IOException {
    PackageSource source = new PackageSource.Builder(packageSource)
        .setDefaultGroupId("org.renjin.cran")
        .build();

    return new PackageBuild(source, Optional.empty(), ignoreGimpleErrors);
  }
}
