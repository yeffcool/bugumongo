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
import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.decoder.Decoder;
import com.bugull.mongo.decoder.DecoderFactory;
import com.bugull.mongo.encoder.Encoder;
import com.bugull.mongo.encoder.EncoderFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
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
    
    public Object fromDBObject(Class clazz, DBObject dbo){
        Object obj = ConstructorCache.getInstance().create(clazz);
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field field : fields){
            Decoder decoder = DecoderFactory.create(field, dbo);
            if(decoder!=null && !decoder.isNullField()){
                decoder.decode(obj);
            }
        }
        return obj;
    }
    
    public DBObject toDBObject(Object obj){
        DBObject dbo = new BasicDBObject();
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field field : fields){
            Encoder encoder = EncoderFactory.create(obj, field);
            if(encoder!=null && !encoder.isNullField()){
                dbo.put(encoder.getFieldName(), encoder.encode());
            }
        }
        return dbo;
    }
    
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
        Class<?> clazz = obj.getClass();
        Field field = FieldsCache.getInstance().getField(clazz, fieldName);
        if(field.getAnnotation(Ref.class) != null){
            fetchRef(obj, field);
        }else if(field.getAnnotation(RefList.class) != null){
            fetchRefList(obj, field);
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
    
    private void fetchRef(BuguEntity obj, Field field){
        try{
            Object o = field.get(obj);
            if( o == null){
                return;
            }
            BuguEntity refObj = (BuguEntity)o;
            String id = refObj.getId();
            Class<?> type = field.getType();
            BuguDao dao = new BuguDao(type);
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
