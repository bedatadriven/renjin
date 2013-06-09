package org.renjin.maven;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.apache.maven.plugin.MojoExecutionException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.primitives.io.connections.GzFileConnection;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.*;
import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Prepares datasets, writes an index, and copies them into target/classes
 * as a resource.
 *
 * <p>GNU R supports several types of data formats and compression; we want to
 * simplify everything at compile time into an uncompressed, serialize PairList so
 * we don't have to muck around with it at runtime. The data files will be compressed
 * in a jar in any case.</p>
 */
public class DatasetsBuilder {

  private File packageRoot;
  private File dataDirectory;
  private List<String> index = Lists.newArrayList();

  public DatasetsBuilder(File packageRoot, File dataDirectory) {
    this.packageRoot = packageRoot;
    this.dataDirectory = dataDirectory;
  }

  public void build() throws MojoExecutionException {
    if(dataDirectory.exists() && dataDirectory.listFiles()!=null) {

      for(File dataFile : dataDirectory.listFiles()) {
        try {
          copyDataFile(dataFile);
        } catch(Exception e) {
          System.err.println("ERROR Processing data file " + dataFile);
        }
      }
    }

    if(!index.isEmpty())  {
      writeIndex();
    }
  }

  private void writeIndex() throws MojoExecutionException {
    File indexFile = new File(packageRoot, "datasets");
    String indexText = Joiner.on("\n").join(index);

    try {
      Files.write(indexText, indexFile, Charsets.UTF_8);
    } catch (IOException e) {
      throw new MojoExecutionException("Failed to write dataset index to " + indexFile.getAbsolutePath(), e);
    }
  }

  private void copyDataFile( File dataFile) throws IOException {
    if(dataFile.getName().endsWith(".rda")) {
      copyRdaFile(dataFile);

    } else if(dataFile.getName().endsWith(".txt.gz")) {
      copyTextFile(dataFile, stripExtension(dataFile, ".txt.gz"));

    } else if(dataFile.getName().endsWith(".txt")) {
      copyTextFile(dataFile, stripExtension(dataFile, ".txt"));

    } else {
      throw new RuntimeException("Don't know how to process datafile " + dataFile.getName());
    }
  }

  /**
   * Copy and decompress the saved PairList in rda format.
   * @param dataFile the source data format
   * @throws IOException
   */
  private void copyRdaFile(File dataFile) throws IOException {
    InputStream in = DatasetsBuilder.decompress(new FileInputStream(dataFile));

    String name = stripExtension(dataFile.getName(), ".rda");
    File targetFile = new File(packageRoot, name);

    FileOutputStream fos = new FileOutputStream(targetFile);
    ByteStreams.copy(in, fos);
    in.close();
    fos.close();

    index.add(name);
  }

  private void copyTextFile(File dataFile, String name) throws IOException {
    // Read into a data frame using read.table()
    PairList.Builder args = new PairList.Builder();
    args.add(StringVector.valueOf(dataFile.getAbsolutePath()));
    args.add("header", LogicalVector.TRUE);
    args.add("sep", StringVector.valueOf(";"));

    FunctionCall call = new FunctionCall(Symbol.get("read.table"), args.build());

    Session session = new SessionBuilder().build();
    SEXP dataFrame = session.getTopLevelContext().evaluate(call);

    PairList.Builder pairList = new PairList.Builder();
    pairList.add(name, dataFrame);

    File targetFile = new File(packageRoot, name);
    FileOutputStream out = new FileOutputStream(targetFile);
    RDataWriter writer = new RDataWriter(session.getTopLevelContext(), out);
    writer.save(pairList.build());
    out.close();
  }

  private static String stripExtension(File file, String ext) {
    return stripExtension(file.getName(), ext);
  }

  private static String stripExtension(String name, String ext) {
    return name.substring(0, name.length() - ext.length());
  }

  public static InputStream decompress(InputStream in) throws IOException {

    PushbackInputStream pushBackIn = new PushbackInputStream(in, 2);
    int b1 = pushBackIn.read();
    int b2 = pushBackIn.read();
    pushBackIn.unread(b2);
    pushBackIn.unread(b1);

    if(b1 == GzFileConnection.GZIP_MAGIC_BYTE1 && b2 == GzFileConnection.GZIP_MAGIC_BYTE2) {
      return new GZIPInputStream(pushBackIn);

    } else if(b1 == 0xFD && b2 == '7') {
      // See http://tukaani.org/xz/xz-javadoc/org/tukaani/xz/XZInputStream.html
      // Set a memory limit of 64mb, if this is not sufficient, it will throw
      // an exception rather than an OutOfMemoryError, which will terminate the JVM
      return new XZInputStream(pushBackIn, 64 * 1024 * 1024);
    }
    return in;
  }
}
