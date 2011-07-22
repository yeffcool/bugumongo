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

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.RefList;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefListEncoder extends AbstractEncoder{
    
    public RefListEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        RefList refList = field.getAnnotation(RefList.class);
        if(refList != null){
            String name = refList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        String typeName = field.getType().getName();
        if(typeName.equals("java.util.List")){
            List<BuguEntity> list = (List<BuguEntity>)value;
            List<DBRef> result = new LinkedList<DBRef>();
            BuguMapper mapper = new BuguMapper();
            for(BuguEntity entity : list){
                result.add(mapper.toDBRef(entity));
            }
            return result;
        }
        else if(typeName.equals("java.util.Set")){
            Set<BuguEntity> set = (Set<BuguEntity>)value;
            Set<DBRef> result = new HashSet<DBRef>();
            BuguMapper mapper = new BuguMapper();
            for(BuguEntity entity : set){
                result.add(mapper.toDBRef(entity));
            }
            return result;
        }
        return null;
    }
    
}
