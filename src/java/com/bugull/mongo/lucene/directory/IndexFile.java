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
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexFile {
    
    private final static Logger logger = Logger.getLogger(IndexFile.class);
    
    private String dirName;
    private String fileName;
    private GridFS fs;
    private DBObject dbo;
    
    private DBCollection filesColl;
    private DBCollection chunksColl;
    
    public IndexFile(String dirName, String fileName){
        this.dirName = dirName;
        this.fileName = fileName;
        fs = BuguFS.getInstance().getFS();
        dbo = new BasicDBObject();
        dbo.put("dirName", dirName);
        dbo.put("fileName", fileName);
        DB db = BuguConnection.getInstance().getDB();
        filesColl = db.getCollection("fs.files");
        chunksColl = db.getCollection("fs.chunks");
    }
    
    public boolean exists(){
        if(fs.findOne(dbo) != null){
            return true;
        }else{
            return false;
        }
    }
    
    private void create(byte[] data){
        GridFSInputFile f = fs.createFile(data);
        f.put("dirName", dirName);
        f.put("fileName", fileName);
        f.put("lastModify", System.currentTimeMillis());
        f.save();
    }
    
    public void write(byte[] data, long position){
        if(data==null || data.length==0){
            return;
        }
        if(!exists()){
            create(data);
        }else if(position >= getLength()){
            append(data);
        }else{
            update(data, position);
        }
    }
    
    private void append(byte[] data){
        DBObject file = filesColl.findOne(dbo);
        ObjectId id = (ObjectId)file.get("_id");
        DBObject query = new BasicDBObject("files_id", id);
        DBObject orderBy = new BasicDBObject("n", -1);
        DBCursor cursor = chunksColl.find(query).sort(orderBy).limit(1);
        if(!cursor.hasNext()){
            return;
        }
        DBObject lastChunk = cursor.next();
        int dataLen = data.length;
        int chunkSize = Integer.parseInt(file.get("chunkSize").toString());
        long length = Long.parseLong(file.get("length").toString());
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
        }else{
            //need to update the last chunk
            Object o = lastChunk.get("data");
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
        
    }
    
    public long getLength(){
        return fs.findOne(dbo).getLength();
    }
    
    public ByteArrayOutputStream getOutputStream(){
        if(!exists()){
            return null;
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try{
            fs.findOne(dbo).writeTo(os);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return os;
    }
    
    public void delete(){
        fs.remove(dbo);
    }
    
    public long getLastModify(){
        GridFSDBFile f = fs.findOne(dbo);
        return Long.parseLong(f.get("lastModify").toString());
    }
    
}
