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

package com.bugull.mongo;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class AdvancedDao extends BuguDao{
    
    public AdvancedDao(Class<?> clazz){
        super(clazz);
    }
    
    public double max(String key){
        return max(key, null);
    }
    
    public double max(String key, DBObject query){
        StringBuilder map = new StringBuilder("function(){emit('");
        map.append(key);
        map.append("', {'value':this.");
        map.append(key);
        map.append("});}");
        String reduce = "function(key, values){var max=values[0].value; for(var i=1;i<values.length; i++){if(values[i].value>max){max=values[i].value;}} return {'value':max}}";
        Iterable<DBObject> results = mapReduce(map.toString(), reduce, query);
        DBObject result = results.iterator().next();
        DBObject dbo = (DBObject)result.get("value");
        return Double.parseDouble(dbo.get("value").toString());
    }
    
    public double min(String key){
        return min(key, null);
    }
    
    public double min(String key, DBObject query){
        StringBuilder map = new StringBuilder("function(){emit('");
        map.append(key);
        map.append("', {'value':this.");
        map.append(key);
        map.append("});}");
        String reduce = "function(key, values){var min=values[0].value; for(var i=1;i<values.length; i++){if(values[i].value<min){min=values[i].value;}} return {'value':min}}";
        Iterable<DBObject> results = mapReduce(map.toString(), reduce, query);
        DBObject result = results.iterator().next();
        DBObject dbo = (DBObject)result.get("value");
        return Double.parseDouble(dbo.get("value").toString());
    }
    
    public double sum(String key){
        return sum(key, null);
    }
    
    public double sum(String key, DBObject query){
        StringBuilder map = new StringBuilder("function(){emit('");
        map.append(key);
        map.append("', {'value':this.");
        map.append(key);
        map.append("});}");
        String reduce = "function(key, values){var sum=0; for(var i=0;i<values.length; i++){sum+=values[i].value;} return {'value':sum}}";
        Iterable<DBObject> results = mapReduce(map.toString(), reduce, query);
        DBObject result = results.iterator().next();
        DBObject dbo = (DBObject)result.get("value");
        return Double.parseDouble(dbo.get("value").toString());
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce) {
        return coll.mapReduce(map, reduce, null, OutputType.INLINE, null).results();
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce, DBObject query) {
        return coll.mapReduce(map, reduce, null, OutputType.INLINE, query).results();
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject orderBy, DBObject query) {
        synchronized(outputTarget){
            MapReduceOutput output = coll.mapReduce(map, reduce, outputTarget, outputType, query);
            DBCollection c = output.getOutputCollection();
            DBCursor cursor = null;
            if(orderBy != null){
                cursor = c.find().sort(orderBy);
            }else{
                cursor = c.find();
            }
            List<DBObject> list = new ArrayList<DBObject>();
            for(Iterator<DBObject> it = cursor.iterator(); it.hasNext(); ){
                list.add(it.next());
            }
            return list;
        }
    }
    
    public Iterable<DBObject> mapReduce(String map, String reduce, String outputTarget, MapReduceCommand.OutputType outputType, DBObject orderBy, int pageNum, int pageSize, DBObject query) {
        synchronized(outputTarget){
            MapReduceOutput output = coll.mapReduce(map, reduce, outputTarget, outputType, query);
            DBCollection c = output.getOutputCollection();
            DBCursor cursor = null;
            if(orderBy != null){
                cursor = c.find().sort(orderBy).skip((pageNum-1)*pageSize).limit(pageSize);
            }else{
                cursor = c.find().skip((pageNum-1)*pageSize).limit(pageSize);
            }
            List<DBObject> list = new ArrayList<DBObject>();
            for(Iterator<DBObject> it = cursor.iterator(); it.hasNext(); ){
                list.add(it.next());
            }
            return list;
        }
    }
    
}
