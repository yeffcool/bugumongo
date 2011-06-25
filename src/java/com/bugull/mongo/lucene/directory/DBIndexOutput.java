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
    
    private final static int BUFFER_SIZE = 32768;
    private final byte[] buffer = new byte[BUFFER_SIZE];
    private long bufferStart = 0;           // position in file of buffer
    private int bufferPosition = 0;         // position in buffer

    private IndexFile file;
    
    public DBIndexOutput(String dirName, String fileName){
        file = new IndexFile(dirName, fileName);
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
            if (BUFFER_SIZE - bufferPosition == 0){
                flush();
            }
        } else {
            if (length > BUFFER_SIZE) {
                if (bufferPosition > 0){
                    flush();
                }
                flushBuffer(b, offset, length);
                bufferStart += length;
            } else {
                int pos = 0; // position in the input data
                int pieceLength;
                while (pos < length) {
                    pieceLength = (length - pos < bytesLeft) ? length - pos : bytesLeft;
                    System.arraycopy(b, pos + offset, buffer, bufferPosition, pieceLength);
                    pos += pieceLength;
                    bufferPosition += pieceLength;
                    // if the buffer is full, flush it
                    bytesLeft = BUFFER_SIZE - bufferPosition;
                    if (bytesLeft == 0) {
                        flush();
                        bytesLeft = BUFFER_SIZE;
                    }
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        flushBuffer(buffer, bufferPosition);
        bufferStart += bufferPosition;
        bufferPosition = 0;
    }
    
    private void flushBuffer(byte[] b, int len) throws IOException {
        flushBuffer(b, 0, len);
    }
    
    private void flushBuffer(byte[] b, int offset, int len) throws IOException{
        byte[] data = Arrays.copyOfRange(b, offset, offset + len);
        file.write(data);
    }

    @Override
    public void close() throws IOException {
        flush();
    }

    @Override
    public long getFilePointer() {
        return bufferStart + bufferPosition;
    }

    @Override
    public void seek(long pos) throws IOException {
        flush();
        bufferStart = pos;
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
