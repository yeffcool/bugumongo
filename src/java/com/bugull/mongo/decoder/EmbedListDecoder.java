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

import com.bugull.mongo.annotations.EmbedList;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.MapperUtil;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedListDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(EmbedListDecoder.class);
    
    public EmbedListDecoder(Field field, DBObject dbo){
        super(field, dbo);
        String fieldName = field.getName();
        EmbedList embedList = field.getAnnotation(EmbedList.class);
        String name = embedList.name();
        if(!name.equals("")){
            fieldName = name;
        }
        value = dbo.get(fieldName);
    }

    @Override
    public void decode(Object obj) {
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        int len = types.length;
        if(len == 1){
            List list = (List)value;
            List result = new ArrayList();
            Class clazz = (Class)types[0];
            for(Object o : list){
                Object embedObj = MapperUtil.fromDBObject(clazz, (DBObject)o);
                result.add(embedObj);
            }
            String typeName = field.getType().getName();
            try{
                if(DataType.isList(typeName)){
                    field.set(obj, result);
                }
                else if(DataType.isSet(typeName)){
                    field.set(obj, new HashSet(result));
                }
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        else if(len == 2){
            Map map = (Map)value;
            Map result = new HashMap();
            Class clazz = (Class)types[1];
            for(Object key : map.keySet()){
                Object val = map.get(key);
                Object embedObj = MapperUtil.fromDBObject(clazz, (DBObject)val);
                result.put(key, embedObj);
            }
            try{
                field.set(obj, result);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
}
