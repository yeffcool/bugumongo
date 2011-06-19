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
public class IndexUpdateThread implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexUpdateThread.class);
    
    private BuguEntity obj;
    
    public IndexUpdateThread(BuguEntity obj){
        this.obj = obj;
    }

    @Override
    public void run() {
        Class<?> clazz = obj.getClass();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        IndexWriterCache cache = IndexWriterCache.getInstance();
        IndexWriter writer = cache.get(name);
        Document doc = new Document();
        IndexCreater creater = new IndexCreater(obj, obj.getId(), null);
        creater.process(doc);
        try{
            Term term = new Term(FieldsCache.getInstance().getIdFieldName(clazz), obj.getId());
            writer.updateDocument(term, doc);
            cache.putLastChange(name, System.currentTimeMillis());
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}
