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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class Uploader {
    
    protected File file;
    protected InputStream input;
    protected byte[] data;
    protected String originalName;
    protected boolean rename;
    protected String filename;
    protected String folder;
    protected Map<String, Object> map;
    
    protected BuguFS fs;
    
    public Uploader(File file, String originalName){
        fs = BuguFS.getInstance();
        this.file = file;
        this.originalName = originalName;
    }
    
    public Uploader(File file, String originalName, boolean rename){
        this(file, originalName);
        this.rename = rename;
    }
    
    public Uploader(InputStream input, String originalName){
        fs = BuguFS.getInstance();
        this.input = input;
        this.originalName = originalName;
    }
    
    public Uploader(InputStream input, String originalName, boolean rename){
        this(input, originalName);
        this.rename = rename;
    }
    
    public Uploader(byte[] data, String originalName){
        fs = BuguFS.getInstance();
        this.data = data;
        this.originalName = originalName;
    }
    
    public Uploader(byte[] data, String originalName, boolean rename){
        this(data, originalName);
        this.rename = rename;
    }
    
    public void setFolder(String folder){
        this.folder = folder;
    }
    
    public void setAttribute(String key, Object value){
        if(map == null){
            map = new HashMap<String, Object>();
        }
        map.put(key, value);
    }
    
    public void save(){
        if(rename){
            String ext = "";
            int index = originalName.lastIndexOf(".");
            if(index > 0){
                ext = originalName.substring(index);
            }
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String dateStr = format.format(date);
            filename = dateStr + date.getTime() + ext;
        }else{
            filename = originalName;
        }
        if(file != null){
            fs.save(file, filename, folder, map);
        }else if(input != null){
            fs.save(input, filename, folder, map);
        }else if(data != null){
            fs.save(data, filename, folder, map);
        }
    }

    public String getFilename() {
        return filename;
    }
    
    public long getLength(){
        return file.length();
    }
    
}
