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

import com.bugull.mongo.annotations.Default;
import com.bugull.mongo.annotations.EmbedList;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.FieldUtil;
import com.bugull.mongo.mapper.MapperUtil;
import com.mongodb.DBObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedListDecoder extends AbstractDecoder{
    
    public EmbedListDecoder(Field field, DBObject dbo){
        super(field, dbo);
        String fieldName = field.getName();
        EmbedList embedList = field.getAnnotation(EmbedList.class);
        String name = embedList.name();
        if(!name.equals(Default.NAME)){
            fieldName = name;
        }
        value = dbo.get(fieldName);
    }

    @Override
    public void decode(Object obj) {
        Class<?> type = field.getType();
        if(type.isArray()){
            decodeArray(obj, type.getComponentType());
        }else{
            ParameterizedType paramType = (ParameterizedType)field.getGenericType();
            Type[] types = paramType.getActualTypeArguments();
            int len = types.length;
            if(len == 1){
                decodeList(obj, (Class)types[0]);
            }else{
                decodeMap(obj, (Class)types[1]);
            }
        }
    }
    
    private void decodeArray(Object obj, Class clazz){
        List list = (ArrayList)value;
        int size = list.size();
        Object arr = Array.newInstance(clazz, size);
        for(int i=0; i<size; i++){
            DBObject o = (DBObject)list.get(i);
            Array.set(arr, i, MapperUtil.fromDBObject(clazz, o));
        }
        FieldUtil.set(obj, field, arr);
    }
    
    private void decodeList(Object obj, Class clazz){
        List list = (ArrayList)value;
        List result = new ArrayList();
        for(Object o : list){
            Object embedObj = MapperUtil.fromDBObject(clazz, (DBObject)o);
            result.add(embedObj);
        }
        Class type = field.getType();
        if(DataType.isList(type)){
            FieldUtil.set(obj, field, result);
        }
        else if(DataType.isSet(type)){
            FieldUtil.set(obj, field, new HashSet(result));
        }
    }
    
    private void decodeMap(Object obj, Class clazz){
        Map map = (Map)value;
        Map result = new HashMap();
        for(Object key : map.keySet()){
            Object val = map.get(key);
            Object embedObj = MapperUtil.fromDBObject(clazz, (DBObject)val);
            result.put(key, embedObj);
        }
        FieldUtil.set(obj, field, result);
    }
    
}
