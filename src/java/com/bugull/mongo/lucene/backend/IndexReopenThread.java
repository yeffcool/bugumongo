package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.cache.IndexSearcherCache;
import com.bugull.mongo.cache.IndexWriterCache;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexReopenThread implements Runnable{
    
    private final static Logger logger = Logger.getLogger(IndexReopenThread.class);
    
    private String name;
    private IndexSearcher searcher;
    
    public IndexReopenThread(String name, IndexSearcher searcher){
        this.name = name;
        this.searcher = searcher;
    }

    @Override
    public void run() {
        IndexSearcherCache searcherCache = IndexSearcherCache.getInstance();
        if(searcherCache.isOpenning(name)){
            return;
        }
        searcherCache.putOpenning(name, Boolean.TRUE);
        try{
            IndexWriterCache writerCache = IndexWriterCache.getInstance();
            long lastChange = writerCache.getLastChange(name);
            long lastOpen = searcherCache.getLastOpen(name);
            if(lastChange > lastOpen){
                IndexReader newReader = searcher.getIndexReader().reopen();
                IndexSearcher newSearcher = new IndexSearcher(newReader);
                searcherCache.put(name, newSearcher);
                searcherCache.putLastOpen(name, System.currentTimeMillis());
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }finally{
            searcherCache.putOpenning(name, Boolean.FALSE);
        }
    }
    
}
