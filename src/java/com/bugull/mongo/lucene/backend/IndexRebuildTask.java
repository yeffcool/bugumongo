package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexWriterCache;
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
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        if(name.equals("")){
            name = clazz.getSimpleName().toLowerCase();
        }
        IndexWriterCache cache = IndexWriterCache.getInstance();
        writer = cache.get(name);
    }

    @Override
    public void run() {
        BuguDao dao = new BuguDao(clazz);
        long count = dao.count();
        int pages = (int) (count / batchSize);
        int remainder = (int) (count % batchSize);
        if(pages > 0){
            for(int i=1; i<=pages; i++){
                List list = dao.findAll(i, batchSize);
                process(list);
            }
        }
        if(remainder > 0){
            List list = dao.findAll(++pages, remainder);
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
    
}
