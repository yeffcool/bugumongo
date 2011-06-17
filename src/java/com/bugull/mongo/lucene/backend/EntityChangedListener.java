package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    public void entityInsert(BuguEntity obj, String id){
        IndexInsertThread thread = new IndexInsertThread(obj, id);
        new Thread(thread).start();
    }
    
    public void entityUpdate(BuguEntity obj){
        String id = obj.getId();
        entityRemove(obj.getClass(), id);
        entityInsert(obj, id);
    }
    
    public void entityRemove(Class<?> clazz, String id){
        IndexRemoveThread thread = new IndexRemoveThread(clazz, id);
        new Thread(thread).start();
    }
    
}
