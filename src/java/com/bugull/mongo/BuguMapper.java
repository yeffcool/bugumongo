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
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.mapper.MapperUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguMapper {
    
    private final static Logger logger = Logger.getLogger(BuguMapper.class);
    
    public static DBRef toDBRef(BuguEntity obj){
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
    
    public static void fetch(BuguEntity obj, String... names){
        for(String name : names){
            String remainder = null;
            int index = name.indexOf(".");
            if(index > 0){
                remainder = name.substring(index+1);
                name = name.substring(0, index);
            }
            fetchOneLevel(obj, name);
            if(remainder != null){
                try{
                    fetchRemainder(obj, name, remainder);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
            }
        }
    }
    
    public static void fetch(List list, String... names){
        List<BuguEntity> result = new ArrayList<BuguEntity>();
        for(Object o : list){
            BuguEntity obj = (BuguEntity)o;
            fetch(obj, names);
            result.add(obj);
        }
        list = result;
    }
    
    private static void fetchOneLevel(BuguEntity obj, String fieldName){
        Field field = FieldsCache.getInstance().getField(obj.getClass(), fieldName);
        if(field.getAnnotation(Ref.class) != null){
            try {
                fetchRef(obj, field);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }else if(field.getAnnotation(RefList.class) != null){
            try {
                fetchRefList(obj, field);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
    
    private static void fetchRemainder(BuguEntity obj, String fieldName, String remainder) throws Exception {
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
            else if(typeName.equals("java.util.Map")){
                Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)value;
                for(Object key : map.keySet()){
                    fetch(map.get(key), remainder);
                }
            }
        }
    }
    
    private static void fetchRef(BuguEntity obj, Field field) throws Exception {
        Object o = field.get(obj);
        if( o == null){
            return;
        }
        BuguEntity refObj = (BuguEntity)o;
        String id = refObj.getId();
        BuguDao dao = DaoCache.getInstance().get(field.getType());
        Object value = dao.findOne(id);
        field.set(obj, value);
    }
    
    private static void fetchRefList(BuguEntity obj, Field field) throws Exception{
        Object o = field.get(obj);
        if(o == null){
            return;
        }
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        int len = types.length;
        if(len == 1){
            Class clazz = (Class)types[0];
            BuguDao dao = DaoCache.getInstance().get(clazz);
            String typeName = field.getType().getName();
            RefList refList = field.getAnnotation(RefList.class);
            if(typeName.equals("java.util.List")){
                List<BuguEntity> list = (List<BuguEntity>)o;
                ObjectId[] arr = new ObjectId[list.size()];
                int i = 0;
                for(BuguEntity ent : list){
                    arr[i++] = new ObjectId(ent.getId());
                }
                DBObject in = new BasicDBObject("$in", arr);
                DBObject query = new BasicDBObject("_id", in);
                String sort = refList.sort();
                List result = null;
                if(sort.equals("")){
                    result = dao.find(query);
                }else{
                    result = dao.find(query, MapperUtil.getSort(sort));
                }
                field.set(obj, result);
            }
            else if(typeName.equals("java.util.Set")){
                Set<BuguEntity> set = (Set<BuguEntity>)o;
                ObjectId[] arr = new ObjectId[set.size()];
                int i = 0;
                for(BuguEntity ent : set){
                    arr[i++] = new ObjectId(ent.getId());
                }
                DBObject in = new BasicDBObject("$in", arr);
                DBObject query = new BasicDBObject("_id", in);
                String sort = refList.sort();
                List result = null;
                if(sort.equals("")){
                    result = dao.find(query);
                }else{
                    result = dao.find(query, MapperUtil.getSort(sort));
                }
                field.set(obj, new HashSet(result));
            }
        }
        else if(len == 2){
            Class clazz = (Class)types[1];
            BuguDao dao = DaoCache.getInstance().get(clazz);
            Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)o;
            Map result = new HashMap();
            for(Object key : map.keySet()){
                BuguEntity refObj = map.get(key);
                String id = refObj.getId();
                Object value = dao.findOne(id);
                result.put(key, value);
            }
            field.set(obj, result);
        }
    }
    
}
