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

package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
import com.bugull.mongo.utils.MapperUtil;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexRemoveTask implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexRemoveTask.class);
    
    private Class<?> clazz;
    private String id;
    
    public IndexRemoveTask(Class<?> clazz, String id){
        this.clazz = clazz;
        this.id = id;
    }

    @Override
    public void run() {
        String name = MapperUtil.getEntityName(clazz);
        IndexWriterCache cache = IndexWriterCache.getInstance();
        IndexWriter writer = cache.get(name);
        Term term = new Term(FieldsCache.getInstance().getIdFieldName(clazz), id);
        try {
            writer.deleteDocuments(term);
        } catch (CorruptIndexException ex) {
            logger.error("IndexWriter can not delete a document from the lucene index", ex);
        } catch (IOException ex) {
            logger.error("IndexWriter can not delete a document from the lucene index", ex);
        }
    }
    
}
