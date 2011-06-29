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
import java.util.Arrays;
import org.apache.lucene.store.IndexOutput;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBIndexOutput extends IndexOutput{
    
    private final static int BUFFER_SIZE = 16384;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private int bufferPosition = 0;    // position in buffer
    
    private long filePosition;    // position in file
    private IndexFile file;
    
    public DBIndexOutput(String dirName, String fileName){
        file = new IndexFile(dirName, fileName);
        if(file.exists()){
            filePosition = file.getLength();
        }else{
            filePosition = 0;
        }
    }

    @Override
    public void writeByte(byte b) throws IOException {
        if (bufferPosition >= BUFFER_SIZE){
            flush();
        }
        buffer[bufferPosition++] = b;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {
        int bytesLeft = BUFFER_SIZE - bufferPosition;
        if (bytesLeft >= length) {
            System.arraycopy(b, offset, buffer, bufferPosition, length);
            bufferPosition += length;
            if (bufferPosition >= BUFFER_SIZE){
                flush();
            }
        } else {
            if (length >= BUFFER_SIZE) {
                if (bufferPosition > 0){
                    flush();
                }
                flushBuffer(b, offset, length);
            } else {
                if(length < bytesLeft){
                    System.arraycopy(b, offset, buffer, bufferPosition, length);
                    bufferPosition += length;
                }else{
                    System.arraycopy(b, offset, buffer, bufferPosition, bytesLeft);
                    flush();
                    System.arraycopy(b, offset + bytesLeft, buffer, 0, length - bytesLeft);
                    bufferPosition = length - bytesLeft;
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if(bufferPosition > 0){
            flushBuffer(buffer, 0, bufferPosition);
        }
    }
    
    private void flushBuffer(byte[] b, int offset, int len) throws IOException{
        byte[] data = Arrays.copyOfRange(b, offset, offset + len);
        file.write(data, filePosition);
        filePosition += len;
        bufferPosition = 0;
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public long getFilePointer() {
        return filePosition + bufferPosition;
    }

    @Override
    public void seek(long pos) throws IOException {
        filePosition = pos;
        flush();
    }

    @Override
    public long length() throws IOException {
        if(file.exists()){
            return file.getLength();
        }else{
            throw new IOException("File does not exist");
        }
    }
    
}
