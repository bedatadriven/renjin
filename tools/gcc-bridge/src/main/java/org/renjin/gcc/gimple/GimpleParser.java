package org.renjin.gcc.gimple;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Invokes Jackson to parse the JSON-encoded Gimple emitted from our
 * GCC plugin into the Gimple tree.
 */
public class GimpleParser {

  private ObjectMapper mapper;

  public GimpleParser() {
    super();

    SimpleModule gimpleModule = new SimpleModule("Gimple", Version.unknownVersion()).addDeserializer(GimpleOp.class,
        new GimpleOpDeserializer());

    mapper = new ObjectMapper();
    mapper.registerModule(gimpleModule);
  }

  public GimpleCompilationUnit parse(Reader reader) throws IOException {
    return mapper.readValue(reader, GimpleCompilationUnit.class);
  }

  public GimpleCompilationUnit parse(File file) throws IOException {
    return mapper.readValue(file, GimpleCompilationUnit.class);
  }

  public GimpleCompilationUnit parse(URL resource) throws IOException {
    return mapper.readValue(resource, GimpleCompilationUnit.class);
  }

}
