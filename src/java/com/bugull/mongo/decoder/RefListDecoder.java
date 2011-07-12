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
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

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
    }
    
    @Override
    public void decode(Object obj){
        List<DBRef> list = (List<DBRef>)value;
        List result = new LinkedList();
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        Class clazz = (Class)types[0];
        if(refList.lazy()){
            BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
            for(DBRef dbRef : list){
                refObj.setId(dbRef.getId().toString());
                result.add(refObj);
            }
        }else{
            BuguDao dao = new BuguDao(clazz);
            for(DBRef dbRef : list){
                BuguEntity refObj = (BuguEntity)dao.findOne(dbRef.getId().toString());
                result.add(refObj);
            }
        }
        try{
            field.set(obj, result);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        if(refList != null){
            String name = refList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
