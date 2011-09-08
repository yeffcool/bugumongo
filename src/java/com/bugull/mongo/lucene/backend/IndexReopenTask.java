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
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexReopenTask implements Runnable {
    
    private final static Logger logger = Logger.getLogger(IndexReopenTask.class);

    @Override
    public void run() {
        IndexSearcherCache searcherCache = IndexSearcherCache.getInstance();
        Map<String, IndexSearcher> map = searcherCache.getAll();
        for(String name : map.keySet()){
            if(searcherCache.isOpenning(name)){
                continue;
            }
            searcherCache.putOpenning(name, Boolean.TRUE);
            IndexSearcher searcher = map.get(name);
            IndexReader reader = searcher.getIndexReader();
            IndexReader newReader = null;
            try{
                newReader = reader.reopen();
            }catch(Exception e){
                logger.error(e.getMessage());
            }
            if(newReader!=null && newReader!=reader){
                try{
                    reader.decRef();
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
                IndexSearcher newSearcher = new IndexSearcher(newReader);
                searcherCache.put(name, newSearcher);
            }
            searcherCache.putOpenning(name, Boolean.FALSE);
        }
    }
    
}
