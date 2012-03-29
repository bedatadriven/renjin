package org.renjin.primitives.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.renjin.eval.EvalException;
import org.renjin.primitives.io.lzma.LzmaDecoder;
import org.renjin.primitives.io.lzma.LzmaEncoder;


/**
 * Utility functions for compression and uncompressing blocks
 * of bytes in R's format
 */
public final class ByteArrayCompression {

  public static byte[] compress(int compression, byte[] buffer) throws Exception {
  
    switch(compression) {
    case 1:
      return ByteArrayCompression.compress1(buffer);
    case 3:
      return ByteArrayCompression.compress3(buffer);
    }
    throw new UnsupportedOperationException("compressed==" + compression
        + " in lazyLoadDBfetch not yet implemented");
  }

  public static byte[] decompress(int compression, byte[] buffer)
      throws IOException, DataFormatException {
    switch (compression) {
    case 1:
      buffer = ByteArrayCompression.decompress1(buffer);
      break;
    case 3:
      buffer = ByteArrayCompression.decompress3(buffer);
      break;
    default:
      throw new UnsupportedOperationException("compressed==" + compression
          + " in lazyLoadDBfetch not yet implemented");
    }
    return buffer;
  }

  public static byte[] decompress1(byte buffer[]) throws IOException,
      DataFormatException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
    int outLength = in.readInt();
  
    Inflater inflater = new Inflater();
    inflater.setInput(buffer, 4, buffer.length - 4);
  
    byte[] result = new byte[outLength];
    inflater.inflate(result);
    inflater.end();
  
    return result;
  }

  public static byte[] compress1(byte buffer[]) throws IOException,
      DataFormatException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(buffer.length);
    
    Deflater deflater = new Deflater();
    deflater.setInput(buffer, 0, buffer.length);
    deflater.finish();
    
    byte compressed[] = new byte[buffer.length * 2];
    int bytesOut = deflater.deflate(compressed);
    deflater.end();
    
    dos.write(compressed, 0, bytesOut);
  
    return baos.toByteArray();
  }

  public static byte[] compress3(byte[] buffer) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);
    dos.writeInt(buffer.length);
    dos.writeByte('Z');
  
    LzmaEncoder encoder = new LzmaEncoder();
    encoder.Code(new ByteArrayInputStream(buffer), baos, buffer.length, buffer.length);
    
    return baos.toByteArray();
  }

  public static byte[] decompress3(byte buffer[]) throws IOException,
      DataFormatException {
    DataInputStream in = new DataInputStream(new ByteArrayInputStream(buffer));
    int outlen = in.readInt();
    byte type = in.readByte();
  
    if (type == 'Z') {
      byte[] properties = Arrays.copyOfRange(buffer, 5, 10);
      ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 10,
          buffer.length - 5);
      ByteArrayOutputStream baos = new ByteArrayOutputStream(outlen);
      LzmaDecoder decoder = new LzmaDecoder();
  
      decoder.SetDecoderProperties(properties);
      if (!decoder.Code(bais, baos, outlen)) {
        throw new IOException("LZMA decompression error");
      }
  
      return baos.toByteArray();
    }
  
    throw new EvalException("decompres3: type = " + (char) type);
    //
    // if (type == 'Z') {
    // lzma_stream strm = LZMA_STREAM_INIT;
    // lzma_ret ret;
    // init_filters();
    // ret = lzma_raw_decoder(&strm, filters);
    // if (ret != LZMA_OK) error("internal error %d in R_decompress3", ret);
    // strm.next_in = p + 5;
    // strm.avail_in = inlen - 5;
    // strm.next_out = buf;
    // strm.avail_out = outlen;
    // ret = lzma_code(&strm, LZMA_RUN);
    // if (ret != LZMA_OK && (strm.avail_in > 0))
    // error("internal error %d in R_decompress3 %d",
    // ret, strm.avail_in);
    // lzma_end(&strm);
    // } else if (type == '2') {
    // int res;
    // res = BZ2_bzBuffToBuffDecompress((char *)buf, &outlen,
    // (char *)(p + 5), inlen - 5, 0, 0);
    // if(res != BZ_OK) error("internal error %d in R_decompress2", res);
    // } else if (type == '1') {
    // uLong outl; int res;
    // res = uncompress(buf, &outl, (Bytef *)(p + 5), inlen - 5);
    // if(res != Z_OK) error("internal error %d in R_decompress1");
    // } else if (type == '0') {
    // buf = p + 5;
    // } else error("unknown type in R_decompress3");
    //
  
  }

}
