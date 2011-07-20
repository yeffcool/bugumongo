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
    protected String fName;
    protected String filename;
    protected Map<String, Object> map;
    
    protected BuguFS fs;
    
    public Uploader(File file, String fName){
        this.file = file;
        this.fName = fName;
        fs = BuguFS.getInstance();
    }
    
    public void setAttribute(String key, Object value){
        if(map == null){
            map = new HashMap<String, Object>();
        }
        map.put(key, value);
    }
    
    public void save(){
        String[] temp = fName.split("[.]");
        String ext = temp[temp.length-1];
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String dateStr = format.format(date);
        filename = dateStr + date.getTime() + "." + ext;
        fs.save(file, filename, map);
    }

    public String getFilename() {
        return filename;
    }
    
}
