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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
    
    protected InputStream input;
    protected String originalName;
    protected boolean rename;
    
    protected String filename;
    protected String folder;
    protected Map<String, Object> params;
    
    protected BuguFS fs;
    
    public Uploader(File file, String originalName){
        fs = BuguFS.getInstance();
        try{
            this.input = new FileInputStream(file);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
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
        this.input = new ByteArrayInputStream(data);
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
        if(params == null){
            params = new HashMap<String, Object>();
        }
        params.put(key, value);
    }
    
    public void save(){
        processFilename();
        saveInputStream();
    }

    public String getFilename() {
        return filename;
    }
    
    protected void processFilename(){
        if(rename){
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String dateStr = format.format(date);
            StringBuilder sb = new StringBuilder();
            sb.append(dateStr).append(date.getTime()).append(".").append(getExtention());
            filename = sb.toString();
        }else{
            filename = originalName;
        }
    }
    
    protected void saveInputStream(){
        fs.save(input, filename, folder, params);
        try{
            input.close();
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    /**
     * @return the extension name, such as doc, png, jpeg
     */
    protected String getExtention(){
        String ext = "";
        int index = originalName.lastIndexOf(".");
        if(index > 0){
            ext = originalName.substring(index + 1);
        }
        return ext;
    }
    
}
