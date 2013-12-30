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

package com.bugull.mongo.lucene.cache;

import com.bugull.mongo.lucene.BuguIndex;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Cache(Map) contains IndexWriter.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexWriterCache {
    
    private final static Logger logger = Logger.getLogger(IndexWriterCache.class);
    
    private static IndexWriterCache instance = new IndexWriterCache();
    
    private final ConcurrentMap<String, IndexWriter> cache = new ConcurrentHashMap<String, IndexWriter>();
    
    private IndexWriterCache(){
        
    }
    
    public static IndexWriterCache getInstance(){
        return instance;
    }
    
    public IndexWriter get(String name){
        IndexWriter writer = cache.get(name);
        if(writer != null){
            return writer;
        }
        
        synchronized(this){
            writer = cache.get(name);
            if(writer == null){
                BuguIndex index = BuguIndex.getInstance();
                IndexWriterConfig conf = new IndexWriterConfig(index.getVersion(), index.getAnalyzer());
                double bufferSizeMB = index.getBufferSizeMB();
                conf.setRAMBufferSizeMB(bufferSizeMB);
                try{
                    String path = index.getDirectoryPath();
                    Directory directory = FSDirectory.open(new File(path + "/" + name));
                    writer = new IndexWriter(directory, conf);
                }catch(IOException ex){
                    logger.error("Something is wrong when create IndexWriter for " + name, ex);
                }
                cache.put(name, writer);
            }
        }
        return writer;
    }
    
    public Map<String, IndexWriter> getAll(){
        return cache;
    }
    
}
