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

// Base.java

package org.renjin.primitives.io.lzma;

/**
 * @author Igor Pavlov of 7-zip fame
 */
class LzmaBase
{
	public static final int kNumRepDistances = 4;
	public static final int kNumStates = 12;
	
	public static int StateInit()
	{
		return 0;
	}
	
	public static int StateUpdateChar(int index)
	{
		if (index < 4) 
			return 0;
		if (index < 10) 
			return index - 3;
		return index - 6;
	}
	
	public static int StateUpdateMatch(int index)
	{
		return (index < 7 ? 7 : 10); 
	}

	public static int StateUpdateRep(int index)
	{ 
		return (index < 7 ? 8 : 11); 
	}
	
	public static int StateUpdateShortRep(int index)
	{ 
		return (index < 7 ? 9 : 11); 
	}

	public static boolean StateIsCharState(int index)
	{ 
		return index < 7; 
	}
	
	public static final int kNumPosSlotBits = 6;
	public static final int kDicLogSizeMin = 0;
	// public static final int kDicLogSizeMax = 28;
	// public static final int kDistTableSizeMax = kDicLogSizeMax * 2;
	
	public static final int kNumLenToPosStatesBits = 2; // it's for speed optimization
	public static final int kNumLenToPosStates = 1 << kNumLenToPosStatesBits;
	
	public static final int kMatchMinLen = 2;
	
	public static int GetLenToPosState(int len)
	{
		len -= kMatchMinLen;
		if (len < kNumLenToPosStates)
			return len;
		return (int)(kNumLenToPosStates - 1);
	}
	
	public static final int kNumAlignBits = 4;
	public static final int kAlignTableSize = 1 << kNumAlignBits;
	public static final int kAlignMask = (kAlignTableSize - 1);
	
	public static final int kStartPosModelIndex = 4;
	public static final int kEndPosModelIndex = 14;
	public static final int kNumPosModels = kEndPosModelIndex - kStartPosModelIndex;
	
	public static final  int kNumFullDistances = 1 << (kEndPosModelIndex / 2);
	
	public static final  int kNumLitPosStatesBitsEncodingMax = 4;
	public static final  int kNumLitContextBitsMax = 8;
	
	public static final  int kNumPosStatesBitsMax = 4;
	public static final  int kNumPosStatesMax = (1 << kNumPosStatesBitsMax);
	public static final  int kNumPosStatesBitsEncodingMax = 4;
	public static final  int kNumPosStatesEncodingMax = (1 << kNumPosStatesBitsEncodingMax);
	
	public static final  int kNumLowLenBits = 3;
	public static final  int kNumMidLenBits = 3;
	public static final  int kNumHighLenBits = 8;
	public static final  int kNumLowLenSymbols = 1 << kNumLowLenBits;
	public static final  int kNumMidLenSymbols = 1 << kNumMidLenBits;
	public static final  int kNumLenSymbols = kNumLowLenSymbols + kNumMidLenSymbols +
			(1 << kNumHighLenBits);
	public static final  int kMatchMaxLen = kMatchMinLen + kNumLenSymbols - 1;
}
