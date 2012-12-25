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

import org.bson.types.ObjectId;

/**
 * A file in mongoDB consists of many chunks.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoChunk {
    
    private MongoFile file;
    private ObjectId id;
    private int chunkNumber;
    private byte[] data;
    private boolean dirty;
    
    public MongoChunk(MongoFile file, int chunkNumber){
        this.file = file;
        this.chunkNumber = chunkNumber;
        this.data = new byte[LuceneIndexFS.CHUNK_SIZE];
        this.dirty = false;
    }
    
    public MongoChunk(MongoFile file, ObjectId id, int chunkNumber, byte[] data){
        this.file = file;
        this.id = id;
        this.chunkNumber = chunkNumber;
        this.data = data;
        this.dirty = false;
    }
    
    public byte readByte(int offset){
        return data[offset];
    }
    
    public void writeByte(int offset, byte b){
        data[offset] = b;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public MongoFile getFile() {
        return file;
    }

    public void setFile(MongoFile file) {
        this.file = file;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(int chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    
}
