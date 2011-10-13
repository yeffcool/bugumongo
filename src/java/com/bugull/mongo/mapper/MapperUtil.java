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

import com.bugull.mongo.annotations.Entity;
import com.bugull.mongo.cache.ConstructorCache;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.decoder.Decoder;
import com.bugull.mongo.decoder.DecoderFactory;
import com.bugull.mongo.encoder.Encoder;
import com.bugull.mongo.encoder.EncoderFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class MapperUtil {
    
    public final static String ID = "_id";
    
    public static Object fromDBObject(Class<?> clazz, DBObject dbo){
        if(dbo == null){
            return null;
        }
        Object obj = ConstructorCache.getInstance().create(clazz);
        Field[] fields = FieldsCache.getInstance().get(clazz);
        for(Field field : fields){
            Decoder decoder = DecoderFactory.create(field, dbo);
            if(decoder!=null && !decoder.isNullField()){
                decoder.decode(obj);
            }
        }
        return obj;
    }
    
    public static DBObject toDBObject(Object obj){
        if(obj == null){
            return null;
        }
        Class<?> clazz = obj.getClass();
        Field[] fields = FieldsCache.getInstance().get(clazz);
        DBObject dbo = new BasicDBObject();
        for(Field field : fields){
            Encoder encoder = EncoderFactory.create(obj, field);
            if(encoder!=null && !encoder.isNullField()){
                dbo.put(encoder.getFieldName(), encoder.encode());
            }
        }
        return dbo;
    }
    
    public static List toList(Class<?> clazz, DBCursor cursor){
        List list = new ArrayList();
        while(cursor.hasNext()){
            DBObject dbo = cursor.next();
            list.add(fromDBObject(clazz, dbo));
        }
        cursor.close();
        return list;
    }
    
    public static DBObject getSort(String orderBy){
        DBObject sort = new BasicDBObject();
        orderBy = orderBy.replaceAll("[{}'']", "");
        String[] arr = orderBy.split(",");
        for(String s : arr){
            String[] kv = s.split(":");
            String k = kv[0].trim();
            String v = kv[1].trim();
            sort.put(k, Integer.parseInt(v));
        }
        return sort;
    }
    
    public static DBObject[] getIndex(String index){
        index = index.replaceAll("\\}[^{^}]+\\{", "};{");
        index = index.replaceAll("[{}'']", "");
        String[] items = index.split(";");
        int len = items.length;
        DBObject[] result = new BasicDBObject[len];
        for(int i=0; i<len; i++){
            DBObject dbo = new BasicDBObject();
            String[] arr = items[i].split(",");
            for(String s : arr){
                String[] kv = s.split(":");
                String k = kv[0].trim();
                String v = kv[1].trim();
                if(v.equalsIgnoreCase("2d")){
                    dbo.put(k, v);
                }else{
                    dbo.put(k, Integer.parseInt(v));
                }
            }
            result[i] = dbo;
        }
        return result;
    }
    
    public static String getEntityName(Class<?> clazz){
        Entity entity = clazz.getAnnotation(Entity.class);
        String name = entity.name();
        if(name.equals("")){
            name = clazz.getSimpleName().toLowerCase();
        }
        return name;
    }
    
}
