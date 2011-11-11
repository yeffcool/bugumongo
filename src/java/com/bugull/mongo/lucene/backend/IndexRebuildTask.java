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

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
import com.bugull.mongo.mapper.MapperUtil;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexRebuildTask implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexRebuildTask.class);
    
    private Class<?> clazz;
    private IndexWriter writer;
    private int batchSize;
    
    public IndexRebuildTask(Class<?> clazz, int batchSize){
        this.clazz = clazz;
        this.batchSize = batchSize;
        String name = MapperUtil.getEntityName(clazz);
        IndexWriterCache cache = IndexWriterCache.getInstance();
        writer = cache.get(name);
    }

    @Override
    public void run() {
        BuguDao dao = DaoCache.getInstance().get(clazz);
        long count = dao.count();
        int pages = (int) (count / batchSize);
        int remainder = (int) (count % batchSize);
        if(pages > 0){
            for(int i=1; i<=pages; i++){
                List list = dao.findForLucene(i, batchSize);
                process(list);
            }
        }
        if(remainder > 0){
            List list = dao.findForLucene(++pages, remainder);
            process(list);
        }
    }
    
    private void process(List list){
        for(Object o : list){
            BuguEntity obj = (BuguEntity)o;
            process(obj);
        }
        try{
            writer.optimize();
            writer.commit();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private void process(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            Document doc = new Document();
            IndexCreator creator = new IndexCreator(obj, "");
            creator.create(doc);
            try{
                Term term = new Term(FieldsCache.getInstance().getIdFieldName(clazz), obj.getId());
                writer.updateDocument(term, doc);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
}
