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

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.fs.BuguFS;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.util.Arrays;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexFile {
    
    private String dirname;
    private String filename;
    private GridFS fs;
    private DBObject dbo;
    
    private DBCollection filesColl;
    private DBCollection chunksColl;
    
    private boolean exist = false;
    private int chunkSize = 0;
    
    public IndexFile(String dirname, String filename){
        this.dirname = dirname;
        this.filename = filename;
        fs = BuguFS.getInstance().getFS();
        dbo = new BasicDBObject("filename", filename);
        dbo.put("dirname", dirname);
        DB db = BuguConnection.getInstance().getDB();
        filesColl = db.getCollection("fs.files");
        chunksColl = db.getCollection("fs.chunks");
    }
    
    public boolean exists(){
        if(!exist){
            DBObject file = filesColl.findOne(dbo);
            exist = file==null?false:true;
            if(exist){
                chunkSize = Integer.parseInt(file.get("chunkSize").toString());
            }
        }
        return exist;
    }
    
    private void create(byte[] data){
        GridFSInputFile f = fs.createFile(data);
        f.setFilename(filename);
        f.put("dirname", dirname);
        f.put("lastModify", System.currentTimeMillis());
        f.save();
    }
    
    public void write(byte[] data, long position){
        if(data==null || data.length==0){
            return;
        }
        if(!exists()){
            create(data);
        }else if(position >= getLength()){    //append at the last chunk
            append(data);
        }else{
            update(data, position);    //update from the position
        }
    }
    
    private void append(byte[] data){
        DBObject file = filesColl.findOne(dbo);
        long length = Long.parseLong(file.get("length").toString());
        int n = (int)(length / chunkSize);
        ObjectId id = (ObjectId)file.get("_id");
        DBObject query = new BasicDBObject("files_id", id);
        query.put("n", n);
        DBObject lastChunk = chunksColl.findOne(query);
        int dataLen = data.length;
        if(length % chunkSize == 0){
            //append new chunk
            DBObject newChunk = new BasicDBObject("files_id", id);
            int lastNumber = Integer.parseInt(lastChunk.get("n").toString());
            newChunk.put("n", lastNumber + 1);
            file.put("lastModify", System.currentTimeMillis());
            if(dataLen < chunkSize){
                newChunk.put("data", new Binary(data));
                chunksColl.insert(newChunk);
                file.put("length", length + dataLen);
                filesColl.save(file);
            }else{
                byte[] newData = Arrays.copyOfRange(data, 0, chunkSize);
                newChunk.put("data", new Binary(newData));
                chunksColl.insert(newChunk);
                file.put("length", length + chunkSize);
                filesColl.save(file);
                byte[] remainderData = Arrays.copyOfRange(data, chunkSize, dataLen);
                append(remainderData);
            }
        }
        else{
            //need to update the last chunk
            byte[] lastData = (byte[])lastChunk.get("data");
            int lastDataLen = lastData.length;
            int diff = (int) (chunkSize - (length % chunkSize));
            file.put("lastModify", System.currentTimeMillis());
            if(dataLen < diff){
                byte[] newData = new byte[lastDataLen + dataLen];
                System.arraycopy(lastData, 0, newData, 0, lastDataLen);
                System.arraycopy(data, 0, newData, lastDataLen, dataLen);
                lastChunk.put("data", new Binary(newData));
                chunksColl.save(lastChunk);
                file.put("length", length + dataLen);
                filesColl.save(file);
            }else{
                byte[] newData = new byte[chunkSize];
                System.arraycopy(lastData, 0, newData, 0, lastDataLen);
                System.arraycopy(data, 0, newData, lastDataLen, diff);
                lastChunk.put("data", new Binary(newData));
                chunksColl.save(lastChunk);
                file.put("length", length + diff);
                filesColl.save(file);
                byte[] remainderData = Arrays.copyOfRange(data, diff, dataLen);
                append(remainderData);
            }
        }
    }
    
    private void update(byte[] data, long position){
        DBObject file = filesColl.findOne(dbo);
        long length = Long.parseLong(file.get("length").toString());
        int chunksMax = (int)(length / chunkSize);
        if(length % chunkSize == 0){
            chunksMax--;
        }
        int n = (int)(position / chunkSize);
        int remainder = (int)(position % chunkSize);
        int dataLen = data.length;
        ObjectId id = (ObjectId)file.get("_id");
        DBObject query = new BasicDBObject("files_id", id);
        query.put("n", n);
        DBObject curChunk = chunksColl.findOne(query);
        byte[] curData = (byte[])curChunk.get("data");
        int curLen = curData.length;
        file.put("lastModify", System.currentTimeMillis());
        if(remainder + dataLen <= chunkSize){
            if(remainder + dataLen <= curLen){
                System.arraycopy(data, 0, curData, remainder, dataLen);
                curChunk.put("data", new Binary(curData));
                chunksColl.save(curChunk);
                filesColl.save(file);
            }else{
                byte[] newData = new byte[remainder + dataLen];
                System.arraycopy(curData, 0, newData, 0, remainder);
                System.arraycopy(data, 0, newData, remainder, dataLen);
                curChunk.put("data", new Binary(newData));
                chunksColl.save(curChunk);
                file.put("length", length + (remainder + dataLen - curLen));
                filesColl.save(file);
            }
        }
        else{
            if(n < chunksMax){
                int diff = chunkSize - remainder;
                System.arraycopy(data, 0, curData, remainder, diff);
                curChunk.put("data", new Binary(curData));
                chunksColl.save(curChunk);
                byte[] remainderData = Arrays.copyOfRange(data, diff, dataLen);
                update(remainderData, position + diff);
            }else{
                int diff = chunkSize - remainder;
                byte[] newData = new byte[chunkSize];
                System.arraycopy(curData, 0, newData, 0, remainder);
                System.arraycopy(data, 0, newData, remainder, diff);
                curChunk.put("data", new Binary(newData));
                chunksColl.save(curChunk);
                file.put("length", length + (chunkSize - curLen));
                filesColl.save(file);
                byte[] remainderData = Arrays.copyOfRange(data, diff, dataLen);
                append(remainderData);
            }
        }
    }
    
    public byte[] getChunkData(long position){
        DBObject file = filesColl.findOne(dbo);
        int n = (int)(position / chunkSize);
        ObjectId id = (ObjectId)file.get("_id");
        DBObject query = new BasicDBObject("files_id", id);
        query.put("n", n);
        DBObject curChunk = chunksColl.findOne(query);
        return (byte[])curChunk.get("data");
    }
    
    public int getPositionInChunk(long position){
        return (int)(position % chunkSize);
    }
    
    public long getLength(){
        return fs.findOne(dbo).getLength();
    }
    
    public void delete(){
        fs.remove(dbo);
    }
    
    public long getLastModify(){
        GridFSDBFile f = fs.findOne(dbo);
        return Long.parseLong(f.get("lastModify").toString());
    }
    
}
