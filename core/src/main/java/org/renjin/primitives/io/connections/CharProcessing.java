/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-${$file.lastModified.year} BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  https://www.gnu.org/licenses/gpl-2.0.txt
 *
 */

package org.renjin.primitives.io.connections;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

public class CharProcessing {


  public static void read(ReadableByteChannel inputChannel, Charset charset, int bufferSize, CharProcessor processor) throws IOException {


    CharsetDecoder decoder = charset.newDecoder();

    int charBufferSize = Math.round(bufferSize / decoder.maxCharsPerByte());
    char[] charArray = new char[charBufferSize];
    CharBuffer charBuffer = CharBuffer.wrap(charArray);

    ByteBuffer inputBuffer = ByteBuffer.allocate(bufferSize);

    while(true) {

      // Fill the input buffer with bytes
      boolean endOfInput = false;
      while(inputBuffer.remaining() > 0) {
        int bytesRead = inputChannel.read(inputBuffer);
        if(bytesRead < 0) {
          endOfInput = true;
          break;
        }
      }

      // Decode into the character buffer
      inputBuffer.flip();
      CoderResult decodeResult = decoder.decode(inputBuffer, charBuffer, endOfInput);

      // Process the characters
      charBuffer.flip();
      processor.process(charArray, charBuffer.remaining(), endOfInput);

      if(endOfInput) {
        break;
      }

      // Prepare our buffers for the next round
      charBuffer.clear();
      inputBuffer.clear();
    }
  }


  public static void read(InputStream inputStream, int bufferSize, ByteProcessor processor) throws IOException {
    read(Channels.newChannel(inputStream), bufferSize, processor);
  }

  public static void read(ReadableByteChannel inputChannel, int bufferSize, ByteProcessor processor) throws IOException {

    ByteBuffer inputBuffer = ByteBuffer.allocate(bufferSize);

    while(true) {

      // Fill the input buffer with bytes
      boolean endOfInput = false;
      while(inputBuffer.remaining() > 0) {
        int bytesRead = inputChannel.read(inputBuffer);
        if(bytesRead < 0) {
          endOfInput = true;
          break;
        }
      }
      // Process the bytes
      inputBuffer.flip();
      processor.process(inputBuffer, endOfInput);

      if(endOfInput) {
        break;
      }

      // Prepare our buffers for the next round
      inputBuffer.clear();
    }
  }


  public static void read(ByteBuffer inputBuffer, Charset charset, int bufferSize, CharProcessor processor) throws IOException {


    CharsetDecoder decoder = charset.newDecoder();

    int charBufferSize = Math.round(bufferSize / decoder.maxCharsPerByte());
    char[] charArray = new char[charBufferSize];
    CharBuffer charBuffer = CharBuffer.wrap(charArray);


    while(true) {

      // Decode into the character buffer
      CoderResult decodeResult = decoder.decode(inputBuffer, charBuffer, true);
      boolean endOfInput = decodeResult == CoderResult.UNDERFLOW;

      // Process the characters
      charBuffer.flip();
      processor.process(charArray, charBuffer.length(), endOfInput);

      if(endOfInput) {
        break;
      }

      // Prepare our buffers for the next round
      charBuffer.clear();
    }
  }
}
