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
            case MONGO:
                //待实现
                break;
            default:
                break;
        }
        return dir;
    }
    
}
