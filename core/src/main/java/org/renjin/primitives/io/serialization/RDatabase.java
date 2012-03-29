package org.renjin.primitives.io.serialization;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Set;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;

import org.renjin.eval.Context;
import org.renjin.primitives.io.ByteArrayCompression;
import org.renjin.sexp.Environment;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.SEXP;
import org.renjin.sexp.Vector;



import com.google.common.collect.Maps;

/**
 * Provides read access to an .rdb file.
 */
public class RDatabase {

  private static class IndexEntry {

    String name;
    int offset;
    int length;
    
    public IndexEntry(String name, Vector entry) {
      super();
      this.name = name;
      this.offset = entry.getElementAsInt(0);
      this.length = entry.getElementAsInt(1);
    }

    @Override
    public String toString() {
      return name + " => " + "[" + offset + ", " + length + "]";
    }
    
  }
 
  private Context context;
  private Environment rho;
  
  private Map<String, IndexEntry> members;
  private Map<String, IndexEntry> environments;
  private MappedByteBuffer buffer;
  private int compression;
  
  
  public RDatabase(Context context, String databaseFile) throws IOException {
    this.context = context;
    loadIndex(databaseFile);
    mapBuffer(databaseFile);
  }
  
  private void mapBuffer(String databaseFile) throws IOException {
    long len = new File(databaseFile).length();
    buffer = 
        new RandomAccessFile(databaseFile, "rw").getChannel()
        .map(FileChannel.MapMode.READ_ONLY, 0, len);
  }

  private File indexFileFromRdb(String rdbFile) {
    if(!rdbFile.endsWith(".rdb")) {
      throw new IllegalArgumentException("Invalid R database name: '" + rdbFile + "'; must end in .rdb");
    }
    return new File(rdbFile.substring(0, rdbFile.length() - ".rdb".length()) + ".rdx");
  }

  private void loadIndex(String rdbFileName) throws IOException {
    File rdxFile = indexFileFromRdb(rdbFileName);
    FileInputStream in = new FileInputStream(rdxFile);
    GZIPInputStream gzin = new GZIPInputStream(in);
    RDataReader reader = new RDataReader(context, gzin);
    ListVector contents = (ListVector)reader.readFile();
    
    members = buildIndex((ListVector) contents.getElementAsSEXP(0));
    environments = buildIndex((ListVector) contents.getElementAsSEXP(1));
    compression = contents.getElementAsInt(2);
    
    gzin.close();
  }

  private Map<String, IndexEntry> buildIndex(ListVector offsets) {
    Map<String, IndexEntry> map = Maps.newHashMap();
    
    for(int i=0;i!=offsets.length();++i) {
      IndexEntry entry = new IndexEntry(offsets.getName(i), (Vector) offsets.getElementAsSEXP(i));
      map.put(entry.name, entry);
    }
    return map;
  }
  
  public SEXP get(String name) throws IOException, DataFormatException {
    IndexEntry entry = members.get(name);
    if(entry == null) {
      throw new IllegalArgumentException(name);
    }
    return readEntry(entry);
  }

  private SEXP readEntry(IndexEntry entry) throws IOException,
      DataFormatException {
    byte[] bytes = getBytes(entry);

    RDataReader reader = new RDataReader(context, rho,
        new ByteArrayInputStream(bytes),
        new RDataReader.PersistentRestorer() {

          @Override
          public SEXP restore(SEXP value) {
            try {
              System.out.println("persistent restore: " + value);
              return getEnvironment(value);
            } catch (Exception e) {
              throw new RuntimeException();
            }
          }
        });

    return reader.readFile();
  }

  private byte[] getBytes(IndexEntry entry) throws IOException,
      DataFormatException {
    byte bytes[] = new byte[entry.length];
    buffer.position(entry.offset);
    buffer.get(bytes, 0, entry.length);
    bytes = ByteArrayCompression.decompress(compression, bytes);
    return bytes;
  }
  
  SEXP getEnvironment(SEXP value) throws IOException, DataFormatException {
    String name = ((Vector)value).getElementAsString(0);
    IndexEntry index = environments.get(name);
    return readEntry(index);
  }

  public Set<String> keySet() {
    return members.keySet();
  }
}
