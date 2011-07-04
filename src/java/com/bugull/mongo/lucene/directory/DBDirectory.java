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

package com.bugull.mongo.lucene.directory;

import com.bugull.mongo.BuguConnection;
import com.bugull.mongo.cache.IndexFileCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBDirectory extends Directory{
    
    private String dirname;
    
    public DBDirectory(String dirname){
        this.dirname = dirname;
    }

    @Override
    public String[] listAll() throws IOException {
        DBCollection coll = BuguConnection.getInstance().getDB().getCollection("fs.files");
        List<String> list = coll.distinct("filename", new BasicDBObject("dirname", dirname));
        String[] arr = new String[list.size()];
        return list.toArray(arr);
    }

    @Override
    public boolean fileExists(String filename) throws IOException {
        IndexFile file = IndexFileCache.getInstance().get(dirname, filename);
        return file.exists();
    }

    @Override
    public long fileModified(String filename) throws IOException {
        IndexFile file = IndexFileCache.getInstance().get(dirname, filename);
        return file.getLastModify();
    }

    @Override
    @Deprecated
    public void touchFile(String name) throws IOException {
        //Lucene never uses this API; it will be removed in Lucene 4.0
    }

    @Override
    public void deleteFile(String filename) throws IOException {
        IndexFile file = IndexFileCache.getInstance().get(dirname, filename);
        file.delete();
    }

    @Override
    public long fileLength(String filename) throws IOException {
        IndexFile file = IndexFileCache.getInstance().get(dirname, filename);
        return file.getLength();
    }

    @Override
    public IndexOutput createOutput(String filename) throws IOException {
        return new DBIndexOutput(dirname, filename);
    }

    @Override
    public IndexInput openInput(String filename) throws IOException {
        return new DBIndexInput(dirname, filename);
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }
    
    @Override
    public Lock makeLock(String name) {
        return new DBLock();
    }
}
