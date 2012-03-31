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

package com.bugull.mongo.cache;

import com.bugull.mongo.lucene.BuguIndex;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    
    private Map<String, IndexWriter> cache;
    
    private IndexWriterCache(){
        cache = new ConcurrentHashMap<String, IndexWriter>();
    }
    
    public static IndexWriterCache getInstance(){
        return instance;
    }
    
    public IndexWriter get(String name){
        IndexWriter writer = null;
        if(cache.containsKey(name)){
            writer = cache.get(name);
        }else{
            synchronized(this){
                if(cache.containsKey(name)){
                    writer = cache.get(name);
                }else{
                    BuguIndex index = BuguIndex.getInstance();
                    String path = index.getDirectoryPath();
                    try{
                        Directory dir = FSDirectory.open(new File(path + "/" + name));
                        IndexWriterConfig conf = new IndexWriterConfig(index.getVersion(), index.getAnalyzer());
                        double bufferSizeMB = index.getBufferSizeMB();
                        if(bufferSizeMB > 0.0){
                            conf.setRAMBufferSizeMB(bufferSizeMB);
                        }
                        writer = new IndexWriter(dir, conf);
                    }catch(Exception ex){
                        logger.error("Something is wrong when create IndexWriter for " + name, ex);
                    }
                    cache.put(name, writer);
                }
            }
        }
        return writer;
    }
    
    public Map<String, IndexWriter> getAll(){
        return cache;
    }
    
}
