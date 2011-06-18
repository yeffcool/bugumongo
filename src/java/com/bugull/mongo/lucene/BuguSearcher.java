package com.bugull.mongo.lucene;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexSearcherCache;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguSearcher {
    
    private final static Logger logger = Logger.getLogger(BuguSearcher.class);
    
    private Class<?> clazz;
    private IndexSearcher searcher;
    private Query query;
    private Sort sort;
    private int pageNumber = 1;
    private int pageSize = 20;
    private int maxPage = 50;
    private int resultCount;
    
    public BuguSearcher(Class<?> clazz){
        this.clazz = clazz;
        Entity entity = clazz.getAnnotation(Entity.class);
        searcher = IndexSearcherCache.getInstance().get(entity.name());
    }
    
    public void setQuery(Query query){
        this.query = query;
    }
    
    public void setSort(Sort sort){
        this.sort = sort;
    }
    
    public void setMaxPage(int maxPage){
        this.maxPage = maxPage;
    }
    
    public void setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
    }
    
    public void setPageSize(int pageSize){
        this.pageSize = pageSize;
    }
    
    public int getResultCount(){
        return resultCount;
    }
    
    public List search(){
        TopDocs topDocs = null;
        try{
            if(sort == null){
                topDocs = searcher.search(query, maxPage*pageSize);
            }else{
                topDocs = searcher.search(query, maxPage*pageSize, sort);
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        if(topDocs == null){
            return Collections.emptyList();
        }
        resultCount = topDocs.totalHits;
        ScoreDoc[] docs = topDocs.scoreDocs;
        List list = new LinkedList();
        BuguDao dao = new BuguDao(clazz);
        int begin = (pageNumber - 1) * pageSize;
        int end = begin + pageSize;
        if(end > resultCount){
            end = resultCount;
        }
        for(int i=begin; i<end; i++){
            try{
                Document doc = searcher.doc(docs[i].doc);
                String id = doc.get(FieldsCache.getInstance().getIdFieldName(clazz));
                list.add(dao.findOne(id));
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        return list;
    }
    
}
