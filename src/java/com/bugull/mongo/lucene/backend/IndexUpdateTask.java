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

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexUpdateTask implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexUpdateTask.class);
    
    private BuguEntity obj;
    
    public IndexUpdateTask(BuguEntity obj){
        this.obj = obj;
    }

    @Override
    public void run() {
        Class<?> clazz = obj.getClass();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        if(name.equals("")){
            name = clazz.getSimpleName().toLowerCase();
        }
        IndexWriterCache cache = IndexWriterCache.getInstance();
        IndexWriter writer = cache.get(name);
        Document doc = new Document();
        IndexCreater creater = new IndexCreater(obj, obj.getId(), null);
        creater.process(doc);
        try{
            Term term = new Term(FieldsCache.getInstance().getIdFieldName(clazz), obj.getId());
            writer.updateDocument(term, doc);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}
