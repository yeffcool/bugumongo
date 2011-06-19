package com.bugull.mongo.lucene;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
import com.bugull.mongo.lucene.backend.IndexCreater;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexRebuilder implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexRebuilder.class);
    
    private Class<?> clazz;
    private IndexWriter writer;
    private int batchSize = 200;
    
    public IndexRebuilder(Class<?> clazz){
        this.clazz = clazz;
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        IndexWriterCache cache = IndexWriterCache.getInstance();
        writer = cache.get(name);
    }
    
    public IndexRebuilder(Class<?> clazz, int batchSize){
        this(clazz);
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        BuguDao dao = new BuguDao(clazz);
        long count = dao.count();
        int pages = (int) (count / batchSize);
        int remainder = (int) (count % batchSize);
        if(pages > 0){
            for(int i=1; i<=pages; i++){
                List list = dao.findAll(pages, batchSize);
                process(list);
            }
        }
        if(remainder > 0){
            pages++;
            List list = dao.findAll(pages, remainder);
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
