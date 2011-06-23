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
 * 待实现
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBIndexOutput extends IndexOutput{

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
