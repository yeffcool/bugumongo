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
import org.apache.lucene.store.IndexOutput;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoIndexOutput extends IndexOutput {
    
    private MongoFile file;
    private boolean isOpen;
    private long position;
    
    public MongoIndexOutput(MongoFile file){
        this.file = file;
        this.isOpen = true;
    }

    /**
     * Forces any buffered output to be written.
     * @throws IOException 
     */
    @Override
    public void flush() throws IOException {
        LuceneIndexFS fs = LuceneIndexFS.getInstance();
        fs.saveFile(file);
    }

    /**
     * Closes this stream to further operations.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        if(isOpen){
            flush();
            isOpen = false;
        }
    }

    /**
     * Returns the current position in this file, where the next write will occur.
     * @return 
     */
    @Override
    public long getFilePointer() {
        return position;
    }

    /**
     * Sets current position in this file, where the next write will occur.
     * @param p
     * @throws IOException 
     */
    @Override
    public void seek(long p) throws IOException {
        this.position = p;
    }

    /**
     * Return the number of bytes in the file.
     * @return
     * @throws IOException 
     */
    @Override
    public long length() throws IOException {
        return file.getFileLength();
    }

    /**
     * Writes a single byte.
     * @param b
     * @throws IOException 
     */
    @Override
    public void writeByte(byte b) throws IOException {
        file.writeByte(position++, b);
    }

    /**
     * Writes an array of bytes.
     * @param bytes
     * @param offset
     * @param len
     * @throws IOException 
     */
    @Override
    public void writeBytes(byte[] bytes, int offset, int len) throws IOException {
        file.writeBytes(position, bytes, offset, len);
        position += len;
    }

}
