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

import com.bugull.mongo.fs.BuguFS;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DBDirectory extends Directory{
    
    private String dirName;
    
    public DBDirectory(String dirName){
        this.dirName = dirName;
    }

    @Override
    public String[] listAll() throws IOException {
        GridFS fs = BuguFS.getInstance().getFS();
        DBObject dbo = new BasicDBObject("dirName", dirName);
        List<GridFSDBFile> list = fs.find(dbo);
        int size = list.size();
        String[] files = new String[size];
        for(int i=0; i<size; i++){
            GridFSDBFile f = list.get(i);
            files[i] = f.get("fileName").toString();
        }
        return files;
    }

    @Override
    public boolean fileExists(String fileName) throws IOException {
        IndexFile file = new IndexFile(dirName, fileName);
        return file.exists();
    }

    @Override
    public long fileModified(String fileName) throws IOException {
        IndexFile file = new IndexFile(dirName, fileName);
        if(file.exists()){
            return file.getLastModify();
        }else{
            throw new IOException("File does not exist: " + fileName);
        }
    }

    @Override
    @Deprecated
    public void touchFile(String name) throws IOException {
        //Lucene never uses this API; it will be removed in Lucene 4.0
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        IndexFile file = new IndexFile(dirName, fileName);
        if(file.exists()){
            file.delete();
        }else{
            throw new IOException("File does not exist: " + fileName);
        }
    }

    @Override
    public long fileLength(String fileName) throws IOException {
        IndexFile file = new IndexFile(dirName, fileName);
        if(file.exists()){
            return file.getLength();
        }else{
            throw new IOException("File does not exist: " + fileName);
        }
    }

    @Override
    public IndexOutput createOutput(String fileName) throws IOException {
        return new DBIndexOutput(dirName, fileName);
    }

    @Override
    public IndexInput openInput(String fileName) throws IOException {
        return new DBIndexInput(dirName, fileName);
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }
    
}
