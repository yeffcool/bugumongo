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

package com.bugull.mongo.encoder;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.DaoCache;
import com.bugull.mongo.mapper.DataType;
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
import java.util.Set;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefListEncoder extends AbstractEncoder{
    
    private RefList refList;
    
    public RefListEncoder(Object obj, Field field){
        super(obj, field);
        refList = field.getAnnotation(RefList.class);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        String name = refList.name();
        if(!name.equals(Default.NAME)){
            fieldName = name;
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        Class<?> type = field.getType();
        if(type.isArray()){
            return encodeArray(type.getComponentType());
        }else{
            return encodeList(type);
        }
    }
    
    private Object encodeArray(Class<?> clazz){
        int len = Array.getLength(value);
        DBRef[] refs = new DBRef[len];
        BuguDao dao = DaoCache.getInstance().get(clazz);
        for(int i=0; i<len; i++){
            BuguEntity entity = (BuguEntity)Array.get(value, i);
            doCascade(dao, entity);
            refs[i] = BuguMapper.toDBRef(entity);
        }
        return refs;
    }
    
    private Object encodeList(Class type){
        ParameterizedType paramType = (ParameterizedType)field.getGenericType();
        Type[] types = paramType.getActualTypeArguments();
        if(DataType.isList(type)){
            List<BuguEntity> list = (List<BuguEntity>)value;
            List<DBRef> result = new ArrayList<DBRef>();
            BuguDao dao = DaoCache.getInstance().get((Class)types[0]);
            for(BuguEntity entity : list){
                doCascade(dao, entity);
                result.add(BuguMapper.toDBRef(entity));
            }
            return result;
        }
        else if(DataType.isSet(type)){
            Set<BuguEntity> set = (Set<BuguEntity>)value;
            Set<DBRef> result = new HashSet<DBRef>();
            BuguDao dao = DaoCache.getInstance().get((Class)types[0]);
            for(BuguEntity entity : set){
                doCascade(dao, entity);
                result.add(BuguMapper.toDBRef(entity));
            }
            return result;
        }
        else if(DataType.isMap(type)){
            Map<Object, BuguEntity> map = (Map<Object, BuguEntity>)value;
            Map<Object, DBRef> result = new HashMap<Object, DBRef>();
            BuguDao dao = DaoCache.getInstance().get((Class)types[1]);
            for(Object key : map.keySet()){
                BuguEntity entity = map.get(key);
                doCascade(dao, entity);
                result.put(key, BuguMapper.toDBRef(entity));
            }
            return result;
        }
        else{
            return null;
        }
    }
    
    private void doCascade(BuguDao dao, BuguEntity entity){
        if(refList.cascade().toUpperCase().indexOf(Default.CASCADE_CREATE)!=-1 && entity.getId()==null){
            dao.insert(entity);
        }
        if(refList.cascade().toUpperCase().indexOf(Default.CASCADE_UPDATE)!=-1 && entity.getId()!=null){
            dao.save(entity);
        }
    }
    
}
