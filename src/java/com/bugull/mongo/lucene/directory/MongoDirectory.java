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

import com.bugull.mongo.mapper.Operator;
import com.mongodb.DBObject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.bson.types.ObjectId;

/**
 * Store lucene index files in mongoDB.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MongoDirectory extends Directory {
    
    private String directoryName;
    private Map<String, MongoFile> files;
    private LuceneIndexFS fs = LuceneIndexFS.getInstance();
    
    public MongoDirectory(String directoryName) throws IOException {
        this.directoryName = directoryName;
        this.setLockFactory(new SingleInstanceLockFactory());
        files = new ConcurrentHashMap<String, MongoFile>();
        List<String> list = fs.getFileList(directoryName);
        for(String filename : list){
            MongoFile mf = this.getFile(filename, false);
            files.put(filename, mf);
        }
    }

    /**
     * Returns an array of strings, one for each file in the directory.
     * @return
     * @throws IOException 
     */
    @Override
    public String[] listAll() throws IOException {
        this.ensureOpen();
        return files.keySet().toArray(new String[]{});
    }

    /**
     * Returns true if a file with the given name exists.
     * @param string
     * @return
     * @throws IOException 
     */
    @Override
    public boolean fileExists(String filename) throws IOException {
        try{
            MongoFile file = this.getFile(filename, false);
            return file != null;
        }catch(IOException ex){
            return false;
        }
    }

    /**
     * Returns the time the named file was last modified.
     * @param string
     * @return
     * @throws IOException 
     */
    @Override
    public long fileModified(String filename) throws IOException {
        this.ensureOpen();
        MongoFile file = this.getFile(filename, false);
        return file.getLastModify();
    }

    /**
     * Deprecated. Lucene never uses this API; it will be removed in 4.0.
     * @param string
     * @throws IOException 
     */
    @Override
    @Deprecated
    public void touchFile(String filename) throws IOException {
        //never use
    }

    /**
     * Removes an existing file in the directory.
     * @param string
     * @throws IOException 
     */
    @Override
    public void deleteFile(String filename) throws IOException {
        this.ensureOpen();
        fs.removeFile(directoryName, filename);
        files.remove(filename);
    }

    /**
     * Returns the length of a file in the directory.
     * @param string
     * @return
     * @throws IOException 
     */
    @Override
    public long fileLength(String filename) throws IOException {
        this.ensureOpen();
        MongoFile file = this.getFile(filename, false);
        return file.getFileLength();
    }

    /**
     * Creates a new, empty file in the directory with the given name. 
     * Returns a stream writing this file.
     * @param filename
     * @return
     * @throws IOException 
     */
    @Override
    public IndexOutput createOutput(String filename) throws IOException {
        this.ensureOpen();
        MongoFile file = this.getFile(filename, true);
        return new MongoIndexOutput(file);
    }

    /**
     * Returns a stream reading an existing file.
     * @param filename
     * @return
     * @throws IOException 
     */
    @Override
    public IndexInput openInput(String filename) throws IOException {
        this.ensureOpen();
        MongoFile file = this.getFile(filename, false);
        return new MongoIndexInput(file);
    }

    /**
     * Closes the directory.
     * @throws IOException 
     */
    @Override
    public void close() throws IOException {
        this.isOpen = false;
    }
    
    private MongoFile getFile(String filename, boolean createIfNotFound) throws IOException {
        if(files.containsKey(filename)){
            return files.get(filename);
        }
        DBObject dbo = fs.findFile(directoryName, filename);
        if(dbo != null || createIfNotFound){
            MongoFile f = new MongoFile(this, filename);
            fillChunks(f, dbo);
            files.put(filename, f);
            return f;
        }
        throw new IOException("File not found: " + filename);
    }
    
    private void fillChunks(MongoFile f, DBObject dbo){
        if(dbo == null){
            f.setFileLength(0);
            fs.createEmptyFile(f);
            f.setChunks(new ConcurrentHashMap<Integer, MongoChunk>());
        }else{
            long len = (Long)dbo.get(LuceneIndexFS.LENGTH);
            f.setFileLength(len);
            ObjectId oid = (ObjectId)dbo.get(Operator.ID);
            f.setId(oid);
            f.setChunks(fs.getChunks(f));
        }
    }
    
    public String getDirectoryName(){
        return directoryName;
    }

}
