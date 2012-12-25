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
import java.util.Map;
import org.bson.types.ObjectId;

/**
 * An abstract presentation of a lucene index file in mongoDB.
 * A file consists of many chunks. For performance reason, Reading and writing operations are 
 * on chunks internally, not on the file itself.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoFile {
    
    private MongoDirectory directory;
    private ObjectId id;
    private String filename;
    private long fileLength;
    private long lastModify;
    
    private Map<Integer, MongoChunk> chunks;
    
    public MongoFile(MongoDirectory directory, String filename){
        this.directory = directory;
        this.filename = filename;
        this.lastModify = System.currentTimeMillis();
    }
    
    public String getFilename(){
        return filename;
    }
    
    public long getLastModify(){
        return lastModify;
    }
    
    /**
     * Get the number of bytes in the file.
     * @return 
     */
    public long getFileLength(){
        return fileLength;
    }
    
    /**
     * Reads and returns the single byte at the specified position.
     * @param position
     * @return 
     */
    public byte readByte(long position) throws IOException{
        int chunkSize = LuceneIndexFS.CHUNK_SIZE;
        int chunkNumber = (int) (position / chunkSize);
        int chunkOffset = (int) (position - (chunkNumber * chunkSize));
        MongoChunk mc = this.getChunk(chunkNumber, false);
        return mc.readByte(chunkOffset);
    }
    
    /**
     * Reads a specified number of bytes into an array at the specified offset.
     * @param position
     * @param bytes
     * @param offset
     * @param len 
     */
    public void readBytes(long position, byte[] bytes, int offset, int len) throws IOException{
        int chunkSize = LuceneIndexFS.CHUNK_SIZE;
        while (len > 0) {
            int chunkNumber = (int) (position / chunkSize);
            int chunkOffset = (int) (position - (chunkNumber * chunkSize));
            int readSize = Math.min(chunkSize - chunkOffset, len);
            MongoChunk mc = this.getChunk(chunkNumber, false);
            System.arraycopy(mc.getData(), chunkOffset, bytes, offset, readSize);

            position += readSize;
            offset += readSize;
            len -= readSize;
        }
    }
    
    /**
     * Writes a single byte at the specified position
     * @param position
     * @param b 
     */
    public void writeByte(long position, Byte b) throws IOException{
        fileLength = Math.max(position + 1, fileLength);
        int chunkSize = LuceneIndexFS.CHUNK_SIZE;
        int chunkNumber = (int) (position / chunkSize);
        int chunkOffset = (int) (position - (chunkNumber * chunkSize));
        MongoChunk mc = this.getChunk(chunkNumber, true);
        mc.writeByte(chunkOffset, b);
        mc.setDirty(true);
    }
    
    /**
     * Writes a specified number of bytes into an array at the specified offset.
     * @param position
     * @param bytes
     * @param offset
     * @param len 
     */
    public void writeBytes(long position, byte[] bytes, int offset, int len) throws IOException{
        fileLength = Math.max(position + len, fileLength);
        int chunkSize = LuceneIndexFS.CHUNK_SIZE;
        while (len > 0) {
            int chunkNumber = (int) (position / chunkSize);
            int chunkOffset = (int) (position - (chunkNumber * chunkSize));
            int writeSize = Math.min(chunkSize - chunkOffset, len);
            MongoChunk mc = this.getChunk(chunkNumber, true);
            byte[] dest = mc.getData();
            System.arraycopy(bytes, offset, dest, chunkOffset, writeSize);
            mc.setDirty(true);
            position += writeSize;
            offset += writeSize;
            len -= writeSize;
        }
    }
    
    private MongoChunk getChunk(int chunkNumber, boolean createIfNotFound) throws IOException {
        if(chunks.containsKey(chunkNumber)){
            return chunks.get(chunkNumber);
        }
        if(createIfNotFound){
            MongoChunk mc = new MongoChunk(this, chunkNumber);
            chunks.put(chunkNumber, mc);
            return mc;
        }
        throw new IOException("Chunk not found: " + chunkNumber);
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public MongoDirectory getDirectory() {
        return directory;
    }

    public void setDirectory(MongoDirectory directory) {
        this.directory = directory;
    }

    public Map<Integer, MongoChunk> getChunks() {
        return chunks;
    }

    public void setChunks(Map<Integer, MongoChunk> chunks) {
        this.chunks = chunks;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

}
