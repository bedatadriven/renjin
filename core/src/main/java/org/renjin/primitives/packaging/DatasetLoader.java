package org.renjin.primitives.packaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

import org.renjin.eval.EvalException;
import org.renjin.primitives.io.connections.GzFileConnection;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.Null;
import org.renjin.sexp.PairList;
import org.renjin.sexp.SEXP;
import org.tukaani.xz.XZInputStream;

import com.google.common.io.Closeables;

public class DatasetLoader {


  public static PairList loadDataset(String resourceName, InputStream in)
      throws IOException {
    if(resourceName.endsWith(".rda")) {
      InputStream gzin = DatasetLoader.decompress(in);
      try {
        RDataReader reader = new RDataReader(gzin);
        SEXP exp = reader.readFile();
        if(exp instanceof PairList) {
          return (PairList)exp;
        } else {
          throw new EvalException("expected pairlist from " + resourceName + ", got " + exp.getTypeName());
        }
      } finally {
        Closeables.closeQuietly(gzin);
      }
    } else {
      System.err.println("Don't know how to read " + resourceName + ", skipping for now...");
      return Null.INSTANCE;
    }
  }

  private static InputStream decompress(InputStream in) throws IOException {
    
    PushbackInputStream pushbackIn = new PushbackInputStream(in, 2);
    int b1 = pushbackIn.read();
    int b2 = pushbackIn.read();
    pushbackIn.unread(b2);
    pushbackIn.unread(b1);
    
    if(b1 == GzFileConnection.GZIP_MAGIC_BYTE1 && b2 == GzFileConnection.GZIP_MAGIC_BYTE2) {
      return new GZIPInputStream(pushbackIn);
  
    } else if(b1 == 0xFD && b2 == '7') {
      // See http://tukaani.org/xz/xz-javadoc/org/tukaani/xz/XZInputStream.html
      // Set a memory limit of 64mb, if this is not sufficient, it will throw
      // an exception rather than an OutOfMemoryError, which will terminate the JVM
      return new XZInputStream(pushbackIn, 64 * 1024 * 1024);
    }
    return in;
  }

}
