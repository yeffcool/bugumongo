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

import com.bugull.mongo.cache.IndexWriterCache;
import com.bugull.mongo.lucene.backend.IndexReopenTask;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguIndex {
    
    private final static Logger logger = Logger.getLogger(BuguIndex.class);
    
    private static BuguIndex instance = new BuguIndex();
    
    private Version version = Version.LUCENE_34;
    private Analyzer analyzer = new StandardAnalyzer(version);
    private String directoryPath;
    
    private ExecutorService executor;
    private int poolSize = 10;
    
    private ScheduledExecutorService scheduler;
    private long period = 60L * 1000L;  //one minute
    
    private boolean reopening = false;
    
    private BuguIndex(){
        
    }
    
    public static BuguIndex getInstance(){
        return instance;
    }
    
    public void open(){
        executor = Executors.newFixedThreadPool(poolSize);
        scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(new IndexReopenTask(), period, period, TimeUnit.MILLISECONDS);
    }
    
    public void close(){
        if(executor != null){
            executor.shutdown();
        }
        if(scheduler != null){
            scheduler.shutdown();
        }
        Map<String, IndexWriter>  map = IndexWriterCache.getInstance().getAll();
        for(IndexWriter writer : map.values()){
            try{
                writer.commit();
                writer.close(true);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    public ExecutorService getExecutor(){
        return executor;
    }
    
    public void setThreadPoolSize(int poolSize){
        this.poolSize = poolSize;
    }
    
    public void setIndexReopenPeriod(long period){
        this.period = period;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public boolean isReopening() {
        return reopening;
    }

    public void setReopening(boolean reopening) {
        this.reopening = reopening;
    }
    
}
