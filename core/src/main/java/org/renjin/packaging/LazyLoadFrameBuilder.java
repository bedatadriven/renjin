package org.renjin.packaging;

import java.io.*;

import com.google.common.io.Files;
import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class LazyLoadFrameBuilder {

  private static final int VERSION_1 = 1;
  private static final int VERSION_2 = 2;

  private File outputDir;

  private Context context;

  private Predicate<NamedValue> filter = Predicates.alwaysTrue();


  public LazyLoadFrameBuilder(Context context) {
    this.context = context;
  }
  
  public LazyLoadFrameBuilder outputTo(File dir) {
    this.outputDir = dir;
    return this;
  }
  
  public LazyLoadFrameBuilder filter(Predicate<NamedValue> filter) {
    this.filter = filter;
    return this;
  }
  
  public void build(Environment env) throws IOException {

    Iterable<NamedValue> toWrite = Iterables.filter(env.namedValues(), filter);


    // Now write an index of symbols
    File indexFile = new File(outputDir, "environment");
    DataOutputStream indexOut = new DataOutputStream(new FileOutputStream(indexFile));

    // mark this format as version 2
    indexOut.writeInt(VERSION_2);

    // write out each (large) symbols to a separate resource file.
    // Small values will be serialized directly in the index file

    indexOut.writeInt(Iterables.size(toWrite));
    for(NamedValue namedValue : toWrite) {

      indexOut.writeUTF(namedValue.getName());
      byte[] bytes = serializeSymbol(namedValue);

      if(bytes.length > 1024) {
        indexOut.writeInt(-1);
        Files.write(bytes, new File(outputDir, namedValue.getName() + ".RData"));
      } else {
        indexOut.writeInt(bytes.length);
        indexOut.write(bytes);
      }
    }
    indexOut.close();
  }

  private byte[] serializeSymbol(NamedValue namedValue) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDataWriter writer = new RDataWriter(context, baos);
    writer.serialize(namedValue.getValue());
    baos.close();
    return baos.toByteArray();
  }
}
