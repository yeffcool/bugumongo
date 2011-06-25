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

import com.bugull.mongo.lucene.backend.IndexReopenTask;
import com.bugull.mongo.lucene.directory.DirectoryType;
import java.util.Timer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguIndex {
    
    private static BuguIndex instance = new BuguIndex();
    
    private Timer timer;
    
    private Version version = Version.LUCENE_32;
    private Analyzer analyzer = new StandardAnalyzer(version);
    private DirectoryType directoryType = DirectoryType.DB;
    private String directoryPath;
    
    private BuguIndex(){
        
    }
    
    public static BuguIndex getInstance(){
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

    public DirectoryType getDirectoryType() {
        return directoryType;
    }

    public void setDirectoryType(DirectoryType directoryType) {
        this.directoryType = directoryType;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }
    
}
