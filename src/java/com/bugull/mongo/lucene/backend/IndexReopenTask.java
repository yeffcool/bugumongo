package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.cache.IndexSearcherCache;
import java.util.Map;
import java.util.TimerTask;
import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexReopenTask extends TimerTask {
    
    private final static Logger logger = Logger.getLogger(IndexReopenTask.class);

    @Override
    public void run() {
        Map<String, IndexSearcher> map = IndexSearcherCache.getInstance().getAll();
        for(String name : map.keySet()){
            try{
                IndexReopenThread thread = new IndexReopenThread(name, map.get(name));
                new Thread(thread).start();
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
}
