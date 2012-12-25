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

import java.io.IOException;
import org.apache.lucene.store.IndexInput;

/**
 * A random-access input stream from a file in a MongoDirectory.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoIndexInput extends IndexInput {
    
    private MongoFile file;
    
    private long position;
    
    public MongoIndexInput(MongoFile file){
        super(MongoIndexInput.class.getSimpleName() + "(" + file.getFilename() + ")");
        this.file = file;
    }

    /**
     * Closes the stream to further operations.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        //do nothing
    }

    /**
     * Returns the current position in this file, where the next read will occur.
     * @return 
     */
    @Override
    public long getFilePointer() {
        return position;
    }

    /**
     * Sets current position in this file, where the next read will occur.
     * @param p
     * @throws IOException 
     */
    @Override
    public void seek(long p) throws IOException {
        this.position = p;
    }

    /**
     * Get the number of bytes in the file.
     * @return 
     */
    @Override
    public long length() {
        return file.getFileLength();
    }

    /**
     * Reads and returns a single byte.
     * @return
     * @throws IOException 
     */
    @Override
    public byte readByte() throws IOException {
        return file.readByte(position++);
    }

    /**
     * Reads a specified number of bytes into an array at the specified offset.
     * @param bytes
     * @param offset
     * @param len
     * @throws IOException 
     */
    @Override
    public void readBytes(byte[] bytes, int offset, int len) throws IOException {
        file.readBytes(position, bytes, offset, len);
        position += len;
    }

}
