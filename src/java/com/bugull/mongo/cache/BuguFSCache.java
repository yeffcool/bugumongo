/*
 * Copyright (c) www.bugull.com
 * 
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
package com.bugull.mongo.cache;

import com.bugull.mongo.fs.BuguFS;
import com.mongodb.gridfs.GridFS;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Cache(Map) contains BuguFS instance.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguFSCache {
    
    private static BuguFSCache instance = new BuguFSCache();
    
    private final ConcurrentMap<String, BuguFS> cache = new ConcurrentHashMap<String, BuguFS>();
    
    private BuguFSCache(){
        
    }
    
    public static BuguFSCache getInstance(){
        return instance;
    }
    
    public BuguFS get(){
        return get(GridFS.DEFAULT_BUCKET, GridFS.DEFAULT_CHUNKSIZE);
    }
    
    public BuguFS get(String bucketName){
        return get(bucketName, GridFS.DEFAULT_CHUNKSIZE);
    }
    
    public BuguFS get(long chunkSize){
        return get(GridFS.DEFAULT_BUCKET, chunkSize);
    }
    
    public BuguFS get(String bucketName, long chunkSize){
        BuguFS fs = cache.get(bucketName);
        if(fs != null){
            return fs;
        }
        fs = new BuguFS(bucketName, chunkSize);
        BuguFS temp = cache.putIfAbsent(bucketName, fs);
        if(temp != null){
            return temp;
        }else{
            return fs;
        }
    }
    
}
