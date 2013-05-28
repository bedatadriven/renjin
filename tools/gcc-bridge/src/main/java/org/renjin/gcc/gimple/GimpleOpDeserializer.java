package org.renjin.gcc.gimple;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom Jackson deserializer to parse the op strings into
 * our enum type.
 */
public class GimpleOpDeserializer extends JsonDeserializer<GimpleOp> {

  @Override
  public GimpleOp deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    return GimpleOp.valueOf(parser.getText().toUpperCase());
  }

}
