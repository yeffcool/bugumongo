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

package com.bugull.mongo;

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.FieldsCache;
import com.mongodb.DB;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguMapper {
    
    private final static Logger logger = Logger.getLogger(BuguMapper.class);
    
    public DBRef toDBRef(BuguEntity obj){
        DB db = BuguConnection.getInstance().getDB();
        Class<?> clazz = obj.getClass();
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        if(name.equals("")){
            name = clazz.getSimpleName().toLowerCase();
        }
        ObjectId id = new ObjectId(obj.getId());
        return new DBRef(db, name, id);
    }
    
    public void fetch(BuguEntity obj, String fieldName){
        String remainder = null;
        int index = fieldName.indexOf(".");
        if(index > 0){
            remainder = fieldName.substring(index+1);
            fieldName = fieldName.substring(0, index);
        }
        fetchOneLevel(obj, fieldName);
        if(remainder != null){
            try{
                fetchRemainder(obj, fieldName, remainder);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    public void fetch(BuguEntity obj, String[] names){
        for(String fieldName : names){
            fetch(obj, fieldName);
        }
    }
    
    public void fetch(List list, String fieldName){
        List<BuguEntity> result = new LinkedList<BuguEntity>();
        for(Object o : list){
            BuguEntity obj = (BuguEntity)o;
            fetch(obj, fieldName);
            result.add(obj);
        }
        list = result;
    }
    
    public void fetch(List list, String[] names){
        List<BuguEntity> result = new LinkedList<BuguEntity>();
        for(Object o : list){
            BuguEntity obj = (BuguEntity)o;
            fetch(obj, names);
            result.add(obj);
        }
        list = result;
    }
    
    private void fetchOneLevel(BuguEntity obj, String fieldName){
        Field field = FieldsCache.getInstance().getField(obj.getClass(), fieldName);
        if(field.getAnnotation(Ref.class) != null){
            fetchRef(obj, field);
        }else if(field.getAnnotation(RefList.class) != null){
            fetchRefList(obj, field);
        }
    }
    
    private void fetchRemainder(BuguEntity obj, String fieldName, String remainder) throws Exception {
        Field field = FieldsCache.getInstance().getField(obj.getClass(), fieldName);
        Object value = field.get(obj);
        if(value == null){
            return;
        }
        if(field.getAnnotation(Ref.class) != null){
            BuguEntity entity = (BuguEntity)value;
            fetch(entity, remainder);
        }else if(field.getAnnotation(RefList.class) != null){
            String typeName = field.getType().getName();
            if(typeName.equals("java.util.List")){
                List<BuguEntity> list = (List<BuguEntity>)value;
                for(BuguEntity entity : list){
                    fetch(entity, remainder);
                }
            }
            else if(typeName.equals("java.util.Set")){
                Set<BuguEntity> set = (Set<BuguEntity>)value;
                for(BuguEntity entity : set){
                    fetch(entity, remainder);
                }
            }
        }
    }
    
    private void fetchRef(BuguEntity obj, Field field){
        try{
            Object o = field.get(obj);
            if( o == null){
                return;
            }
            BuguEntity refObj = (BuguEntity)o;
            String id = refObj.getId();
            BuguDao dao = new BuguDao(field.getType());
            Object value = dao.findOne(id);
            field.set(obj, value);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private void fetchRefList(BuguEntity obj, Field field){
        try{
            Object o = field.get(obj);
            if(o == null){
                return;
            }
            ParameterizedType type = (ParameterizedType)field.getGenericType();
            Type[] types = type.getActualTypeArguments();
            Class clazz = (Class)types[0];
            BuguDao dao = new BuguDao(clazz);
            String typeName = field.getType().getName();
            if(typeName.equals("java.util.List")){
                List<BuguEntity> list = (List<BuguEntity>)o;
                List result = new LinkedList();
                for(BuguEntity refObj : list){
                    String id = refObj.getId();
                    Object value = dao.findOne(id);
                    result.add(value);
                }
                field.set(obj, result);
            }
            else if(typeName.equals("java.util.Set")){
                Set<BuguEntity> set = (Set<BuguEntity>)o;
                Set result = new HashSet();
                for(BuguEntity refObj : set){
                    String id = refObj.getId();
                    Object value = dao.findOne(id);
                    result.add(value);
                }
                field.set(obj, result);
            }
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
}
