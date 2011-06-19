package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    public void entityInsert(BuguEntity obj){
        IndexInsertThread thread = new IndexInsertThread(obj);
        new Thread(thread).start();
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexUpdateThread thread = new IndexUpdateThread(obj);
        new Thread(thread).start();
    }
    
    public void entityRemove(Class<?> clazz, String id){
        IndexRemoveThread thread = new IndexRemoveThread(clazz, id);
        new Thread(thread).start();
    }
    
}
