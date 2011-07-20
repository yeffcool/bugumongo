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

package com.bugull.mongo.fs;

import com.bugull.mongo.BuguConnection;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.File;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguFS {
    
    private final static Logger logger = Logger.getLogger(BuguFS.class);
    
    private static BuguFS instance = new BuguFS();
    
    private GridFS fs;
    
    private BuguFS(){
        fs = new GridFS(BuguConnection.getInstance().getDB());
    }
    
    public static BuguFS getInstance(){
        return instance;
    }
    
    public GridFS getFS(){
        return fs;
    }
    
    public void save(File file){
        save(file, file.getName());
    }
    
    public void save(File file, String filename){
        save(file, filename, null);
    }
    
    public void save(File file, String filename, Map params){
        GridFSInputFile f = null;
        try{
            f = fs.createFile(file);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        f.setFilename(filename.toLowerCase());
        if(params != null){
            f.putAll(params);
        }
        f.save();
    }
    
    public void save(byte[] data, String filename){
        save(data, filename);
    }
    
    public void save(byte[] data, String filename, Map params){
        GridFSInputFile f = fs.createFile(data);
        f.setFilename(filename.toLowerCase());
        if(params != null){
            f.putAll(params);
        }
        f.save();
    }
    
    public GridFSDBFile findOne(String filename){
        return fs.findOne(filename.toLowerCase());
    }
    
    public GridFSDBFile findOne(DBObject query){
        return fs.findOne(query);
    }
    
    public void remove(String filename){
        fs.remove(filename.toLowerCase());
    }
    
    public void remove(DBObject query){
        fs.remove(query);
    }
    
}
