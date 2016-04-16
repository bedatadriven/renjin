/*
 * R : A Computer Language for Statistical Data Analysis
 * Copyright (C) 1995, 1996  Robert Gentleman and Ross Ihaka
 * Copyright (C) 1997--2008  The R Development Core Team
 * Copyright (C) 2003, 2004  The R Foundation
 * Copyright (C) 2010 bedatadriven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.renjin.primitives.io.serialization;

import com.google.common.collect.Lists;
import com.google.common.io.ByteSource;
import com.google.common.io.Closeables;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.parser.NumericLiterals;
import org.renjin.primitives.Primitives;
import org.renjin.primitives.sequence.IntSequence;
import org.renjin.primitives.vector.ConvertingStringVector;
import org.renjin.primitives.vector.RowNamesVector;
import org.renjin.sexp.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import static org.renjin.primitives.io.serialization.SerializationFormat.*;
import static org.renjin.sexp.SexpType.LANGSXP;
import static org.renjin.sexp.SexpType.LISTSXP;


public class RDataReader {

  private InputStream conn;
  private StreamReader in;

  private int version;
  private Version writerVersion;
  private Version releaseVersion;

  private List<SEXP> referenceTable = Lists.newArrayList();

  private PersistentRestorer restorer;
  private ReadContext readContext;

  public RDataReader(Context context, InputStream conn) {
    this.readContext = new SessionReadContext(context.getSession());
    this.conn = conn;
  }

  public RDataReader(Context context, Environment rho, InputStream conn, PersistentRestorer restorer) {
    this(context, conn);
    this.restorer = restorer;
  }

  public RDataReader(InputStream conn) {
    this.readContext = new NullReadContext();
    this.conn = conn;
  }

  public SEXP readFile() throws IOException {
    byte streamType = readStreamType(conn);
    in = createStreamReader(streamType, conn);
    readAndVerifyVersion();
    return readExp();
  }

  protected void readAndVerifyVersion() throws IOException {
    version = in.readInt();
    writerVersion = new Version(in.readInt());
    releaseVersion = new Version(in.readInt());

    if(version != VERSION2) {
      if(releaseVersion.isExperimental()) {
        throw new IOException(String.format("cannot read unreleased workspace version %d written by experimental R %s",
            version, writerVersion));
      } else {
        throw new IOException(String.format("cannot read workspace version %d written by R %s; need R %s or newer",
            version, releaseVersion, releaseVersion));
      }
    }
  }

  public static boolean isRDataFile(ByteSource inputSupplier) throws IOException {
    InputStream in = inputSupplier.openStream();
    try {
      byte streamType = readStreamType(in);
      return streamType != -1;
    } finally {
      Closeables.closeQuietly(in);
    }
  }

  public static byte readStreamType(InputStream in) throws IOException {
    byte bytes[] = new byte[7];
    bytes[0] = (byte) in.read();
    bytes[1] = (byte) in.read();

    if(bytes[1] == '\n') {
      switch(bytes[0]) {
        case XDR_FORMAT:
        case ASCII_FORMAT:
        case BINARY_FORMAT:
          return bytes[0];
        default:
          return -1;
      }
    }
    for(int i= 2;i!=7;++i) {
      bytes[i] = (byte) in.read();
    }

    String header = new String(bytes,0,5);
    if(header.equals(ASCII_MAGIC_HEADER)) {
      return ASCII_FORMAT;
    } else if(header.equals(BINARY_MAGIC_HEADER)) {
      return BINARY_FORMAT;
    } else if(header.equals(XDR_MAGIC_HEADER)) {
      return XDR_FORMAT;
    } else {
      return -1;
    }
  }

  private static StreamReader createStreamReader(byte type, InputStream conn) throws IOException {
    switch(type) {
      case XDR_FORMAT:
      case BINARY_FORMAT:
        return new XdrReader(conn);
      case ASCII_FORMAT:
        return new AsciiReader(conn);
      default:
        throw new IOException("Unknown format");
    }
  }

  public SEXP readExp() throws IOException {

    int flags = in.readInt();
    switch(Flags.getType(flags)) {
      case NILVALUE_SXP:
        return Null.INSTANCE;
      case EMPTYENV_SXP:
        return Environment.EMPTY;
      case BASEENV_SXP:
        return readContext.getBaseEnvironment();
      case GLOBALENV_SXP:
        return readContext.getGlobalEnvironment();
      case UNBOUNDVALUE_SXP:
        return Symbol.UNBOUND_VALUE;
      case MISSINGARG_SXP:
        return Symbol.MISSING_ARG;
      case BASENAMESPACE_SXP:
        return readContext.getBaseNamespaceEnvironment();
      case SexpType.REFSXP:
        return readReference(flags);
      case PERSISTSXP:
        return readPersistentExp();
      case SexpType.SYMSXP:
        return readSymbol();
      case PACKAGESXP:
        return readPackage();
      case NAMESPACESXP:
        return readNamespace();
      case SexpType.ENVSXP:
        return readEnv(flags);
      case LISTSXP:
        return readPairList(flags);
      case LANGSXP:
        return readLangExp(flags);
      case SexpType.CLOSXP:
        return readClosure(flags);
      case SexpType.PROMSXP:
        return readPromise(flags);
      case SexpType.DOTSXP:
        return readDotExp(flags);
      case SexpType.EXTPTRSXP:
        return readExternalPointer(flags);
      case WEAKREFSXP:
        return readWeakReference(flags);
      case SexpType.SPECIALSXP:
      case SexpType.BUILTINSXP:
        return readPrimitive(flags);
      case SexpType.CHARSXP:
        return readCharExp(flags);
      case SexpType.LGLSXP:
        return readLogical(flags);
      case SexpType.INTSXP:
        return readIntVector(flags);
      case SexpType.REALSXP:
        return readDoubleExp(flags);
      case SexpType.CPLXSXP:
        return readComplexExp(flags);
      case SexpType.STRSXP:
        return readStringVector(flags);
      case SexpType.VECSXP:
        return readListExp(flags);
      case SexpType.EXPRSXP:
        return readExpExp(flags);
      case SexpType.BCODESXP:
        return readBytecode(flags);
      case CLASSREFSXP:
        throw new IOException("this version of R cannot read class references");
      case GENERICREFSXP:
        throw new IOException("this version of R cannot read generic function references");
      case SexpType.RAWSXP:
        return rawRawVector(flags);
      case SexpType.S4SXP:
        return readS4XP(flags);
      default:
        throw new IOException(String.format("ReadItem: unknown type %d, perhaps written by later version of R",
            Flags.getType(flags)));
    }
  }



  private SEXP rawRawVector(int flags) throws IOException {
    int length = in.readInt();
    byte[] bytes = in.readString(length);
    AttributeMap attributes = readAttributes(flags);
    return new RawVector(bytes, attributes);
  }

  private SEXP readPromise(int flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    SEXP env = readTag(flags);
    SEXP value = readExp();
    SEXP expr = readExp();

    if(env != Null.INSTANCE) {
      return readContext.createPromise(expr, (Environment)env);
    } else {
      return new Promise(expr, value);
    }
  }

  private SEXP readClosure(int flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    Environment env = (Environment) readTag(flags);
    PairList formals = (PairList) readExp();
    SEXP body =  readExp();

    return new Closure(env, formals, body, attributes);
  }

  private SEXP readLangExp(int flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    SEXP tag = readTag(flags);
    SEXP function = readExp();
    PairList arguments = (PairList) readExp();
    return new FunctionCall(function, arguments, attributes);
  }

  /**
   * Reads a GNU R Byte code object.
   * 
   * <p>Renjin does not use the GNU R byte code format, but for the purpose of interoperability, 
   * we want to be able to read in functions byte-code compiled by GNU R. Fortunately, we can do this 
   * quite simply because the original S-Expression is retained along with the byte code as a constant
   * pool entry.</p>
   * 
   */
  private SEXP readBytecode(int flags) throws IOException {
    int nReps = in.readInt();
    SEXP[] reps = new SEXP[nReps];
    return readBC1(reps);
  }

  private SEXP readBC1(SEXP[] reps) throws IOException {
    // Read (and discard) the byte code, which is encoded as IntVector
    SEXP code = readExp();

    // Read the constant pool
    SEXP[] constants = readBytecodeConstants(reps);
    
    // The original S-Expression is stored as the first entry in the constant pool.
    return constants[0];
  }

  /**
   * Reads the constant pool associated with a bytecode object.
   */
  private SEXP[] readBytecodeConstants(SEXP[] reps) throws IOException {
    // Read the constant pool, which contains the original SEXP that we're looking for
    int nEntries = in.readInt();
    SEXP[] pool = new SEXP[nEntries];
    for(int i=0; i < nEntries; ++i) {
      int type = in.readInt();
      switch (type) {
        case SexpType.BCODESXP:
          pool[i] = readBC1(reps);
          break;
        case LANGSXP:
        case LISTSXP:
        case BCREPDEF:
        case BCREPREF:
        case ATTRLANGSXP:
        case ATTRLISTSXP:
          pool[i] = readBCLang(type, reps);
          break;
        default:
          pool[i] = readExp();
      }
    }
    return pool;
  }

  private SEXP readBCLang(int type, SEXP[] reps) throws IOException {
    switch (type) {
      case BCREPREF:
        return reps[in.readInt()];
      
      case BCREPDEF:
      case LANGSXP:
      case LISTSXP:
      case ATTRLANGSXP:
      case ATTRLISTSXP:
      {
        PairList.Node ans;
        int pos = -1;
        if (type == BCREPDEF) {
          pos = in.readInt();
          type = in.readInt();
        }
        
        // Read attributes if defined
        AttributeMap attributes;
        switch (type) {
          case ATTRLANGSXP:
          case ATTRLISTSXP:
            attributes = readAttributes();
            break;
          
          default:
            attributes = AttributeMap.EMPTY;
            break;
        }
        
        // Create either a function call or a plain pair list
        switch (type) {
          case ATTRLANGSXP:
          case LANGSXP:
            ans = new FunctionCall(Null.INSTANCE, Null.INSTANCE, attributes);
            break;
          case ATTRLISTSXP:
          case LISTSXP:
            ans = new PairList.Node(Null.INSTANCE, Null.INSTANCE, attributes, Null.INSTANCE);
            break;
          
          default:
            throw new UnsupportedOperationException("BCLang type: " + type);
        }
        
        if (pos >= 0) {
          reps[pos] = ans;
        }

        ans.setTag(readExp());
        ans.setValue(readBCLang(in.readInt(), reps));
        
        SEXP next = readBCLang(in.readInt(), reps);
        if(next != Null.INSTANCE) {
          ans.setNextNode((PairList.Node) next);
        }
        return ans;
      }

      default:
        return readExp();
    }
  }

  private SEXP readDotExp(int flags) throws IOException {
    throw new IOException("readDotExp not impl");
  }

  private PairList readPairList(int flags) throws IOException {

    PairList.Node head = null;
    PairList.Node tail = null;

    while(Flags.getType(flags) != NILVALUE_SXP) {
      AttributeMap attributes = readAttributes(flags);
      SEXP tag = readTag(flags);
      SEXP value = readExp();

      if(tag == Symbols.ROW_NAMES && RowNamesVector.isOldCompactForm(value)) {
        value = RowNamesVector.fromOldCompactForm(value);
      }

      PairList.Node node = new PairList.Node(tag, value, attributes, Null.INSTANCE);
      if(head == null) {
        head = node;
        tail = node;
      } else {
        tail.setNextNode(node);
        tail = node;
      }

      // read the next element in the list
      flags = in.readInt();
    }
    return head == null ? Null.INSTANCE : head;
  }

  private SEXP readTag(int flags) throws IOException {
    return Flags.hasTag(flags) ? readExp() : Null.INSTANCE;
  }

  private AttributeMap readAttributes(int flags) throws IOException {
    if(Flags.hasAttributes(flags)) {
      return readAttributes();
    } else {
      return AttributeMap.EMPTY;
    }
  }

  private AttributeMap readAttributes() throws IOException {
    SEXP pairList = readExp();
    AttributeMap attributes = AttributeMap.fromPairList((PairList) pairList);
    SEXP rns = attributes.get(Symbols.ROW_NAMES);
      /* 
       * There is a special case when GNU R serializes a empty 
       * row names vector, it uses an integer vector with two entries, 
       * first is NA, the second is the number of rows.
       */
    if (rns instanceof IntVector) {
      IntVector rniv = (IntVector)rns;
      if (rniv.length() == 2 && rniv.isElementNA(0)) {
        ConvertingStringVector csv = new ConvertingStringVector(
            IntSequence.fromTo(1, rniv.getElementAsInt(1)), AttributeMap.EMPTY);
        AttributeMap.Builder amb = attributes.copy();
        amb.set(Symbols.ROW_NAMES, csv);
        attributes = amb.build();
      }
    }
    return attributes;
  }

  private SEXP readPackage() throws IOException {
    throw new IOException("package");
  }

  private SEXP readReference(int flags) throws IOException {
    int i = readReferenceIndex(flags);
    return referenceTable.get(i);
  }

  private int readReferenceIndex(int flags) throws IOException {
    int i = Flags.unpackRefIndex(flags);
    if (i == 0) {
      return in.readInt() - 1;
    } else {
      return i - 1;
    }
  }

  private SEXP readSymbol() throws IOException {

    // always followed by a CHARSEXP
    int flags = in.readInt();
    if(Flags.getType(flags) != SexpType.CHARSXP) {
      throw new IllegalStateException("Expected a CHARSXP");
    }
    String name;
    int length = in.readInt();
    if(length < 0) {
      name = "NA";
    } else {
      name = new String(in.readString(length));
    }
    return addReadRef(Symbol.get(name));
  }

  private SEXP addReadRef(SEXP value) {
    referenceTable.add(value);
    return value;
  }

  private SEXP readNamespace() throws IOException {
    StringVector name = readPersistentNamesVector();
    SEXP namespace = readContext.findNamespace(Symbol.get(name.getElementAsString(0)));
    if(namespace == Null.INSTANCE) {
      throw new IllegalStateException("Cannot find namespace '" + name + "'");
    }
    return addReadRef( namespace );
  }

  private SEXP readEnv(int flags) throws IOException {

    Environment env = Environment.createChildEnvironment(Environment.EMPTY);
    addReadRef(env);

    boolean locked = in.readInt() == 1;
    SEXP parent = readExp();
    SEXP frame = readExp();
    SEXP hashtab = readExp(); // unused

    // NB: environment's attributes is ALWAYS written,
    // regardless of flag
    SEXP attributes = readExp();

    env.setParent( parent == Null.INSTANCE ? Environment.EMPTY : (Environment)parent );
    env.setVariables( (PairList) frame );


    if(locked) {
      env.lock(true);
    }

    return env;
  }

  private SEXP readS4XP(int flags) throws IOException {
    return new S4Object(readAttributes(flags));
  }

  private SEXP readListExp(int flags) throws IOException {
    SEXP[] values = readExpArray();
    AttributeMap attributes = readAttributes(flags);
    return new ListVector(values, attributes);
  }

  private SEXP readExpExp(int flags) throws IOException {
    SEXP[] values = readExpArray();
    AttributeMap attributes = readAttributes(flags);
    return new ExpressionVector(values, attributes);
  }

  private SEXP[] readExpArray() throws IOException {
    int length = in.readInt();
    SEXP values[] = new SEXP[length];
    for(int i=0;i!=length;++i) {
      values[i] = readExp();
    }
    return values;
  }

  private SEXP readStringVector(int flags) throws IOException {
    int length = in.readInt();
    String[] values = new String[length];
    for(int i=0;i!=length;++i) {
      values[i] = ((CHARSEXP)readExp()).getValue();
    }
    return new StringArrayVector(values, readAttributes(flags));
  }

  private SEXP readComplexExp(int flags) throws IOException {
    int length = in.readInt();
    Complex[] values = new Complex[length];
    for(int i=0;i!=length;++i) {
      values[i] = new Complex(in.readDouble(), in.readDouble());
    }
    return new ComplexArrayVector(values, readAttributes(flags));
  }

  private SEXP readDoubleExp(int flags) throws IOException {
    int length = in.readInt();
    double[] values = new double[length];
    for(int i=0;i!=length;++i) {
      values[i] = in.readDouble();
    }
    return new DoubleArrayVector(values, readAttributes(flags));
  }

  private SEXP readIntVector(int flags) throws IOException {
    int length = in.readInt();
    IntBuffer buffer = in.readIntBuffer(length);
    return new IntBufferVector(buffer, length, readAttributes(flags));
  }


  private SEXP readLogical(int flags) throws IOException {
    int length = in.readInt();
    int values[] = new int[length];
    for(int i=0;i!=length;++i) {
      values[i] = in.readInt();
    }
    return new LogicalArrayVector(values, readAttributes(flags));
  }

  private SEXP readCharExp(int flags) throws IOException {
    int length = in.readInt();

    if (length == -1) {
      return new CHARSEXP(StringVector.NA );
    } else  {
      byte buf[] = in.readString(length);
      if(Flags.isUTF8Encoded(flags)) {
        return new CHARSEXP(new String(buf, "UTF8"));
      } else if(Flags.isLatin1Encoded(flags)) {
        return new CHARSEXP(new String(buf, "Latin1"));
      } else {
        return new CHARSEXP(new String(buf));
      }
    }
  }

  private SEXP readPrimitive(int flags) throws IOException {
    int nameLength = in.readInt();
    String name = new String(in.readString(nameLength));
    return Primitives.getBuiltin(name);
  }

  private SEXP readWeakReference(int flags) throws IOException {
    throw new IOException("weakRef not yet impl");
  }

  private SEXP readExternalPointer(int flags) throws IOException {
    ExternalPtr ptr = new ExternalPtr(null);
    addReadRef(ptr);
    //R_SetExternalPtrAddr(s, NULL);
    readExp(); // protected (not used)
    readExp(); // tag (not used)
    ptr = (ExternalPtr) ptr.setAttributes(readAttributes(flags));
    return ptr;
  }

  private SEXP readPersistentExp() throws IOException {
    if(restorer == null) {
      throw new IOException("no restore method available");
    }
    return addReadRef( restorer.restore(readPersistentNamesVector()) );
  }

  private StringVector readPersistentNamesVector() throws IOException {
    if(in.readInt() != 0) {
      throw new IOException("names in persistent strings are not supported yet");
    }
    int len = in.readInt();
    String values[] = new String[len];
    for(int i=0;i!=len;++i) {
      values[i] = ((CHARSEXP)readExp()).getValue();
    }
    return new StringArrayVector(values);
  }

  private interface StreamReader {
    int readInt() throws IOException;
    IntBuffer readIntBuffer(int size) throws IOException;
    byte[] readString(int length) throws IOException;
    double readDouble() throws IOException;
  }

  private static class AsciiReader implements StreamReader {

    private BufferedReader reader;

    private AsciiReader(BufferedReader reader) {
      this.reader = reader;
    }

    private AsciiReader(InputStream in) {
      this(new BufferedReader(new InputStreamReader(in)));
    }

    public String readWord() throws IOException {
      int codePoint;
      do {
        codePoint = reader.read();
        if(codePoint == -1) {
          throw new EOFException();
        }
      } while(Character.isWhitespace(codePoint));

      StringBuilder sb = new StringBuilder();
      while(!Character.isWhitespace(codePoint)) {
        sb.appendCodePoint(codePoint);
        codePoint = reader.read();
      }
      return sb.toString();
    }

    @Override
    public int readInt() throws IOException {
      String word = readWord();
      if("NA".equals(word)) {
        return IntVector.NA;
      } else {
        return Integer.parseInt(word);
      }
    }

    @Override
    public IntBuffer readIntBuffer(int size) throws IOException {
      int[] array = new int[size];
      for(int i=0;i!=size;++i) {
        array[i] = readInt();
      }
      return IntBuffer.wrap(array);
    }

    @Override
    public double readDouble() throws IOException {
      String word = readWord();
      if("NA".equals(word)){
        return DoubleVector.NA;
      } else if("Inf".equals(word)) {
        return Double.POSITIVE_INFINITY;
      } else if("-Inf".equals(word)){
        return Double.NEGATIVE_INFINITY;
      } else {
        return NumericLiterals.parseDouble(word);
      }
    }

    @Override
    public byte[] readString(int length) throws IOException {
      byte buf[] = null;
      if(length > 0) {
        buf = new byte[length];
        int codePoint;
        do {
          codePoint = reader.read();
          if(codePoint == -1) {
            throw new EOFException();
          }
        } while(Character.isWhitespace(codePoint));

        for(int i = 0; i < length; i++) {
          if(codePoint == '\\') {
            codePoint = reader.read();
            switch(codePoint) {
              case 'n': buf[i] = '\n'; break;
              case 't': buf[i] = '\t'; break;
              case 'v': buf[i] = '\013'; break;
              case 'b' : buf[i] = '\b'; break;
              case 'r' : buf[i] = '\r'; break;
              case 'f' : buf[i] = '\f'; break;
              case 'a' : buf[i] = '\007'; break;
              case '\\': buf[i] = '\\'; break;
              case '?' : buf[i] = '\177'; break;
              case '\'': buf[i] = '\''; break;
              case '\"': buf[i] = '\"'; break; /* closing " for emacs */
              case '0': case '1': case '2': case '3':
              case '4': case '5': case '6': case '7':
                int d = 0, j = 0;
                while('0' <= codePoint && codePoint < '8' && j < 3) {
                  d = d * 8 + (codePoint - '0');
                  codePoint = reader.read();
                  j++;
                }
                buf[i] = (byte)d;
                continue;
              default  : buf[i] = (byte)codePoint;
            }
          } else {
            buf[i] = (byte)codePoint;
          }
          codePoint = reader.read();
          if(codePoint == -1) {
            throw new EOFException();
          }
        }
      }

      return buf;
    }
  }

  private static class XdrReader implements StreamReader {
    private final DataInputStream in;

    private XdrReader(DataInputStream in) throws IOException {
      this.in = in;
    }

    public XdrReader(InputStream conn) throws IOException {
      this(new DataInputStream(new BufferedInputStream(conn)));
    }

    @Override
    public int readInt() throws IOException {
      return in.readInt();
    }

    @Override
    public IntBuffer readIntBuffer(int size) throws IOException {
      ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size * 4);
      ReadableByteChannel channel = Channels.newChannel(in);
      while(byteBuffer.hasRemaining()) {
        channel.read(byteBuffer);
      }
      byteBuffer = (ByteBuffer)byteBuffer.rewind();
      byteBuffer.order(ByteOrder.BIG_ENDIAN);
      IntBuffer intBuffer = byteBuffer.asIntBuffer();
      assert intBuffer.limit() == size;
      return intBuffer;
    }

    @Override
    public byte[] readString(int length) throws IOException {
      byte buf[] = new byte[length];
      in.readFully(buf);
      return buf;
    }

    @Override
    public double readDouble() throws IOException {
      long bits = in.readLong();
      return Double.longBitsToDouble(bits);
    }
  }

  /**
   * Interface that allows Renjin containers to restore objects
   * previously stored by {@link RDataWriter.PersistenceHook}
   */
  public interface PersistentRestorer {
    SEXP restore(StringVector values);
  }

}
