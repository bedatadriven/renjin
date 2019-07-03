package org.renjin.tools;


import org.apache.commons.vfs2.FileObject;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
import org.renjin.repackaged.guava.hash.Funnels;
import org.renjin.repackaged.guava.hash.Hasher;
import org.renjin.repackaged.guava.hash.Hashing;
import org.renjin.repackaged.guava.io.ByteStreams;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;

import java.io.IOException;
import java.io.InputStream;

public class Md5 {

  public static StringVector hashFiles(@Current Context context, StringVector files) {
    StringVector.Builder builder = StringVector.newBuilder();
    for (String file : files) {
      try {
        builder.add(hashFile(context.resolveFile(file)));
      } catch (IOException e) {
        builder.addNA();
      }
    }
    builder.setAttribute(Symbols.NAMES, files);

    return builder.build();
  }

  private static String hashFile(FileObject fileObject) throws IOException {
    Hasher hasher = Hashing.md5().newHasher();
    try(InputStream inputStream = fileObject.getContent().getInputStream()) {
      ByteStreams.copy(inputStream, Funnels.asOutputStream(hasher));
    }
    return hasher.hash().toString();
  }
}
