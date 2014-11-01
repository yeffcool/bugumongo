/*
 * Copyright (c) www.bugull.com
 * 
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

import com.bugull.mongo.exception.AggregationException;
import com.bugull.mongo.utils.Aggregation;
import com.bugull.mongo.utils.MapperUtil;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Convenient class for creating aggregating operation.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguAggregation<T> {
    
    private DBCollection coll;
    private List<DBObject> stages;
    
    public BuguAggregation(DBCollection coll){
        this.coll = coll;
        stages = new ArrayList<DBObject>();
    }
    
    public BuguAggregation addStage(DBObject stage){
        stages.add(stage);
        return this;
    }
    
    public Iterable<DBObject> results() throws AggregationException {
        int size = stages.size();
        if(size <= 0){
            throw new AggregationException("Empty stage in aggregation pipeline!");
        }
        AggregationOutput output = coll.aggregate(stages);
        CommandResult cr = output.getCommandResult();
        if(! cr.ok()){
            throw new AggregationException(cr.getErrorMessage());
        }
        return output.results();
    }
    
    public static class Pipeline {
        
        public static DBObject project(DBObject dbo){
            return new BasicDBObject(Aggregation.PROJECT, dbo);
        }
        
        public static DBObject project(String fields){
            return new BasicDBObject(Aggregation.PROJECT, MapperUtil.getSort(fields));
        }

        public static DBObject match(DBObject dbo){
            return new BasicDBObject(Aggregation.MATCH, dbo);
        }
        
        public static DBObject match(String key, Object value){
            return new BasicDBObject(Aggregation.MATCH, new BasicDBObject(key, value));
        }
        
        public static DBObject match(BuguQuery query){
            return new BasicDBObject(Aggregation.MATCH, query.getCondition());
        }

        public static DBObject limit(int n){
            return new BasicDBObject(Aggregation.LIMIT, n);
        }

        public static DBObject skip(int n){
            return new BasicDBObject(Aggregation.SKIP, n);
        }
        
        public static DBObject sort(String orderBy){
            return new BasicDBObject(Aggregation.SORT, MapperUtil.getSort(orderBy));
        }

        public static DBObject unwind(String field){
            return new BasicDBObject(Aggregation.UNWIND, field);
        }

        public static DBObject group(DBObject dbo){
            return new BasicDBObject(Aggregation.GROUP, dbo);
        }
        
    }

}
