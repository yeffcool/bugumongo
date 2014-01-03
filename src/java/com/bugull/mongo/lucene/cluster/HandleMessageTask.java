/*
 * Copyright (c) www.bugull.com
 * 
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

package com.bugull.mongo.lucene.cluster;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.lucene.BuguIndex;
import com.bugull.mongo.lucene.backend.*;
import com.bugull.mongo.misc.InternalDao;

/**
 * Handle the received message.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class HandleMessageTask implements Runnable {
    
    private ClusterMessage message;

    public HandleMessageTask(ClusterMessage message) {
        this.message = message;
    }
    
    @Override
    public void run() {
        switch(message.getType()){
            case ClusterMessage.TYPE_INSERT:
                handleInsert();
                break;
            case ClusterMessage.TYPE_UPDATE:
                handleUpdate();
                break;
            case ClusterMessage.TYPE_REMOVE:
                handleRemove();
                break;
            case ClusterMessage.TYPE_REF_BY:
                handleRefBy();
                break;
            default:
                break;
        }
    }
    
    private void handleInsert(){
        EntityMessage msg = (EntityMessage)message;
        BuguEntity entity = msg.getEntity();
        if(entity != null){
            IndexInsertTask task = new IndexInsertTask(entity);
            BuguIndex.getInstance().getExecutor().execute(task);
        }
    }
    
    private void handleUpdate(){
        EntityMessage msg = (EntityMessage)message;
        BuguEntity entity = msg.getEntity();
        if(entity != null){
            IndexUpdateTask task = new IndexUpdateTask(entity);
            BuguIndex.getInstance().getExecutor().execute(task);
        }
    }
    
    private void handleRemove(){
        ClassIdMessage msg = (ClassIdMessage)message;
        Class<?> clazz = msg.getClazz();
        String id = msg.getId();
        if(clazz!=null && id!=null){
            IndexRemoveTask task = new IndexRemoveTask(clazz, id);
            BuguIndex.getInstance().getExecutor().execute(task);
        }
    }
    
    private void handleRefBy(){
        ClassIdMessage msg = (ClassIdMessage)message;
        Class<?> clazz = msg.getClazz();
        String id = msg.getId();
        if(clazz!=null && id!=null){
            InternalDao dao = DaoCache.getInstance().get(clazz);
            EntityChangedListener luceneListener = dao.getLuceneListener();
            if(luceneListener != null){
                RefEntityChangedListener refListener = luceneListener.getRefListener();
                if(refListener != null){
                    refListener.entityChange(clazz, id);
                }
            }
        }
    }

}
