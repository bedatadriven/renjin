package org.renjin.tools;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.renjin.eval.Context;
import org.renjin.primitives.annotations.Current;
import org.renjin.sexp.StringVector;
import org.renjin.sexp.Symbols;
import org.renjin.util.NamesBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {


  public static StringVector Rmd5(@Current Context context, StringVector paths) throws IOException, NoSuchAlgorithmException {
    StringVector.Builder result = new StringVector.Builder();
    NamesBuilder names = NamesBuilder.withInitialCapacity(paths.length());
    for(String path : paths) {
      result.add(hashFile(context, path));
    }
    result.setAttribute(Symbols.NAMES, names.build());
    return result.build();
  }

  protected static String hashFile(Context context, String path)
      throws FileSystemException, NoSuchAlgorithmException, IOException {
    try {
      FileObject file = context.resolveFile(path);
      InputStream in = file.getContent().getInputStream();
      byte[] buffer = new byte[1024];
      MessageDigest complete = MessageDigest.getInstance("MD5");
      int numRead;
  
      do {
        numRead = in.read(buffer);
        if (numRead > 0) {
          complete.update(buffer, 0, numRead);
        }
      } while (numRead != -1);
  
      in.close();
      
      StringBuilder string = new StringBuilder();
      byte[] b = complete.digest();
  
      for (int i=0; i < b.length; i++) {
        string.append(Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ));
      }
      return string.toString();
    } catch(FileSystemException e) {
      return StringVector.NA;
    }
  }
}
