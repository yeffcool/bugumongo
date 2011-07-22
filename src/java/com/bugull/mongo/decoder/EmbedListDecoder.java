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

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.EmbedList;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedListDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(EmbedListDecoder.class);
    
    public EmbedListDecoder(Field field, DBObject dbo){
        super(field, dbo);
    }

    @Override
    public void decode(Object obj) {
        List list = (List)value;
        List result = new LinkedList();
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        Class clazz = (Class)types[0];
        BuguMapper mapper = new BuguMapper();
        for(Object o : list){
            Object embedObj = mapper.fromDBObject(clazz, (DBObject)o);
            result.add(embedObj);
        }
        String typeName = field.getType().getName();
        try{
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

    @Override
    public String getFieldName() {
        String fieldName = field.getName();
        EmbedList embedList = field.getAnnotation(EmbedList.class);
        if(embedList != null){
            String name = embedList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
