package com.bugull.mongo.lucene.directory;

import java.io.IOException;
import org.apache.lucene.store.IndexOutput;

/**
 * 待实现
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoIndexOutput extends IndexOutput{

    @Override
    public void writeByte(byte b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeBytes(byte[] bytes, int i, int i1) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws IOException {
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
    public long length() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
