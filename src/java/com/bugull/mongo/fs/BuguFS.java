package com.bugull.mongo.fs;

import com.bugull.mongo.BuguConnection;
import com.mongodb.gridfs.GridFS;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguFS {
    
    private static BuguFS instance;
    
    private GridFS fs;
    
    private BuguFS(){
        fs = new GridFS(BuguConnection.getInstance().getDB());
    }
    
    public static BuguFS getInstance(){
        if(instance == null){
            instance = new BuguFS();
        }
        return instance;
    }
    
    public GridFS getFS(){
        return fs;
    }
    
}
