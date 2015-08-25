package org.renjin.tools;


import com.google.common.hash.Funnels;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import org.apache.commons.vfs2.FileObject;
import org.renjin.eval.Context;
import org.renjin.invoke.annotations.Current;
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
        InputStream inputStream = fileObject.getContent().getInputStream();
        Hasher hasher = Hashing.md5().newHasher();
        try {
            ByteStreams.copy(inputStream, Funnels.asOutputStream(hasher));
            
        } finally {
            Closeables.closeQuietly(inputStream);
        }
        
        return hasher.hash().toString();
    }
}
