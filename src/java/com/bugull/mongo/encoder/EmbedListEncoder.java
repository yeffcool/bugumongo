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

import com.bugull.mongo.annotations.EmbedList;
import com.bugull.mongo.mapper.DataType;
import com.bugull.mongo.mapper.MapperUtil;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
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
public class EmbedListEncoder extends AbstractEncoder{
    
    public EmbedListEncoder(Object obj, Field field){
        super(obj, field);
    }

    @Override
    public String getFieldName() {
        String fieldName = field.getName();
        EmbedList embedList = field.getAnnotation(EmbedList.class);
        String name = embedList.name();
        if(!name.equals("")){
            fieldName = name;
        }
        return fieldName;
    }

    @Override
    public Object encode() {
        String typeName = field.getType().getName();
        if(DataType.isList(typeName)){
            List list = (List)value;
            List<DBObject> result = new ArrayList<DBObject>();
            for(Object o : list){
                result.add(MapperUtil.toDBObject(o));
            }
            return result;
        }
        else if(DataType.isSet(typeName)){
            Set set = (Set)value;
            Set<DBObject> result = new HashSet<DBObject>();
            for(Object o : set){
                result.add(MapperUtil.toDBObject(o));
            }
            return result;
        }
        else if(DataType.isMap(typeName)){
            Map map = (Map)value;
            Map result = new HashMap();
            for(Object key : map.keySet()){
                result.put(key, MapperUtil.toDBObject(map.get(key)));
            }
            return result;
        }
        return null;
    }
    
}
