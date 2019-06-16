package org.renjin.gcc.link;

import org.renjin.repackaged.asm.Type;
import org.renjin.repackaged.guava.base.Charsets;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Properties;

public class RecordSymbol {

  private final String name;
  private final Type providedType;

  public RecordSymbol(String name, String recordClass) {
    this.name = name;
    this.providedType = Type.getObjectType(recordClass);
  }

  public Type getProvidedType() {
    return providedType;
  }

  public String getName() {
    return name;
  }

  public static Optional<RecordSymbol> forName(ClassLoader classLoader, String name)  {
    try(InputStream in = classLoader.getResourceAsStream("META-INF/org.renjin.gcc.records/" + name)) {
      if (in == null) {
        return Optional.empty();
      }
      try (InputStreamReader reader = new InputStreamReader(in, Charsets.UTF_8)) {
        Properties properties = new Properties();
        properties.load(reader);

        return Optional.of(fromDescriptor(name, properties));
      }
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private static RecordSymbol fromDescriptor(String name, Properties properties) {
    return new RecordSymbol(name, properties.getProperty("class"));
  }

}
