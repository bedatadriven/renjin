package org.renjin.packaging;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.renjin.eval.Context;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.Environment;
import org.renjin.sexp.NamedValue;
import org.renjin.sexp.SEXP;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

public class LazyLoadFrameBuilder {

  private static final int VERSION_1 = 1;
  
  private File outputFile;

  private Context context;

  private Predicate<NamedValue> filter = Predicates.alwaysTrue();
  
  public LazyLoadFrameBuilder(Context context) {
    this.context = context;
  }
  
  public LazyLoadFrameBuilder outputTo(File file) {
    this.outputFile = file;
    return this;
  }
  
  public LazyLoadFrameBuilder filter(Predicate<NamedValue> filter) {
    this.filter = filter;
    return this;
  }
  
  public void build(Environment env) throws IOException {
    FileOutputStream fos = new FileOutputStream(outputFile);
    DataOutputStream dos = new DataOutputStream(fos);
    
    dos.writeInt(VERSION_1);
    
    Iterable<NamedValue> toWrite = Iterables.filter(env.namedValues(), filter);
    dos.writeInt(Iterables.size(toWrite));
    
    for(NamedValue boundValue : toWrite) {
      dos.writeUTF(boundValue.getName());
      serialize(dos, boundValue.getValue());
    }
    dos.close();
  }

  private void serialize(DataOutputStream dos, SEXP value) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzos = new GZIPOutputStream(baos);
    RDataWriter writer = new RDataWriter(context, gzos);
    writer.serialize(value);
    gzos.close();
    
    byte[] bytes = baos.toByteArray();
    dos.writeInt(bytes.length);
    dos.write(bytes, 0, bytes.length);
  }
}
