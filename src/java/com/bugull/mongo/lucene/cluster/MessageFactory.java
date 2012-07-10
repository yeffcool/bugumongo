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

package com.bugull.mongo.lucene.cluster;

import com.bugull.mongo.BuguEntity;

/**
 * Create kinds of message.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MessageFactory {
    
    public static EntityMessage createInsertMessage(BuguEntity entity){
        EntityMessage message = new EntityMessage();
        message.setType(ClusterMessage.TYPE_INSERT);
        message.setEntity(entity);
        return message;
    }
    
    public static EntityMessage createUpdateMessage(BuguEntity entity){
        EntityMessage message = new EntityMessage();
        message.setType(ClusterMessage.TYPE_UPDATE);
        message.setEntity(entity);
        return message;
    }
    
    public static ClassIdMessage createRemoveMessage(Class clazz, String id){
        ClassIdMessage message = new ClassIdMessage();
        message.setType(ClusterMessage.TYPE_REMOVE);
        message.setClazz(clazz);
        message.setId(id);
        return message;
    }
    
    public static ClassIdMessage createRefByMessage(Class clazz, String id){
        ClassIdMessage message = new ClassIdMessage();
        message.setType(ClusterMessage.TYPE_REF_BY);
        message.setClazz(clazz);
        message.setId(id);
        return message;
    }

}
