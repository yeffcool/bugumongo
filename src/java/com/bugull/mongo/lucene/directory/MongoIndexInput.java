package com.bugull.mongo.lucene.directory;

import java.io.IOException;
import org.apache.lucene.store.IndexInput;

/**
 * 待实现
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoIndexInput extends IndexInput{

    @Override
    public byte readByte() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void readBytes(byte[] bytes, int i, int i1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getFilePointer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void seek(long l) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long length() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
