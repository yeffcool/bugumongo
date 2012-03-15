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

package com.bugull.mongo.lucene;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.cache.IndexSearcherCache;
import com.bugull.mongo.mapper.FieldUtil;
import com.bugull.mongo.mapper.MapperUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguSearcher<T> {
    
    private final static Logger logger = Logger.getLogger(BuguSearcher.class);
    
    private Class<T> clazz;
    private IndexSearcher searcher;
    private IndexReader reader;
    
    private Query query;
    private Sort sort;
    private Filter filter;
    private int pageNumber = 1;
    private int pageSize = 20;
    private int maxPage = 50;
    private int resultCount;
    private BuguHighlighter highlighter;
    
    public BuguSearcher(Class<T> clazz){
        this.clazz = clazz;
        String name = MapperUtil.getEntityName(clazz);
        searcher = IndexSearcherCache.getInstance().get(name);
        reader = searcher.getIndexReader();
        reader.incRef();
    }
    
    public BuguSearcher<T> setQuery(Query query){
        this.query = query;
        return this;
    }
    
    public BuguSearcher<T> setSort(Sort sort){
        this.sort = sort;
        return this;
    }
    
    public BuguSearcher<T> setFilter(Filter filter){
        this.filter = filter;
        return this;
    }
    
    public BuguSearcher<T> setMaxPage(int maxPage){
        this.maxPage = maxPage;
        return this;
    }
    
    public BuguSearcher<T> setPageNumber(int pageNumber){
        this.pageNumber = pageNumber;
        return this;
    }
    
    public BuguSearcher<T> setPageSize(int pageSize){
        this.pageSize = pageSize;
        return this;
    }
    
    public BuguSearcher<T> setHighlighter(BuguHighlighter highlighter) {
        this.highlighter = highlighter;
        return this;
    }
    
    public int getResultCount(){
        return resultCount;
    }
    
    public List<T> search(Query query){
        this.query = query;
        return search();
    }
    
    public List<T> search(Query query, Sort sort){
        this.query = query;
        this.sort = sort;
        return search();
    }
    
    public List<T> search(Query query, Filter filter){
        this.query = query;
        this.filter = filter;
        return search();
    }
    
    public List<T> search(Query query, Filter filter, Sort sort){
        this.query = query;
        this.filter = filter;
        this.sort = sort;
        return search();
    }
    
    public List<T> search(){
        TopDocs topDocs = null;
        try{
            if(sort == null){
                topDocs = searcher.search(query, filter, maxPage*pageSize);
            }else{
                topDocs = searcher.search(query, filter, maxPage*pageSize, sort);
            }
        }catch(IOException ex){
            logger.error("Something is wrong when doing lucene search", ex);
        }
        if(topDocs == null){
            return Collections.emptyList();
        }
        resultCount = topDocs.totalHits;
        ScoreDoc[] docs = topDocs.scoreDocs;
        List<T> list = new ArrayList<T>();
        BuguDao<T> dao = DaoCache.getInstance().get(clazz);
        int begin = (pageNumber - 1) * pageSize;
        int end = begin + pageSize;
        if(end > resultCount){
            end = resultCount;
        }
        for(int i=begin; i<end; i++){
            Document doc = null;
            try{
                doc = searcher.doc(docs[i].doc);
            }catch(IOException ex){
                logger.error("Lucene IndexSearcher can not get the document", ex);
            }
            if(doc != null){
                String id = doc.get(FieldsCache.getInstance().getIdFieldName(clazz));
                list.add(dao.findOne(id));
            }
        }
        //process highlighter
        if(highlighter != null){
            for(Object obj : list){
                String[] fields = highlighter.getFields();
                for(String fieldName : fields){
                    if(! fieldName.contains(".")){
                        Field field = FieldsCache.getInstance().getField(clazz, fieldName);
                        String fieldValue = FieldUtil.get(obj, field).toString();
                        String result = null;
                        try{
                            result = highlighter.getResult(fieldName, fieldValue);
                        }catch(Exception ex){
                            logger.error("Something is wrong when getting the highlighter result", ex);
                        }
                        if(result!=null && !result.equals("")){
                            FieldUtil.set(obj, field, result);
                        }
                    }
                }
            }
        }
        return list;
    }
    
    public void close(){
        try{
            reader.decRef();
        }catch(IOException ex){
            logger.error("Something is wrong when decrease the reference of IndexReader", ex);
        }
    }
    
    public IndexSearcher getSearcher(){
        return searcher;
    }

}
