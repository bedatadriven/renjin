package org.renjin.packaging;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Closer;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.renjin.eval.EvalException;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.parser.RParser;
import org.renjin.primitives.io.connections.GzFileConnection;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.primitives.io.serialization.RDataWriter;
import org.renjin.sexp.*;
import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * Prepares datasets, writes an index, and copies them into target/classes
 * as a resource.
 *
 * <p>GNU R supports several types of data formats and compression; we want to
 * simplify everything at compile time into an uncompressed, serialized objects
 * we don't have to muck around with it at runtime. The data files will be compressed
 * in a jar in any case.</p>
 * 
 * <p>To complicate things, a single "dataset" can contain multiple R objects. Again,
 * to simplify things at runtime, we'll write out each element to a seperate resource
 * file, and then write a "datasets" index file that maps logical datasets to the 
 * named R objects.
 */
public class DatasetsBuilder {

  private File packageOutputDir;
  private File dataObjectDirectory;
  private File dataDirectory;
  
  /**
   * Maps logical datasets to R object names
   */
  private Multimap<String, String> indexMap = HashMultimap.create();

  /**
   *
   * @param dataDirectory the source directory containing the package's data files
   * @param packageOutputDir the dir to write output files
   */
  public DatasetsBuilder(File dataDirectory, File packageOutputDir) {
    this.packageOutputDir = packageOutputDir;
    
    this.dataObjectDirectory = new File(packageOutputDir, "data");
    this.dataObjectDirectory.mkdirs();
    
    this.dataDirectory = dataDirectory;
  }

  public void build() throws FileNotFoundException  {
    if(dataDirectory.exists()) {
      File[] files = dataDirectory.listFiles();
      if(files != null) {
        for(File dataFile : files) {
          try {
            processDataset(dataFile);
          } catch(EvalException e) {
            System.err.println("ERROR processing data file " + dataFile.getName() + ": " + e.getMessage());
            e.printRStackTrace(System.err);
          } catch(Exception e) {
            System.err.println("Exception processing data file " + dataFile);
            e.printStackTrace();
          }
        }
      }
    }

    if(!indexMap.isEmpty())  {
      writeIndex();
    }
  }

  private void writeIndex() throws FileNotFoundException  {
    
    Properties index = new Properties();
    for(String logicalDatasetName : indexMap.keySet()) {
      index.put(logicalDatasetName, Joiner.on(",").join(indexMap.get(logicalDatasetName)));
    }
    
    File indexFile = new File(packageOutputDir, "datasets");
    FileOutputStream out = new FileOutputStream(indexFile);
    try {
      index.store(out, "Datasets index");
      out.close();
    } catch (IOException e) {
      throw new RuntimeException("Failed to write dataset index to " + indexFile.getAbsolutePath(), e);
    }
  }

  @VisibleForTesting
  void processDataset(File dataFile) throws IOException {
    if(dataFile.getName().endsWith("datalist")) {
      return;
    } else if(dataFile.getName().endsWith(".rda") || dataFile.getName().endsWith(".RData")) {
      processRDataFile(dataFile);

    } else if(dataFile.getName().endsWith(".txt")) {
      processTextFile(dataFile, stripExtension(dataFile), "");

    } else if(dataFile.getName().endsWith(".txt.gz")) {
      processTextFile(dataFile, stripExtension(dataFile, ".txt.gz"), "");

    } else if(dataFile.getName().endsWith(".tab")) {
      processTextFile(dataFile, stripExtension(dataFile), "");

    } else if(dataFile.getName().endsWith(".tab.gz")) {
      processTextFile(dataFile, stripExtension(dataFile, ".tab.gz"), "");

    } else if(dataFile.getName().toLowerCase().endsWith(".csv")) {
      processTextFile(dataFile, stripExtension(dataFile), ";");
      
    } else if(dataFile.getName().endsWith(".R")) {
      processRScript(dataFile, stripExtension(dataFile));
    
    } else {
      throw new RuntimeException("Don't know how to process datafile " + dataFile.getName());
    }
  }

  /**
   * Copy and decompress the saved PairList in rda format.
   * @param dataFile the source data format
   * @throws IOException
   */
  private void processRDataFile(File dataFile) throws IOException {
    Closer closer = Closer.create();
    InputStream in = closer.register(DatasetsBuilder.decompress(dataFile));
    SEXP exp;
    try { 
      RDataReader reader = new RDataReader(in);
      exp = reader.readFile();
    } catch(Throwable e) {
      throw closer.rethrow(e);
    } finally {
      in.close();
    }
    
    if(!(exp instanceof PairList)) {
      throw new UnsupportedOperationException("Expected to find a pairlist in " + dataFile + ", found a " + exp.getTypeName());
    }
    
    String logicalDatasetName = stripExtension(dataFile.getName());
    Session session = new SessionBuilder().withoutBasePackage().build();
    writePairList(logicalDatasetName, session, (PairList)exp);
  }

  private String stripExtension(String name) {
    int lastDot = name.lastIndexOf('.');
    return name.substring(0, lastDot);
  }
  

  private String stripExtension(File dataFile) {
    return stripExtension(dataFile.getName());
  }


  private static String stripExtension(File file, String ext) {
    return stripExtension(file.getName(), ext);
  }

  private static String stripExtension(String name, String ext) {
    return name.substring(0, name.length() - ext.length());
  }

  /**
   * Text files (*.tab, *.csv, *.txt) are processed with utils::read.table() and the
   * resulting data.frame is stored as the single object of the logical dataset.
   */
  private void processTextFile(File dataFile, String logicalDatasetName, String sep) throws IOException {
    // Read into a data frame using read.table()
    PairList.Builder args = new PairList.Builder();
    args.add(StringVector.valueOf(dataFile.getAbsolutePath()));
    args.add("header", LogicalVector.TRUE);
    args.add("sep", StringVector.valueOf(sep));

    FunctionCall readTable = FunctionCall.newCall(Symbol.get("::"), Symbol.get("utils"), Symbol.get("read.table"));
    FunctionCall call = new FunctionCall(readTable, args.build());

    Session session = new SessionBuilder().build();
    SEXP dataFrame = session.getTopLevelContext().evaluate(call);

    PairList.Builder pairList = new PairList.Builder();
    pairList.add(logicalDatasetName, dataFrame);

    writePairList(logicalDatasetName, session, pairList.build());
  }
  
  /**
   * R Scripts are evaluated, and any resulting objects in the global
   * namespace are considered part of the dataset.
   * 
   */
  private void processRScript(File scriptFile, String logicalDatasetName) throws IOException {

    Session session = new SessionBuilder().build();
    FileReader reader = new FileReader(scriptFile);
    ExpressionVector source = RParser.parseAllSource(reader);
    reader.close();
    
    session.getTopLevelContext().evaluate(source);
    
    PairList.Builder pairList = new PairList.Builder();
    for(Symbol symbol : session.getGlobalEnvironment().getSymbolNames()) {
      if(!symbol.getPrintName().startsWith(".")) {
        pairList.add(symbol, session.getGlobalEnvironment().getVariable(symbol));
      }
    }   
    writePairList(logicalDatasetName, session, pairList.build());
  }
  
  /**
   * Write each element of the pairlist out to a separate resource
   * file so that it can be loaded on demand, rather than en mass
   * when a package is loaded. 
   */
  private void writePairList(String logicalDatasetName, Session session,
      PairList pairList) throws FileNotFoundException, IOException {
        
    for(PairList.Node node : pairList.nodes()) {
      
      if(indexMap.values().contains(node.getName())) {
        throw new UnsupportedOperationException(String.format("Duplicate R object '%s' name in dataset '%s' ",
            node.getName(), logicalDatasetName));
      }
      indexMap.put(logicalDatasetName, node.getName());
      
      File targetFile = new File(dataObjectDirectory, node.getName());
      FileOutputStream out = new FileOutputStream(targetFile);
      RDataWriter writer = new RDataWriter(session.getTopLevelContext(), out);
      writer.save(node.getValue());
      out.close();    
    }
  }


  /**
   * Check the input stream for a compression header and wrap in a decompressing
   * stream (gzip or xz) if necessary
   */
  public static InputStream decompress(File file) throws IOException {

    FileInputStream in = new FileInputStream(file);
    int b1 = in.read();
    int b2 = in.read();
    int b3 = in.read();
    in.close();
    
    if(b1 == GzFileConnection.GZIP_MAGIC_BYTE1 && b2 == GzFileConnection.GZIP_MAGIC_BYTE2) {
      return new GZIPInputStream(new FileInputStream(file));

    } else if(b1 == 0xFD && b2 == '7') {
      // See http://tukaani.org/xz/xz-javadoc/org/tukaani/xz/XZInputStream.html
      // Set a memory limit of 64mb, if this is not sufficient, it will throw
      // an exception rather than an OutOfMemoryError, which will terminate the JVM
      return new XZInputStream(new FileInputStream(file), 64 * 1024 * 1024);
      
    } else if (b1 == 'B' && b2 == 'Z' && b3 == 'h' ) {
      return new BZip2CompressorInputStream(new FileInputStream(file));
    
    } else {
      return new FileInputStream(file);
    }
  }
}
