package org.renjin.packaging;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.repackaged.guava.base.Function;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.NamedValue;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LazyLoadFrame {
  
  private static final int OLD_VERSION = 1;
  private static final int VERSION = 2;

  
  public static Iterable<NamedValue> load(Context context,
                                          Function<String, InputStream> resourceProvider) throws IOException {

    DataInputStream din = new DataInputStream(resourceProvider.apply("environment"));
    int version = din.readInt();
    if(version == OLD_VERSION) {
      return readOldVersion(din);
    }
    if(version != VERSION) {
      throw new IOException("Unsupported version: " + version);
    }

    int count = din.readInt();
    ListVector.NamedBuilder vector = new ListVector.NamedBuilder(0, count);

    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int length = din.readInt();
      if(length < 0) {
        vector.add(name, new SerializedPromise(resourceProvider, name));
      } else {
        byte[] serialized = new byte[length];
        din.readFully(serialized);
        RDataReader reader = new RDataReader(context, new ByteArrayInputStream(serialized));
        vector.add(name, reader.readFile());
      }
    }
    din.close();
    return vector.build().namedValues();
  }

  private static Iterable<NamedValue> readOldVersion(DataInputStream din) throws IOException {
    int count = din.readInt();
    ListVector.NamedBuilder vector = new ListVector.NamedBuilder(0, count);

    for(int i=0;i!=count;++i) {
      String name = din.readUTF();
      int byteCount = din.readInt();
      byte[] bytes = new byte[byteCount];
      din.readFully(bytes);
      vector.add(name, new SerializedPromise1(bytes));
    }
    din.close();
    return vector.build().namedValues();
  }

}
