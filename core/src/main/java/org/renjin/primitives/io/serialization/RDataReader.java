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
import com.google.common.io.InputSupplier;
import org.apache.commons.math.complex.Complex;
import org.renjin.eval.Context;
import org.renjin.parser.ParseUtil;
import org.renjin.primitives.Primitives;
import org.renjin.sexp.*;

import java.io.*;
import java.util.List;

import static org.renjin.primitives.io.serialization.SerializationFormat.*;


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
  
  public static boolean isRDataFile(InputSupplier<InputStream> inputSupplier) throws IOException {
    InputStream in = inputSupplier.getInput();
    try {
      byte streamType = readStreamType(in);
      return streamType != -1;
    } finally {
      in.close();
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
        return new XdrReader(conn);
      case ASCII_FORMAT:
        return new AsciiReader(conn);
      case BINARY_FORMAT:
        return new BinaryReader(conn);
      default:
        throw new IOException("Unknown format");
    }
  }

  public SEXP readExp() throws IOException {

    Flags flags = new Flags(in.readInt());
    switch(flags.type) {
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
      case REFSXP:
        return readReference(flags);
      case PERSISTSXP:
        return readPersistentExp();
      case SYMSXP:
        return readSymbol();
      case PACKAGESXP:
        return readPackage();
      case NAMESPACESXP:
        return readNamespace();
      case ENVSXP:
        return readEnv(flags);
      case LISTSXP:
        return readPairList(flags);
      case LANGSXP:
        return readLangExp(flags);
      case CLOSXP:
        return readClosure(flags);
      case PROMSXP:
        return readPromise(flags);
      case DOTSXP:
        return readDotExp(flags);
      case EXTPTRSXP:
        return readExternalPointer(flags);
      case WEAKREFSXP:
        return readWeakReference(flags);
      case SPECIALSXP:
      case BUILTINSXP:
        return readPrimitive(flags);
      case CHARSXP:
        return readCharExp(flags);
      case LGLSXP:
        return readLogical(flags);
      case INTSXP:
        return readIntVector(flags);
      case REALSXP:
        return readDoubleExp(flags);
      case CPLXSXP:
        return readComplexExp(flags);
      case STRSXP:
        return readStringVector(flags);
      case VECSXP:
        return readListExp(flags);
      case EXPRSXP:
        return readExpExp(flags);
      case BCODESXP:
        throw new IOException("Byte code expressions are not supported.");
      case CLASSREFSXP:
        throw new IOException("this version of R cannot read class references");
      case GENERICREFSXP:
        throw new IOException("this version of R cannot read generic function references");
      case RAWSXP:
        return rawRawVector(flags);
      case S4SXP:
        return readS4XP(flags);
      default:
        throw new IOException(String.format("ReadItem: unknown type %d, perhaps written by later version of R", flags.type));
    }
  }


  private SEXP rawRawVector(Flags flags) throws IOException {
    int length = in.readInt();
    byte[] bytes = in.readString(length);
    AttributeMap attributes = readAttributes(flags);
    return new RawVector(bytes, attributes);
  }

  private SEXP readPromise(Flags flags) throws IOException {
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

  private SEXP readClosure(Flags flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    Environment env = (Environment) readTag(flags);
    PairList formals = (PairList) readExp();
    SEXP body =  readExp();

    return new Closure(env, formals, body, attributes);
  }

  private SEXP readLangExp(Flags flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    SEXP tag = readTag(flags);
    SEXP function = readExp();
    PairList arguments = (PairList) readExp();
    return new FunctionCall(function, arguments, attributes);
  }

  private SEXP readDotExp(Flags flags) throws IOException {
    throw new IOException("readDotExp not impl");
  }

  private PairList readPairList(Flags flags) throws IOException {
    AttributeMap attributes = readAttributes(flags);
    SEXP tag = readTag(flags);
    SEXP value = readExp();
    PairList nextNode = (PairList) readExp();

    return new PairList.Node(tag, value, attributes, nextNode);
  }

  private SEXP readTag(Flags flags) throws IOException {
    return flags.hasTag ? readExp() : Null.INSTANCE;
  }

  private AttributeMap readAttributes(Flags flags) throws IOException {
    if(flags.hasAttributes) {
      SEXP pairList = readExp();
      return AttributeMap.fromPairList((PairList) pairList);
    } else {
      return AttributeMap.EMPTY;
    }
  }

  private SEXP readPackage() throws IOException {
    throw new IOException("package");
  }

  private SEXP readReference(Flags flags) throws IOException {
    int i = readReferenceIndex(flags);
    return referenceTable.get(i);
  }

  private int readReferenceIndex(Flags flags) throws IOException {
    int i = flags.unpackRefIndex();
    if (i == 0)
      return in.readInt() - 1;
    else
      return i - 1;
  }

  private SEXP readSymbol() throws IOException {
    CHARSEXP printName = (CHARSEXP) readExp();
    return addReadRef(Symbol.get(printName.getValue()));
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

  private SEXP readEnv(Flags flags) throws IOException {

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

  private SEXP readS4XP(Flags flags) throws IOException {
    return new S4Object(readAttributes(flags));
  }

  private SEXP readListExp(Flags flags) throws IOException {
    SEXP[] values = readExpArray();
    AttributeMap attributes = readAttributes(flags);
    return new ListVector(values, attributes);
  }

  private SEXP readExpExp(Flags flags) throws IOException {
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

  private SEXP readStringVector(Flags flags) throws IOException {
    int length = in.readInt();
    String[] values = new String[length];
    for(int i=0;i!=length;++i) {
      values[i] = ((CHARSEXP)readExp()).getValue();
    }
    return new StringArrayVector(values, readAttributes(flags));
  }

  private SEXP readComplexExp(Flags flags) throws IOException {
    int length = in.readInt();
    Complex[] values = new Complex[length];
    for(int i=0;i!=length;++i) {
      values[i] = new Complex(in.readDouble(), in.readDouble());
    }
    return new ComplexVector(values, readAttributes(flags));
  }

  private SEXP readDoubleExp(Flags flags) throws IOException {
    int length = in.readInt();
    double[] values = new double[length];
    for(int i=0;i!=length;++i) {
      values[i] = in.readDouble();
    }
    return new DoubleArrayVector(values, readAttributes(flags));
  }

  private SEXP readIntVector(Flags flags) throws IOException {
    int length = in.readInt();
    int[] values = new int[length];
    for(int i=0;i!=length;++i) {
      values[i] = in.readInt();
    }
    return new IntArrayVector(values, readAttributes(flags));
  }


  private SEXP readLogical(Flags flags) throws IOException {
    int length = in.readInt();
    int values[] = new int[length];
    for(int i=0;i!=length;++i) {
      values[i] = in.readInt();
    }
    return new LogicalArrayVector(values, readAttributes(flags));
  }

  private SEXP readCharExp(Flags flags) throws IOException {
    int length = in.readInt();

    if (length == -1) {
      return new CHARSEXP(StringVector.NA );
    } else  {
      byte buf[] = in.readString(length);
      if(flags.isUTF8Encoded()) {
        return new CHARSEXP(new String(buf, "UTF8"));
      } else if(flags.isLatin1Encoded()) {
        return new CHARSEXP(new String(buf, "Latin1"));
      } else {
        return new CHARSEXP(new String(buf));
      }
    }
  }

  private SEXP readPrimitive(Flags flags) throws IOException {
    int nameLength = in.readInt();
    String name = new String(in.readString(nameLength));
    return Primitives.getBuiltin(name);
  }

  private SEXP readWeakReference(Flags flags) throws IOException {
    throw new IOException("weakRef not yet impl");
  }

  private SEXP readExternalPointer(Flags flags) throws IOException {
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
    byte[] readString(int length) throws IOException;
    double readDouble() throws IOException;
  }

  private static class BinaryReader implements StreamReader {

    private final DataInput in;

    private BinaryReader(DataInputStream in) throws IOException {
      this.in = in;
    }
    private BinaryReader(InputStream in) throws IOException {
      this(new DataInputStream(in));
    }

    public int readInt() throws IOException {
      return in.readInt();
    }

    @Override
    public byte[] readString(int length) throws IOException {
      byte buf[] = new byte[length];
      in.readFully(buf);
      return buf;
    }

    @Override
    public double readDouble() throws IOException {
      return in.readDouble();
    }
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
    public double readDouble() throws IOException {
      String word = readWord();
      if("NA".equals(word)){
        return DoubleVector.NA;
      } else if("Inf".equals(word)) {
        return Double.POSITIVE_INFINITY;
      } else if("-Inf".equals(word)){
        return Double.NEGATIVE_INFINITY;
      } else {
        return ParseUtil.parseDouble(word);
      }
    }

    @Override
    public byte[] readString(int length) throws IOException {
//if (length > 0) {
//	    int c, d, i, j;
//	    struct R_instring_stream_st iss;
//
//	    InitInStringStream  (&iss, stream);
//	    while(isspace(c = GetChar(&iss)))
//		;
//	    UngetChar(&iss, c);
//	    for (i = 0; i < length; i++) {
//		if ((c =  GetChar(&iss)) == '\\') {
//		    switch(c = GetChar(&iss)) {
//		    case 'n' : buf[i] = '\n'; break;
//		    case 't' : buf[i] = '\t'; break;
//		    case 'v' : buf[i] = '\v'; break;
//		    case 'b' : buf[i] = '\b'; break;
//		    case 'r' : buf[i] = '\r'; break;
//		    case 'f' : buf[i] = '\f'; break;
//		    case 'a' : buf[i] = '\a'; break;
//		    case '\\': buf[i] = '\\'; break;
//		    case '?' : buf[i] = '\?'; break;
//		    case '\'': buf[i] = '\''; break;
//		    case '\"': buf[i] = '\"'; break; /* closing " for emacs */
//		    case '0': case '1': case '2': case '3':
//		    case '4': case '5': case '6': case '7':
//			d = 0; j = 0;
//			while('0' <= c && c < '8' && j < 3) {
//			    d = d * 8 + (c - '0');
//			    c = GetChar(&iss);
//			    j++;
//			}
//			buf[i] = d;
//			UngetChar(&iss, c);
//			break;
//		    default  : buf[i] = c;
//		    }
//		}
//		else buf[i] = c;
      throw new IOException("reading strings from ascii file not yet impl");
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
