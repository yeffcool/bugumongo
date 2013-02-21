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

import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.DBConnectionException;
import com.bugull.mongo.exception.FieldException;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.FieldUtil;
import com.bugull.mongo.mapper.IdUtil;
import com.bugull.mongo.mapper.InternalDao;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.bugull.mongo.mapper.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * The utility class for ODM(Object Document Mapping).
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class BuguMapper {
    
    private final static Logger logger = Logger.getLogger(BuguMapper.class);
    
    /**
     * @deprecated BuguDao and BuguQuery can convert by itself now. This method is not used any more.
     * @param obj
     * @return 
     */
    @Deprecated
    public static DBRef toDBRef(BuguEntity obj){
        String idStr = obj.getId();
        if(StringUtil.isEmpty(idStr)){
            return null;
        }
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        Class<?> clazz = obj.getClass();
        String name = MapperUtil.getEntityName(clazz);
        return new DBRef(db, name, IdUtil.toDbId(clazz, idStr));
    }
    
    /**
     * @deprecated BuguDao and BuguQuery can convert by itself now. This method is not used any more.
     * @param clazz
     * @param idStr
     * @return 
     */
    @Deprecated
    public static DBRef toDBRef(Class<?> clazz, String idStr){
        if(StringUtil.isEmpty(idStr)){
            return null;
        }
        DB db = null;
        try {
            db = BuguConnection.getInstance().getDB();
        } catch (DBConnectionException ex) {
            logger.error(ex.getMessage(), ex);
        }
        String name = MapperUtil.getEntityName(clazz);
        return new DBRef(db, name, IdUtil.toDbId(clazz, idStr));
    }
    
    /**
     * Fetch out the lazy @Property, @Embed, @EmbedList field of a list
     * @param list the list needs to operate on
     */
    public static void fetchLazy(List list){
        for(Object o : list){
            if(o != null){
                BuguEntity obj = (BuguEntity)o;
                BuguEntity newObj = (BuguEntity)DaoCache.getInstance().get(obj.getClass()).findOne(obj.getId());
                FieldUtil.copy(newObj, obj);
            }
        }
    }
    
    /**
     * Fetch out the lazy @Property, @Embed, @EmbedList field of an entity.
     * <p>The entity must be an element of a list.</p>
     * @param obj the entity needs to operate on
     */
    public static void fetchLazy(BuguEntity obj){
        BuguEntity newObj = (BuguEntity)DaoCache.getInstance().get(obj.getClass()).findOne(obj.getId());
        FieldUtil.copy(newObj, obj);
    }
    
    /**
     * Fetch out the cascade @Ref or @RefList entity.
     * @param obj the entity needs to operate on
     * @param names the fields' names
     */
    public static void fetchCascade(BuguEntity obj, String... names){
        if(obj != null){
            for(String name : names){
                String remainder = null;
                int index = name.indexOf(".");
                if(index > 0){
                    remainder = name.substring(index+1);
                    name = name.substring(0, index);
                }
                fetchOneLevel(obj, name);
                if(remainder != null){
                    fetchRemainder(obj, name, remainder);
                }
            }
        }
    }
    
    /**
     * Fetch out the cascade @Ref or @RefList entity.
     * @param list the list needs to operate on
     * @param names the fields' names
     */
    public static void fetchCascade(List list, String... names){
        for(Object o : list){
            if(o != null){
                BuguEntity obj = (BuguEntity)o;
                fetchCascade(obj, names);
            }
        }
    }
    
    private static void fetchOneLevel(BuguEntity obj, String fieldName){
        Field field = null;
        try{
            field = FieldsCache.getInstance().getField(obj.getClass(), fieldName);
        }catch(FieldException ex){
            logger.error(ex.getMessage(), ex);
        }
        if(field.getAnnotation(Ref.class) != null){
            fetchRef(obj, field);
        }else if(field.getAnnotation(RefList.class) != null){
            fetchRefList(obj, field);
        }
    }
    
    private static void fetchRemainder(BuguEntity obj, String fieldName, String remainder){
        Field field = null;
        try{
            field = FieldsCache.getInstance().getField(obj.getClass(), fieldName);
        }catch(FieldException ex){
            logger.error(ex.getMessage(), ex);
        }
        Object value = FieldUtil.get(obj, field);
        if(value == null){
            return;
        }
        if(field.getAnnotation(Ref.class) != null){
            BuguEntity entity = (BuguEntity)value;
            fetchCascade(entity, remainder);
        }else if(field.getAnnotation(RefList.class) != null){
            Class type = field.getType();
            if(DataType.isList(type)){
                List<BuguEntity> list = (List<BuguEntity>)value;
                for(BuguEntity entity : list){
                    fetchCascade(entity, remainder);
                }
            }
            else if(DataType.isSet(type)){
                Set<BuguEntity> set = (Set<BuguEntity>)value;
                for(BuguEntity entity : set){
                    fetchCascade(entity, remainder);
                }
            }
            else if(DataType.isMap(type)){
                Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)value;
                for(Entry<Object, BuguEntity> entry : map.entrySet()){
                    fetchCascade(entry.getValue(), remainder);
                }
            }
        }
    }
    
    private static void fetchRef(BuguEntity obj, Field field){
        Object o = FieldUtil.get(obj, field);
        if( o == null){
            return;
        }
        BuguEntity refObj = (BuguEntity)o;
        String id = refObj.getId();
        Class cls = FieldUtil.getRealType(field);
        InternalDao dao = DaoCache.getInstance().get(cls);
        Object value = dao.findOne(id);
        FieldUtil.set(obj, field, value);
    }
    
    private static void fetchRefList(BuguEntity obj, Field field){
        Class<?> type = field.getType();
        if(type.isArray()){
            fetchArray(obj, field, type.getComponentType());
        }else{
            ParameterizedType paramType = (ParameterizedType)field.getGenericType();
            Type[] types = paramType.getActualTypeArguments();
            int len = types.length;
            if(len == 1){
                //for List and Set
                fetchListAndSet(obj, field, (Class)types[0]);
            }else if(len == 2){
                //for Map
                fetchMap(obj, field, (Class)types[1]);
            }
        }
    }
    
    private static void fetchArray(BuguEntity obj, Field field, Class clazz) {
        Object o = FieldUtil.get(obj, field);
        if(o == null){
            return;
        }
        int len = Array.getLength(o);
        clazz = FieldUtil.getRealType(clazz);
        Object arr = Array.newInstance(clazz, len);
        List<Object> idList = new ArrayList<Object>();
        for(int i=0; i<len; i++){
            Object item = Array.get(o, i);
            if(item != null){
                BuguEntity entity = (BuguEntity)item;
                Object dbId = IdUtil.toDbId(entity.getClass(), entity.getId());
                idList.add(dbId);
            }
        }
        DBObject in = new BasicDBObject(Operator.IN, idList);
        DBObject query = new BasicDBObject(Operator.ID, in);
        InternalDao dao = DaoCache.getInstance().get(clazz);
        RefList refList = field.getAnnotation(RefList.class);
        String sort = refList.sort();
        List<BuguEntity> entityList = null;
        if(sort.equals(Default.SORT)){
            entityList = dao.find(query);
        }else{
            entityList = dao.find(query, MapperUtil.getSort(sort));
        }
        if(entityList.size() != len){
            len = entityList.size();
            arr = Array.newInstance(clazz, len);
        }
        for(int i=0; i<len; i++){
            Array.set(arr, i, entityList.get(i));
        }
        FieldUtil.set(obj, field, arr);
    }
    
    private static void fetchListAndSet(BuguEntity obj, Field field, Class clazz){
        Object o = FieldUtil.get(obj, field);
        if(o == null){
            return;
        }
        Class type = field.getType();
        RefList refList = field.getAnnotation(RefList.class);
        clazz = FieldUtil.getRealType(clazz);
        InternalDao dao = DaoCache.getInstance().get(clazz);
        if(DataType.isList(type)){
            List<BuguEntity> list = (List<BuguEntity>)o;
            List<Object> idList = new ArrayList<Object>();
            for(BuguEntity ent : list){
                if(ent != null){
                    Object dbId = IdUtil.toDbId(ent.getClass(), ent.getId());
                    idList.add(dbId);
                }
            }
            DBObject in = new BasicDBObject(Operator.IN, idList);
            DBObject query = new BasicDBObject(Operator.ID, in);
            String sort = refList.sort();
            List result = null;
            if(sort.equals(Default.SORT)){
                result = dao.find(query);
            }else{
                result = dao.find(query, MapperUtil.getSort(sort));
            }
            FieldUtil.set(obj, field, result);
        }
        else if(DataType.isSet(type)){
            Set<BuguEntity> set = (Set<BuguEntity>)o;
            List<Object> idList = new ArrayList<Object>();
            for(BuguEntity ent : set){
                if(ent != null){
                    Object dbId = IdUtil.toDbId(ent.getClass(), ent.getId());
                    idList.add(dbId);
                }
            }
            DBObject in = new BasicDBObject(Operator.IN, idList);
            DBObject query = new BasicDBObject(Operator.ID, in);
            String sort = refList.sort();
            List result = null;
            if(sort.equals(Default.SORT)){
                result = dao.find(query);
            }else{
                result = dao.find(query, MapperUtil.getSort(sort));
            }
            FieldUtil.set(obj, field, new HashSet(result));
        }
    }
    
    private static void fetchMap(BuguEntity obj, Field field, Class clazz) {
        Object o = FieldUtil.get(obj, field);
        if(o == null){
            return;
        }
        Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)o;
        Map result = new HashMap();
        clazz = FieldUtil.getRealType(clazz);
        InternalDao dao = DaoCache.getInstance().get(clazz);
        for(Entry<Object, BuguEntity> entry : map.entrySet()){
            BuguEntity refObj = entry.getValue();
            if(refObj != null){
                String id = refObj.getId();
                Object value = dao.findOne(id);
                result.put(entry.getKey(), value);
            }else{
                result.put(entry.getKey(), null);
            }
        }
        FieldUtil.set(obj, field, result);
    }
    
}
