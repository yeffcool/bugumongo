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

package com.bugull.mongo.mapper;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.cache.DaoCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class DeleteCascadeTask implements Runnable{
        
    private List<Field> refFields;
    private List<Field> refListFields;
    private BuguEntity entity;

    public DeleteCascadeTask(List<Field> refFields, List<Field> refListFields, BuguEntity entity) {
        this.refFields = refFields;
        this.refListFields = refListFields;
        this.entity = entity;
    }

    @Override
    public void run(){
        for(Field f : refFields){
            processRef(f);
        }
        for(Field f : refListFields){
            processRefList(f);
        }
    }
    
    private void processRef(Field f){
        Object value = FieldUtil.get(entity, f);
        if(value != null){
            Class<?> type = f.getType();
            BuguDao dao = DaoCache.getInstance().get(type);
            BuguEntity obj = (BuguEntity)value;
            dao.remove(obj.getId());
        }
    }
    
    private void processRefList(Field f){
        Object value = FieldUtil.get(entity, f);
        if(value == null){
            return;
        }
        ObjectId[] objs = null;
        Class<?> clazz = null;
        Class<?> type = f.getType();
        if(type.isArray()){
            clazz = type.getComponentType();
            objs = getArrayIds(value);
        }else{
            ParameterizedType paramType = (ParameterizedType)f.getGenericType();
            Type[] types = paramType.getActualTypeArguments();
            int len = types.length;
            if(len == 1){
                clazz = (Class)types[0];
                objs = getListIds(value, type);
            }else if(len == 2){
                clazz = (Class)types[1];
                objs = getMapIds(value);
            }
        }
        BuguDao dao = DaoCache.getInstance().get(clazz);
        DBObject in = new BasicDBObject(Operator.IN, objs);
        DBObject query = new BasicDBObject(Operator.ID, in);
        dao.remove(query);
    }
    
    private ObjectId[] getArrayIds(Object value){
        int len = Array.getLength(value);
        ObjectId[] objs = new ObjectId[len];
        for(int i=0; i<len; i++){
            BuguEntity obj = (BuguEntity)Array.get(value, i);
            objs[i] = new ObjectId(obj.getId());
        }
        return objs;
    }
    
    private ObjectId[] getListIds(Object value, Class type){
        ObjectId[] objs = null;
        if(DataType.isList(type)){
            List<BuguEntity> list = (List<BuguEntity>)value;
            int len = list.size();
            objs = new ObjectId[len];
            int i = 0;
            for(BuguEntity obj : list){
                objs[i++] = new ObjectId(obj.getId());
            }
        }
        else if(DataType.isSet(type)){
            Set<BuguEntity> set = (Set<BuguEntity>)value;
            int len = set.size();
            objs = new ObjectId[len];
            int i = 0;
            for(BuguEntity obj : set){
                objs[i++] = new ObjectId(obj.getId());
            }
        }
        return objs;
    }
    
    private ObjectId[] getMapIds(Object value){
        Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)value;
        Set<Object> keys = map.keySet();
        int len = keys.size();
        ObjectId[] objs = new ObjectId[len];
        int i = 0;
        for(Object key : keys){
            BuguEntity obj = map.get(key);
            objs[i++] = new ObjectId(obj.getId());
        }
        return objs;
    }
    
}
