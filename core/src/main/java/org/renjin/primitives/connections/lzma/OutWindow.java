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

// LZ.OutWindow

package org.renjin.primitives.connections.lzma;

import java.io.IOException;

/**
 * @author Igor Pavlov of 7-zip fame
 */
class OutWindow
{
	byte[] _buffer;
	int _pos;
	int _windowSize = 0;
	int _streamPos;
	java.io.OutputStream _stream;
	
	public void Create(int windowSize)
	{
		if (_buffer == null || _windowSize != windowSize)
			_buffer = new byte[windowSize];
		_windowSize = windowSize;
		_pos = 0;
		_streamPos = 0;
	}
	
	public void SetStream(java.io.OutputStream stream) throws IOException
	{
		ReleaseStream();
		_stream = stream;
	}
	
	public void ReleaseStream() throws IOException
	{
		Flush();
		_stream = null;
	}
	
	public void Init(boolean solid)
	{
		if (!solid)
		{
			_streamPos = 0;
			_pos = 0;
		}
	}
	
	public void Flush() throws IOException
	{
		int size = _pos - _streamPos;
		if (size == 0)
			return;
		_stream.write(_buffer, _streamPos, size);
		if (_pos >= _windowSize)
			_pos = 0;
		_streamPos = _pos;
	}
	
	public void CopyBlock(int distance, int len) throws IOException
	{
		int pos = _pos - distance - 1;
		if (pos < 0)
			pos += _windowSize;
		for (; len != 0; len--)
		{
			if (pos >= _windowSize)
				pos = 0;
			_buffer[_pos++] = _buffer[pos++];
			if (_pos >= _windowSize)
				Flush();
		}
	}
	
	public void PutByte(byte b) throws IOException
	{
		_buffer[_pos++] = b;
		if (_pos >= _windowSize)
			Flush();
	}
	
	public byte GetByte(int distance)
	{
		int pos = _pos - distance - 1;
		if (pos < 0)
			pos += _windowSize;
		return _buffer[pos];
	}
}
