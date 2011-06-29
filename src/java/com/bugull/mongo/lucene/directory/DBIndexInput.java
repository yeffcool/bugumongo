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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.store.IndexInput;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBIndexInput extends IndexInput{
    
    private final static Logger logger = Logger.getLogger(IndexFile.class);
    
    private int position;
    private int length;
    private byte[] data;
    
    public DBIndexInput(String dirName, String fileName){
        IndexFile file = new IndexFile(dirName, fileName);
        ByteArrayOutputStream os = file.getOutputStream();
        if(os != null){
            data = os.toByteArray();
            try {
                os.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
            length = data.length;
        }
        position = 0;
    }

    @Override
    public byte readByte() throws IOException {
        if(position + 1 <= length){
            return data[position++];
        }else{
            throw new IOException("Reading past end of file");
        }
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        if(position + len <= length){
            System.arraycopy(data, position, b, offset, len);
            position += len;
        }else{
            throw new IOException("Reading past end of file");
        }
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }

    @Override
    public long getFilePointer() {
        return position;
    }

    @Override
    public void seek(long pos) throws IOException {
        if(pos <= length){
            position = (int)pos;
        }else{
            throw new IOException("seeking past end of file");
        }
    }

    @Override
    public long length() {
        return length;
    }
    
}
