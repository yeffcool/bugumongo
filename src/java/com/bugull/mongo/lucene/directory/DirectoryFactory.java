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

import java.io.File;
import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DirectoryFactory {
    
    private final static Logger logger = Logger.getLogger(DirectoryFactory.class);
    
    public static Directory create(DirectoryType type, String path, String name){
        Directory dir = null;
        switch(type){
            case FS:
                try{
                    dir = FSDirectory.open(new File(path + "/" + name));
                }catch(Exception e){
                    logger.error(e);
                }
                break;
            case DB:
                //待实现
                break;
            default:
                break;
        }
        return dir;
    }
    
}
