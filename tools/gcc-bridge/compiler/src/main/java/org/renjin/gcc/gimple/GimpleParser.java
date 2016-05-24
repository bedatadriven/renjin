package org.renjin.gcc.gimple;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.base.Charsets;

import java.io.*;
import java.net.URL;

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

  public GimpleCompilationUnit parse(Reader reader) throws IOException {
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
      return parse(reader);
    } finally {
      reader.close();
    }
  }

  public GimpleCompilationUnit parse(URL resource) throws IOException {
    InputStreamReader reader = new InputStreamReader(resource.openStream(), Charsets.UTF_8);
    try {
      return parse(reader);
    } finally {
      reader.close();
    }
  }
}
