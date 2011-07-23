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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.cache.IndexSearcherCache;
import com.bugull.mongo.cache.IndexWriterCache;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexReopenTask implements Runnable {
    
    private final static Logger logger = Logger.getLogger(IndexReopenTask.class);

    @Override
    public void run() {
        Map<String, IndexSearcher> map = IndexSearcherCache.getInstance().getAll();
        for(String name : map.keySet()){
            IndexSearcher searcher = map.get(name);
            IndexSearcherCache searcherCache = IndexSearcherCache.getInstance();
            if(searcherCache.isOpenning(name)){
                continue;
            }
            searcherCache.putOpenning(name, Boolean.TRUE);
            try{
                IndexWriterCache writerCache = IndexWriterCache.getInstance();
                long lastChange = writerCache.getLastChange(name);
                long lastOpen = searcherCache.getLastOpen(name);
                if(lastChange > lastOpen){
                    IndexWriter writer = writerCache.get(name);
                    writer.optimize();
                    writer.commit();
                    IndexReader newReader = searcher.getIndexReader().reopen();
                    IndexSearcher newSearcher = new IndexSearcher(newReader);
                    searcherCache.put(name, newSearcher);
                    searcherCache.putLastOpen(name, System.currentTimeMillis());
                }
            }catch(Exception e){
                logger.error(e.getMessage());
            }finally{
                searcherCache.putOpenning(name, Boolean.FALSE);
            }
        }
    }
    
}
