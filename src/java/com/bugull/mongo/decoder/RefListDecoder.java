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

package com.bugull.mongo.decoder;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguDao;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.mapper.MapperUtil;
import com.mongodb.BasicDBObject;
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
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefListDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(RefListDecoder.class);
    
    private RefList refList;
    
    public RefListDecoder(Field field, DBObject dbo){
        super(field, dbo);
        refList = field.getAnnotation(RefList.class);
        String fieldName = field.getName();
        String name = refList.name();
        if(!name.equals("")){
            fieldName = name;
        }
        value = dbo.get(fieldName);
    }
    
    @Override
    public void decode(Object obj){
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        int len = types.length;
        if(len == 1){
            Class clazz = (Class)types[0];
            List<DBRef> list = (List<DBRef>)value;
            List<BuguEntity> result = new ArrayList<BuguEntity>();
            if(refList.lazy()){
                for(DBRef dbRef : list){
                    BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                    refObj.setId(dbRef.getId().toString());
                    result.add(refObj);
                }
            }else{
                ObjectId[] arr = new ObjectId[list.size()];
                int i = 0;
                for(DBRef dbRef : list){
                    arr[i++] = (ObjectId)dbRef.getId();
                }
                DBObject in = new BasicDBObject("$in", arr);
                DBObject query = new BasicDBObject(MapperUtil.ID, in);
                BuguDao dao = DaoCache.getInstance().get(clazz);
                String sort = refList.sort();
                if(sort.equals("")){
                    result = dao.find(query);
                }else{
                    result = dao.find(query, MapperUtil.getSort(sort));
                }
            }
            try{
                String typeName = field.getType().getName();
                if(typeName.equals("java.util.List")){
                    field.set(obj, result);
                }
                else if(typeName.equals("java.util.Set")){
                    field.set(obj, new HashSet(result));
                }
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        else if(len == 2){
            Class clazz = (Class)types[1];
            Map<Object, DBRef> map = (Map<Object, DBRef>)value;
            Map<Object, BuguEntity> result = new HashMap<Object, BuguEntity>();
            if(refList.lazy()){
                for(Object key : map.keySet()){
                    DBRef dbRef = map.get(key);
                    BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                    refObj.setId(dbRef.getId().toString());
                    result.put(key, refObj);
                }
            }else{
                BuguDao dao = DaoCache.getInstance().get(clazz);
                for(Object key : map.keySet()){
                    DBRef dbRef = map.get(key);
                    BuguEntity refObj = (BuguEntity)dao.findOne(dbRef.getId().toString());
                    result.put(key, refObj);
                }
            }
            try{
                field.set(obj, result);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
}
