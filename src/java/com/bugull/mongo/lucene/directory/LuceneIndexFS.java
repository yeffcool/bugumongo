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
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.mapper.Operator;
import com.mongodb.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class LuceneIndexFS {
    
    private final static Logger logger = Logger.getLogger(LuceneIndexFS.class);
    
    public final static int CHUNK_SIZE = 256 * 1024;  //256K
    
    //for files
    public final static String DIR_NAME = "dirName";
    public final static String FILENAME = "filename";
    public final static String LENGTH = "length";
    public final static String LAST_MODIFY = "lastModify";
    
    //for chunks
    public final static String FILE_ID = "fileId";
    public final static String CHUNK_NUMBER = "chunkNumber";
    public final static String DATA = "data";
    
    private static LuceneIndexFS instance;
    
    private DBCollection files;
    private DBCollection chunks;
    
    private LuceneIndexFS(){
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error("Can not get database instance! Please ensure connected to mongoDB correctly.", ex);
        }
        files = db.getCollection("lucene.files");
        chunks = db.getCollection("lucene.chunks");
    }
    
    public static LuceneIndexFS getInstance(){
        if(instance == null){
            instance = new LuceneIndexFS();
        }
        return instance;
    }
    
    public void saveFile(MongoFile f){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, f.getId());
        dbo.put(DIR_NAME, f.getDirectory().getDirectoryName());
        dbo.put(FILENAME, f.getFilename());
        dbo.put(LENGTH, f.getFileLength());
        dbo.put(LAST_MODIFY, f.getLastModify());
        files.save(dbo);
        Map<Integer, MongoChunk> map = f.getChunks();
        for(Entry<Integer, MongoChunk> entry : map.entrySet()){
            MongoChunk mc = entry.getValue();
            if(mc.isDirty()){
                dbo = new BasicDBObject();
                dbo.put(Operator.ID, mc.getId());
                dbo.put(FILE_ID, f.getId().toString());
                dbo.put(CHUNK_NUMBER, mc.getChunkNumber());
                dbo.put(DATA, mc.getData());
                chunks.save(dbo);
            }
        }
    }
    
    public void createEmptyFile(MongoFile f){
        DBObject dbo = new BasicDBObject();
        //dbo.put(DIR_NAME, dbo)
        dbo.put(DIR_NAME, f.getDirectory().getDirectoryName());
        dbo.put(FILENAME, f.getFilename());
        dbo.put(LENGTH, 0L);
        dbo.put(LAST_MODIFY, System.currentTimeMillis());
        files.insert(dbo);
        f.setId((ObjectId)dbo.get(Operator.ID));
    }
    
    public DBObject findFile(String directoryName, String filename){
        DBObject query = new BasicDBObject();
        query.put(DIR_NAME, directoryName);
        query.put(FILENAME, filename);
        return files.findOne(query);
    }
    
    public void removeFile(String directoryName, String filename){
        //remove file
        DBObject query = new BasicDBObject();
        query.put(DIR_NAME, directoryName);
        query.put(FILENAME, filename);
        DBObject f = files.findOne(query);
        String fId = f.get(Operator.ID).toString();
        files.remove(query);
        //remove chunks
        query = new BasicDBObject();
        query.put(FILE_ID, fId);
        chunks.remove(query);
    }
    
    public List<String> getFileList(String directoryName){
        List<String> list = new ArrayList<String>();
        DBObject query = new BasicDBObject();
        query.put(DIR_NAME, directoryName);
        DBCursor cursor = files.find(query);
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            list.add((String)dbo.get(FILENAME));
        }
        cursor.close();
        return list;
    }
    
    public Map<Integer, MongoChunk> getChunks(MongoFile f){
        Map<Integer, MongoChunk> map = new ConcurrentHashMap<Integer, MongoChunk>();
        DBObject query = new BasicDBObject();
        query.put(FILE_ID, f.getId());
        DBCursor cursor  = chunks.find(query);
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            ObjectId chunkId = (ObjectId)dbo.get(Operator.ID);
            Integer chunkNumber = (Integer)dbo.get(CHUNK_NUMBER);
            byte[] data = (byte[])dbo.get(DATA);
            MongoChunk chunk = new MongoChunk(f, chunkId, chunkNumber, data);
            map.put(chunkNumber, chunk);
        }
        cursor.close();
        return map;
    }

}
