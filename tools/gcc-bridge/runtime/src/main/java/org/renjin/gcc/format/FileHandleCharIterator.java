package org.renjin.gcc.format;

import org.renjin.gcc.runtime.FileHandle;

import java.io.IOException;

public class FileHandleCharIterator implements CharIterator, AutoCloseable {

    private FileHandle fileHandle;
    private boolean eof;
    private char nextChar;

    public FileHandleCharIterator(FileHandle fileHandle) {
        this.fileHandle = fileHandle;
        readNext();
    }

    private void readNext() {
        int b = 0;
        try {
            b = fileHandle.read();
        } catch (IOException e) {
            eof = true;
            fileHandle.setError(e);
        }
        if(b == -1) {
            eof = true;
        } else {
            nextChar = (char) b;
        }
    }

    @Override
    public boolean hasMore() {
        return !eof;
    }

    @Override
    public char peek() {
        return nextChar;
    }

    @Override
    public char next() {
        char c = nextChar;
        readNext();
        return c;
    }

    @Override
    public void close() {
        if(!eof) {
            try {
                fileHandle.seekCurrent(-1L);
            } catch (IOException e) {
                fileHandle.setError(e);
            }
        }
    }
}
