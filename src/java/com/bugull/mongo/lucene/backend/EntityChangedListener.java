package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguEntity;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EntityChangedListener {
    
    public void entityInsert(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexInsertThread thread = new IndexInsertThread(obj);
            new Thread(thread).start();
        }
    }
    
    public void entityUpdate(BuguEntity obj){
        IndexFilterChecker checker = new IndexFilterChecker(obj);
        if(checker.needIndex()){
            IndexUpdateThread thread = new IndexUpdateThread(obj);
            new Thread(thread).start();
        }else{
            entityRemove(obj.getClass(), obj.getId());
        }
        
    }
    
    public void entityRemove(Class<?> clazz, String id){
        IndexRemoveThread thread = new IndexRemoveThread(clazz, id);
        new Thread(thread).start();
    }
    
}
