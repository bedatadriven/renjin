package org.renjin.gnur.api;

import org.renjin.gcc.runtime.BytePtr;
import org.renjin.gcc.runtime.Ptr;
import org.renjin.primitives.Native;
import org.renjin.serialization.RDataWriter;
import org.renjin.serialization.Serialization;
import org.renjin.sexp.SEXP;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;

/**
 * Helpers for dealing with pointers to structures of type {@code R_outpstream_t}.
 *
 * <p>The C structure has the following definition (from Rinternals.h)</p>
 * <pre>
 * struct R_outpstream_st {
 *    R_pstream_data_t data;   // Opaque pstream
 *    R_pstream_format_t type;
 *    int version;
 *    void (*OutChar)(R_outpstream_t, int);
 *    void (*OutBytes)(R_outpstream_t, void *, int);
 *    SEXP (*OutPersistHookFunc)(SEXP, SEXP);
 *    SEXP OutPersistHookData;
 *  };
 * </pre>
 *
 */
class OutPStream {

  /**
   * The {@code data} field is a caller-supplied opaque pointer that is
   * passed to the {@code outBytes} function pointer.
   */
  static final int DATA_OFFSET = 0;

  /**
   * The {@code type} field defines the serialization type:
   * <pre>
   * typedef enum {
   *     R_pstream_any_format,
   *     R_pstream_ascii_format,
   *     R_pstream_binary_format,
   *     R_pstream_xdr_format,
   *     R_pstream_asciihex_format
   * }
   * </pre>
   */
  static final int TYPE_OFFSET = 4;
  static final int VERSION_OFFSET = 8;
  static final int OUTCHAR_OFFSET = 12;
  static final int OUTBYTES_OFFSET = 16;
  static final int HOOK_OFFSET = 20;
  static final int HOOK_DATA_OFFSET = 24;

  private enum Format {
    ANY,
    ASCII,
    BINARY,
    XDR,
    ASCII_HEX
  }

  static void serialize(SEXP sexp, Ptr outputStream) throws IOException {

    checkVersion(outputStream);

    Serialization.SerializationType serializationType = checkType(outputStream);

    // The R_outpstream_st structure contains a function pointer ("outbytes") that
    // does the actual writing. We wrap this MethodHandle in an java.io.Outputstream
    // so that we can write to it.

    Ptr outBytesPtr = outputStream.getPointer(OUTBYTES_OFFSET);
    OutputStream out = new OutBytesWrapper(outputStream, outBytesPtr.toMethodHandle());

    RDataWriter writer = new RDataWriter(Native.currentContext(), out, serializationType);

    writer.serialize(sexp);
  }

  private static void checkVersion(Ptr outputStream) {
    int version = outputStream.getInt(VERSION_OFFSET);
    if(version != RDataWriter.SERIALIZATION_VERSION) {
      throw new UnsupportedOperationException("Unsupported serialization version: " + version);
    }
  }

  private static Serialization.SerializationType checkType(Ptr outputStream) {
    int type = outputStream.getInt(TYPE_OFFSET);
    if(type == Format.XDR.ordinal()) {
      return Serialization.SerializationType.XDR;
    } else if(type == Format.BINARY.ordinal()) {
      return Serialization.SerializationType.BINARY;
    }
    String typeString;
    if(type >= 0 && type < Format.values().length) {
      typeString = Format.values()[type].name() + " (" + type + ")";
    } else {
      typeString = Integer.toString(type);
    }
    throw new UnsupportedOperationException("Unsupported serialization format: " + typeString + ". Only Binary and XDR supported.");
  }

  private static class OutBytesWrapper extends OutputStream {

    private final Ptr outputStream;
    private final MethodHandle writer;

    private final byte[] buffer1 = new byte[1];

    private OutBytesWrapper(Ptr outputStream, MethodHandle writer) {
      this.outputStream = outputStream;
      this.writer = writer;
    }

    @Override
    public void write(int b) throws IOException {
      // Re-use flyweight byte array
      buffer1[0] = (byte) b;
      write(buffer1, 0, 1);
    }

    @Override
    public void write(byte[] array, int offset, int length) throws IOException {
      BytePtr buffer = new BytePtr(array, offset);
      try {
        writer.invoke(outputStream, buffer, length);
      } catch (IOException e) {
        throw e;
      } catch (Throwable throwable) {
        throw new IOException(throwable);
      }
    }
  }
}
