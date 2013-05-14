/*
 * Copyright (c) www.bugull.com
 * 
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
import com.bugull.mongo.lucene.cluster.ClusterConfig;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;

/**
 * Set the lucene index attributes.
 * 
 * <p>Singleton Pattern is used here. An application should use only one BuguIndex.</p>
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguIndex {
    
    private final static Logger logger = Logger.getLogger(BuguIndex.class);
    
    private static BuguIndex instance;
    
    private double bufferSizeMB = 16;
    private Version version = Version.LUCENE_35;
    private Analyzer analyzer = new StandardAnalyzer(version);
    
    private String directoryPath;
    private ClusterConfig clusterConfig;  //used in clustering environment
    
    private ExecutorService executor;  //thread pool to maintain index writing 
    private int threadPoolSize = 10;  //default thread pool size is 10
    
    private ScheduledExecutorService scheduler;  //scheduler to reopen index
    private long period = 30L * 1000L;  //in default, reopen index per 30 seconds
    
    private boolean reopening = false;
    private boolean rebuilding = false;
    
    private BuguIndex(){
        
    }
    
    public static BuguIndex getInstance(){
        if(instance == null){
            instance = new BuguIndex();
        }
        return instance;
    }
    
    public void open(){
        executor = Executors.newFixedThreadPool(threadPoolSize);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new IndexReopenTask(), period, period, TimeUnit.MILLISECONDS);
        if(clusterConfig != null){
            clusterConfig.validate();
        }
    }
    
    public void close(){
        try{
            if(executor != null){
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
            if(scheduler != null){
                scheduler.shutdown();
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            }
            if(clusterConfig != null){
                clusterConfig.invalidate();
            }
        }catch(InterruptedException ex){
            logger.error(ex.getMessage(), ex);
        }
        
        Map<String, IndexWriter>  map = IndexWriterCache.getInstance().getAll();
        for(IndexWriter writer : map.values()){
            if(writer != null){
                Directory dir = writer.getDirectory();
                try{
                    writer.commit();
                    writer.close(true);
                }catch(CorruptIndexException ex){
                    logger.error("Can not commit and close the lucene index", ex);
                }catch(IOException ex){
                    logger.error("Can not commit and close the lucene index", ex);
                }finally{
                    try{
                        if(dir != null && IndexWriter.isLocked(dir)){
                            IndexWriter.unlock(dir);
                        }
                    }catch(IOException ex){
                        logger.error("Can not unlock the lucene index", ex);
                    }
                }
            }
        }
    }
    
    public ExecutorService getExecutor(){
        return executor;
    }
    
    public void setThreadPoolSize(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
    }

    public double getBufferSizeMB() {
        return bufferSizeMB;
    }

    public void setBufferSizeMB(double bufferSizeMB) {
        this.bufferSizeMB = bufferSizeMB;
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

    public boolean isRebuilding() {
        return rebuilding;
    }

    public void setRebuilding(boolean rebuilding) {
        this.rebuilding = rebuilding;
    }

    public ClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(ClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
    }
    
}
