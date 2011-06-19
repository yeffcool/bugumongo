package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexRemoveThread implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexRemoveThread.class);
    
    private Class<?> clazz;
    private String id;
    
    public IndexRemoveThread(Class<?> clazz, String id){
        this.clazz = clazz;
        this.id = id;
    }

    @Override
    public void run() {
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        IndexWriterCache cache = IndexWriterCache.getInstance();
        IndexWriter writer = cache.get(name);
        try{
            Term term = new Term(FieldsCache.getInstance().getIdFieldName(clazz), id);
            writer.deleteDocuments(term);
            cache.putLastChange(name, System.currentTimeMillis());
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}
