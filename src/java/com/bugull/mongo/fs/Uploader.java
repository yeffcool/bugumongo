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

package com.bugull.mongo.fs;

import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class Uploader {
    
    private final static Logger logger = Logger.getLogger(Uploader.class);
    
    protected File file;
    protected String fName;
    protected String filename;
    protected GridFS fs;
    protected Map<String, Object> map;
    
    public Uploader(File file, String fName){
        this.file = file;
        this.fName = fName;
        fs = BuguFS.getInstance().getFS();
    }
    
    public void setAttribute(String key, Object value){
        if(map == null){
            map = new HashMap<String, Object>();
        }
        map.put(key, value);
    }
    
    public void save(){
        String[] temp = fName.split("[.]");
        String ext = temp[temp.length-1].toLowerCase();
        if(ext.equals("jpg")){
            ext = "jpeg";
        }
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String dateStr = format.format(date);
        filename = dateStr + date.getTime() + "." + ext;
        GridFSInputFile f = null;
        try{
            f = fs.createFile(file);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        f.setFilename(filename);
        if(map != null){
            f.putAll(map);
        }
        f.save();
    }

    public String getFilename() {
        return filename;
    }
    
}
