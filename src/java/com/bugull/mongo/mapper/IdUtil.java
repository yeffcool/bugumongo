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

package com.bugull.mongo.mapper;

import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.cache.FieldsCache;
import java.lang.reflect.Field;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IdUtil {
    
    /**
     * Convert the id string to object, which matching the id data in mongoDB.
     * @param clazz
     * @param id
     * @return 
     */
    public static Object toDbId(Class<?> clazz, String id){
        if(StringUtil.isEmpty(id)){
            return null;
        }
        Object result = null;
        Field idField = FieldsCache.getInstance().getIdField(clazz);
        Id idAnnotation = idField.getAnnotation(Id.class);
        switch(idAnnotation.type()){
            case AUTO_GENERATE:
                result = new ObjectId(id);
                break;
            case AUTO_INCREASE:
                result = Long.parseLong(id);
                break;
            case USER_DEFINE:
                result = id;
                break;
        }
        return result;
    }

}
