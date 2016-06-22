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
