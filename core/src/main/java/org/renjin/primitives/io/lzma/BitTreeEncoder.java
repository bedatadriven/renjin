package org.renjin.primitives.io.lzma;
import java.io.IOException;

public class BitTreeEncoder
{
	short[] Models;
	int NumBitLevels;
	
	public BitTreeEncoder(int numBitLevels)
	{
		NumBitLevels = numBitLevels;
		Models = new short[1 << numBitLevels];
	}
	
	public void Init()
	{
		RangeDecoder.InitBitModels(Models);
	}
	
	public void Encode(RangeEncoder rangeEncoder, int symbol) throws IOException
	{
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0; )
		{
			bitIndex--;
			int bit = (symbol >>> bitIndex) & 1;
			rangeEncoder.Encode(Models, m, bit);
			m = (m << 1) | bit;
		}
	}
	
	public void ReverseEncode(RangeEncoder rangeEncoder, int symbol) throws IOException
	{
		int m = 1;
		for (int  i = 0; i < NumBitLevels; i++)
		{
			int bit = symbol & 1;
			rangeEncoder.Encode(Models, m, bit);
			m = (m << 1) | bit;
			symbol >>= 1;
		}
	}
	
	public int GetPrice(int symbol)
	{
		int price = 0;
		int m = 1;
		for (int bitIndex = NumBitLevels; bitIndex != 0; )
		{
			bitIndex--;
			int bit = (symbol >>> bitIndex) & 1;
			price += RangeEncoder.GetPrice(Models[m], bit);
			m = (m << 1) + bit;
		}
		return price;
	}
	
	public int ReverseGetPrice(int symbol)
	{
		int price = 0;
		int m = 1;
		for (int i = NumBitLevels; i != 0; i--)
		{
			int bit = symbol & 1;
			symbol >>>= 1;
			price += RangeEncoder.GetPrice(Models[m], bit);
			m = (m << 1) | bit;
		}
		return price;
	}
	
	public static int ReverseGetPrice(short[] Models, int startIndex,
			int NumBitLevels, int symbol)
	{
		int price = 0;
		int m = 1;
		for (int i = NumBitLevels; i != 0; i--)
		{
			int bit = symbol & 1;
			symbol >>>= 1;
			price += RangeEncoder.GetPrice(Models[startIndex + m], bit);
			m = (m << 1) | bit;
		}
		return price;
	}
	
	public static void ReverseEncode(short[] Models, int startIndex,
			RangeEncoder rangeEncoder, int NumBitLevels, int symbol) throws IOException
	{
		int m = 1;
		for (int i = 0; i < NumBitLevels; i++)
		{
			int bit = symbol & 1;
			rangeEncoder.Encode(Models, startIndex + m, bit);
			m = (m << 1) | bit;
			symbol >>= 1;
		}
	}
}
