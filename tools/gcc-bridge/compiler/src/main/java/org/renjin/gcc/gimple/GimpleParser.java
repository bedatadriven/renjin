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
package org.renjin.gcc.gimple;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Parses a JSON-encoded {@link GimpleCompilationUnit} emitted from our GCC plugin
 */
public class GimpleParser {

  private final ObjectMapper mapper;

  public GimpleParser() {
    super();

    SimpleModule gimpleModule = new SimpleModule("Gimple", Version.unknownVersion()).addDeserializer(GimpleOp.class,
        new GimpleOpDeserializer());

    mapper = new ObjectMapper();
    mapper.registerModule(gimpleModule);
  }

  private GimpleCompilationUnit parse(Reader reader) throws IOException {
    GimpleCompilationUnit unit = mapper.readValue(reader, GimpleCompilationUnit.class);
    for (GimpleFunction function : unit.getFunctions()) {
      function.setUnit(unit);
    }
    for (GimpleVarDecl varDecl : unit.getGlobalVariables()) {
      varDecl.setUnit(unit);
      varDecl.setGlobal(true);
    }
    return unit;
  }

  public GimpleCompilationUnit parse(File file) throws IOException {
    FileReader reader = new FileReader(file);
    try {
      GimpleCompilationUnit unit = parse(reader);
      unit.setSourceFile(file);
      return unit;
    } finally {
      reader.close();
    }
  }

}
