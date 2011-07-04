/**
 * Copyright (c) www.bugull.com
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bugull.mongo.lucene.directory;

import com.bugull.mongo.cache.IndexFileCache;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.store.IndexInput;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBIndexInput extends IndexInput{
    
    private final static Logger logger = Logger.getLogger(IndexFile.class);
    
    private byte[] buffer;
    private int length;
    private int bufferPosition = 0;
    private long filePosition = 0;
    
    private IndexFile file;
    
    public DBIndexInput(String dirname, String filename){
        file = IndexFileCache.getInstance().get(dirname, filename);
        if(file.exists()){
            fill();
        }
    }
    
    private void fill(){
        buffer = file.getChunkData(filePosition);
        length = buffer.length;
        bufferPosition = 0;
    }

    @Override
    public byte readByte() throws IOException {
        if(!file.exists()){
            throw new IOException("File does not exists");
        }
        if (bufferPosition >= length){
            fill();
        }
        filePosition++;
        return buffer[bufferPosition++];
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        if(!file.exists()){
            throw new IOException("File does not exists");
        }
        if (bufferPosition >= length){
            fill();
        }
        if(bufferPosition + len <= length){
            System.arraycopy(buffer, bufferPosition, b, offset, len);
            filePosition += len;
            bufferPosition += len;
        }else{
            int diff = length - bufferPosition;
            System.arraycopy(buffer, bufferPosition, b, offset, diff);
            filePosition += diff;
            bufferPosition += diff;
            readBytes(b, offset + diff, len - diff);
        }
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }

    @Override
    public long getFilePointer() {
        return filePosition;
    }

    @Override
    public void seek(long pos) throws IOException {
        filePosition = pos;
        fill();
        bufferPosition = file.getPositionInChunk(filePosition);
    }

    @Override
    public long length() {
        return length;
    }
    
}
