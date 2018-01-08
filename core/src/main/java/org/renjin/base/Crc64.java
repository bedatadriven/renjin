/*
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.base;

public class Crc64 {

  private static long _crc64Array [] = new long [256];

  /**
   * Initialization of _crc64Array.
   */
  static {

    for( int i = 0 ; i <= 255 ; ++i ) {
      long k = i;
      for( int j = 0 ; j < 8 ; ++j ) {
        if( (k & 1) != 0 ) {
          k = (k >>> 1) ^ 0xd800000000000000l;
        }
        else {
          k = k >>> 1;
        }
      }
      _crc64Array[ i ] = k;
    }
  }


  /**
   * Returns the crc64 checksum for the given sequence.
   *
   * @param sequence sequence
   * @return the crc64 checksum for the sequence
   */
  public static String getCrc64 ( String sequence ) {
    long crc64Number = 0;
    for( int i = 0; i < sequence.length(); ++i ) {
      char symbol = sequence.charAt( i );
      long a = ( crc64Number >>> 8 );
      long b = (crc64Number ^ symbol) & 0xff;
      crc64Number = a ^ _crc64Array[ (int) b ];
    }

    String crc64String = Long.toHexString( crc64Number ).toUpperCase();
    StringBuffer crc64 = new StringBuffer( "0000000000000000" );
    crc64.replace( crc64.length() - crc64String.length(),
            crc64.length(),
            crc64String );

    return crc64.toString();
  }
}

