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

import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.exception.DBQueryException;
import com.bugull.mongo.mapper.IdUtil;
import com.bugull.mongo.mapper.MapperUtil;
import com.bugull.mongo.mapper.Operator;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

/**
 * Convenient class for creating DBObject queries.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class BuguQuery<T> {
    
    private final static Logger logger = Logger.getLogger(BuguQuery.class);
    
    private DBCollection coll;
    private Class<T> clazz;
    private DBObject keys;
    
    private String orderBy;
    private DBObject condition;
    private int pageNumber = 0;  //default value is zero
    private int pageSize = 0;  //default value is zero
    
    public BuguQuery(DBCollection coll, Class<T> clazz, DBObject keys){
        this.coll = coll;
        this.clazz = clazz;
        this.keys = keys;
        condition = new BasicDBObject();
    }
    
    private void appendEquals(String key, String op, Object value){
        if(key.equals(Operator.ID)){
            Object dbId = IdUtil.toDbId(clazz, (String)value);
            append(key, op, dbId);
        }
        else if(key.indexOf(".")!=-1){
            append(key, op, value);
        }
        else{
            Field f = FieldsCache.getInstance().getField(clazz, key);
            if(f.getAnnotation(Id.class) != null){
                Object dbId = IdUtil.toDbId(clazz, (String)value);
                append(Operator.ID, op, dbId);
            }
            else if(value instanceof BuguEntity){
                append(key, op, BuguMapper.toDBRef((BuguEntity)value));
            }
            else{
                append(key, op, value);
            }
        }
    }
    
    private List<Object> toIds(Object... values){
        List<Object> idList = new ArrayList<Object>();
        int len = values.length;
        for(int i=0; i<len; i++){
            if(values[i] != null){
                Object dbId = IdUtil.toDbId(clazz, (String)values[i]);
                idList.add(dbId);
            }
        }
        return idList;
    }
    
    private List<DBRef> toDBRefs(Object... values){
        List<DBRef> refList = new ArrayList<DBRef>();
        int len = values.length;
        for(int i=0; i<len; i++){
            if(values[i] != null){
                refList.add(BuguMapper.toDBRef((BuguEntity)values[i]));
            }
        }
        return refList;
    }
    
    private void appendIn(String key, String op, Object... values){
        if(key.equals(Operator.ID)){
            append(key, op, toIds(values));
        }
        else if(key.indexOf(".")!=-1){
            append(key, op, values);
        }
        else{
            Field f = FieldsCache.getInstance().getField(clazz, key);
            if(f.getAnnotation(Id.class) != null){
                append(Operator.ID, op, toIds(values));
            }
            else if(values[0] instanceof BuguEntity){
                append(key, op, toDBRefs(values));
            }
            else{
                append(key, op, values);
            }
        }
    }
    
    private void append(String key, String op, Object value){
        if(op == null) {
            condition.put(key, value);
            return;
        }
        Object obj = condition.get(key);
        DBObject dbo = null;
        if(!(obj instanceof DBObject)) {
            dbo = new BasicDBObject(op, value);
            condition.put(key, dbo);
        } else {
            dbo = (DBObject)condition.get(key);
            dbo.put(op, value);
        }
    }
    
    public BuguQuery<T> is(String key, Object value){
        appendEquals(key, null, value);
        return this;
    }
    
    public BuguQuery<T> notEquals(String key, Object value){
        appendEquals(key, Operator.NE, value);
        return this;
    }
    
    public BuguQuery<T> or(BuguQuery... qs){
        List list = (List)condition.get(Operator.OR);
        if(list == null){
            list = new ArrayList();
            condition.put(Operator.OR, list);
        }
        for(BuguQuery q : qs){
            list.add(q.getCondition());
        }
        return this;
    }
    
    public BuguQuery<T> and(BuguQuery... qs){
        List list = (List)condition.get(Operator.AND);
        if(list == null){
            list = new ArrayList();
            condition.put(Operator.AND, list);
        }
        for(BuguQuery q : qs){
            list.add(q.getCondition());
        }
        return this;
    }
    
    public BuguQuery<T> greaterThan(String key, Object value){
        append(key, Operator.GT, value);
        return this;
    }
    
    public BuguQuery<T> greaterThanEquals(String key, Object value){
        append(key, Operator.GTE, value);
        return this;
    }
    
    public BuguQuery<T> lessThan(String key, Object value){
        append(key, Operator.LT, value);
        return this;
    }
    
    public BuguQuery<T> lessThanEquals(String key, Object value){
        append(key, Operator.LTE, value);
        return this;
    }
    
    public BuguQuery<T> in(String key, Object... values){
        appendIn(key, Operator.IN, values);
        return this;
    }
    
    public BuguQuery<T> notIn(String key, Object... values){
        appendIn(key, Operator.NIN, values);
        return this;
    }
    
    public BuguQuery<T> all(String key, Object... values){
        append(key, Operator.ALL, values);
        return this;
    }
    
    /**
     * Note: the regex string must in Java style, not JavaScript style.
     * @param key
     * @param regex
     * @return 
     */
    public BuguQuery<T> regex(String key, String regex){
        append(key, null, Pattern.compile(regex));
        return this;
    }
    
    public BuguQuery<T> size(String key, int value){
        append(key, Operator.SIZE, value);
        return this;
    }
    
    public BuguQuery<T> mod(String key, int divisor, int remainder){
        append(key, Operator.MOD, new int[]{divisor, remainder});
        return this;
    }
    
    public BuguQuery<T> existsField(String key){
        append(key, Operator.EXISTS, Boolean.TRUE);
        return this;
    }
    
    public BuguQuery<T> notExistsField(String key){
        append(key, Operator.EXISTS, Boolean.FALSE);
        return this;
    }
    
    public BuguQuery<T> withinCenter(String key, double x, double y, double radius){
        DBObject dbo = new BasicDBObject(Operator.CENTER, new Object[]{new Double[]{x, y}, radius});
        append(key, Operator.WITHIN, dbo);
        return this;
    }
    
    public BuguQuery<T> withinBox(String key, double x1, double y1, double x2, double y2){
        DBObject dbo = new BasicDBObject(Operator.BOX, new Object[]{new Double[]{x1, y1}, new Double[]{x2, y2} });
    	append(key, Operator.WITHIN, dbo);
    	return this;
    }
    
    public BuguQuery<T> near(String key, double x, double y){
        append(key, Operator.NEAR, new Double[]{x, y});
        return this;
    }

    public BuguQuery<T> near(String key, double x, double y, double maxDistance){
        append(key, Operator.NEAR, new Double[]{x, y, maxDistance});
        return this;
    }
    
    public BuguQuery<T> sort(String orderBy){
        this.orderBy = orderBy;
        return this;
    }
    
    public BuguQuery<T> pageNumber(int pageNumber){
        this.pageNumber = pageNumber;
        return this;
    }
    
    public BuguQuery<T> pageSize(int pageSize){
        this.pageSize = pageSize;
        return this;
    }
    
    private void checkSingle() throws DBQueryException{
        if(orderBy!=null || pageNumber!=0 || pageSize!=0){
            throw new DBQueryException("You should use results() to get a list, when you use sorting or pagination");
        }
    }
    
    public T result(){
        try{
            checkSingle();
        }catch(DBQueryException ex){
            logger.error(ex.getMessage(), ex);
        }
        DBObject dbo = coll.findOne(condition);
        return MapperUtil.fromDBObject(clazz, dbo);
    }
    
    public List<T> results(){
        DBCursor cursor = coll.find(condition, keys);
        if(orderBy != null){
            cursor.sort(MapperUtil.getSort(orderBy));
        }
        if(pageNumber>0 && pageSize>0){
            cursor.skip((pageNumber-1)*pageSize).limit(pageSize);
        }
        return MapperUtil.toList(clazz, cursor);
    }
    
    public long count(){
        return coll.count(condition);
    }
    
    public boolean exists(){
        DBObject dbo = coll.findOne(condition);
        return dbo != null;
    }
    
    public List distinct(String key){
        return coll.distinct(key, condition);
    }

    public DBObject getCondition() {
        return condition;
    }
    
}
