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

import com.bugull.mongo.mapper.MapperUtil;
import java.lang.reflect.Field;
import org.bson.types.ObjectId;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IdEncoder extends AbstractEncoder{
    
    public IdEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public boolean isNullField(){
        return false;
    }
    
    @Override
    public String getFieldName(){
        return MapperUtil.ID;
    }
    
    @Override
    public Object encode(){
        if(value == null){
            return new ObjectId();
        }else{
            return new ObjectId(value.toString());
        }
    }
    
}
