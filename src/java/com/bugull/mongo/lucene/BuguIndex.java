package com.bugull.mongo.lucene;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.IndexSearcherCache;
import com.bugull.mongo.lucene.backend.IndexReopenTask;
import java.util.Timer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguIndex {
    
    private static BuguIndex instance;
    
    private Timer timer;
    
    private Analyzer analyzer;
    private int directoryType;
    private String directoryPath;
    
    private BuguIndex(){
        
    }
    
    public static BuguIndex getInstance(){
        if(instance == null){
            instance = new BuguIndex();
        }
        return instance;
    }
    
    public void startIndexReopenTask(long period){
        cancelIndexReopenTask();
        timer = new Timer();
        timer.schedule(new IndexReopenTask(), period, period);
    }
    
    public void cancelIndexReopenTask(){
        if(timer != null){
            timer.cancel();
        }
    }
    
    public IndexSearcher getIndexSearcher(Class<?> clazz){
        Entity entity = clazz.getAnnotation(Entity.class);
        return IndexSearcherCache.getInstance().get(entity.name());
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public int getDirectoryType() {
        return directoryType;
    }

    public void setDirectoryType(int directoryType) {
        this.directoryType = directoryType;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
    
}
