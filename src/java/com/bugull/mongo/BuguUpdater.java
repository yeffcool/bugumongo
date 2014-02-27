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

import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.backend.EntityChangedListener;
import com.bugull.mongo.lucene.backend.IndexChecker;
import com.bugull.mongo.utils.IdUtil;
import com.bugull.mongo.utils.MapperUtil;
import com.bugull.mongo.utils.Operator;
import com.bugull.mongo.utils.ReferenceUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * There are so many update operation, so put it together here.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
@SuppressWarnings("unchecked")
public class BuguUpdater<T> {
    
    private DBCollection coll;
    private Class<T> clazz;
    private WriteConcern concern;
    private EntityChangedListener luceneListener;
    
    public BuguUpdater(DBCollection coll, Class<T> clazz, WriteConcern concern, EntityChangedListener luceneListener){
        this.coll = coll;
        this.clazz = clazz;
        this.concern = concern;
        this.luceneListener = luceneListener;
    }
    
    private T findOne(String id){
        DBObject dbo = new BasicDBObject();
        dbo.put(Operator.ID, IdUtil.toDbId(clazz, id));
        DBObject result = coll.findOne(dbo);
        return MapperUtil.fromDBObject(clazz, result);
    }
    
    private WriteResult updateOne(String id, DBObject dbo, String... keys){
        DBObject condition = new BasicDBObject(Operator.ID, IdUtil.toDbId(clazz, id));
        WriteResult wr = coll.update(condition, dbo, false, false, concern); //update one
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, keys)){
            BuguEntity entity = (BuguEntity)findOne(id);
            luceneListener.entityUpdate(entity);
        }
        return wr;
    }
    
    private WriteResult updateMulti(DBObject condition, DBObject dbo, String... keys){
        WriteResult wr = coll.update(condition, dbo, false, true, concern);  //update multi
        if(luceneListener != null && IndexChecker.hasIndexAnnotation(clazz, keys)){
            List ids = coll.distinct(Operator.ID, condition);
            for(Object id : ids){
                BuguEntity entity = (BuguEntity)findOne(id.toString());
                luceneListener.entityUpdate(entity);
            }
        }
        return wr;
    }
    
    private Object checkSingleValue(String key, Object value){
        Object result = value;
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            result = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedField(clazz, key)){
            result = MapperUtil.toDBObject(value);
        }
        return result;
    }
    
    private Object checkArrayValue(String key, Object value){
        Object result = value;
        if(value instanceof BuguEntity){
            BuguEntity be = (BuguEntity)value;
            result = ReferenceUtil.toDbReference(clazz, key, be.getClass(), be.getId());
        }else if(!(value instanceof DBObject) && 
                FieldsCache.getInstance().isEmbedListField(clazz, key)){
            result = MapperUtil.toDBObject(value);
        }
        return result;
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param query the query condition
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(BuguQuery query, String key, Object value){
        value = checkSingleValue(key, value);
        DBObject dbo = new BasicDBObject(key, value);
        DBObject set = new BasicDBObject(Operator.SET, dbo);
        return updateMulti(query.getCondition(), set, key);
    }
    
    /**
     * Update some entities, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param query the query condition
     * @param values
     * @return the new key/value pairs
     */
    public WriteResult set(BuguQuery query, Map values){
        DBObject dbo = new BasicDBObject(values);
        DBObject set = new BasicDBObject(Operator.SET, dbo);
        Set<String> keys = (Set<String>) values.keySet();
        return updateMulti(query.getCondition(), set, keys.toArray(new String[0]));
    }
    
    /**
     * Update a field's value of an entity.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return set(ent.getId(), key, value);
    }
    
    /**
     * Update a field's value of an entity.
     * Notice: EmbedList and RefList fields is not supported yet.
     * @param id the entity's id
     * @param key the field's name
     * @param value the field's new value
     * @return 
     */
    public WriteResult set(String id, String key, Object value){
        value = checkSingleValue(key, value);
        DBObject query = new BasicDBObject(key, value);
        DBObject set = new BasicDBObject(Operator.SET, query);
        return updateOne(id, set, key);
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param t the entity needs to be updated
     * @param values the new key/value pairs
     * @return 
     */
    public WriteResult set(T t, Map values){
        BuguEntity ent = (BuguEntity)t;
        return set(ent.getId(), values);
    }
    
    /**
     * Update an entity, with new key/value pairs.
     * Notice: the Map values must can be converted to DBObject.
     * @param id the entity's id
     * @param values the new key/value pairs
     * @return 
     */
    public WriteResult set(String id, Map values){
        DBObject set = new BasicDBObject(Operator.SET, new BasicDBObject(values));
        Set<String> keys = (Set<String>) values.keySet();
        return updateOne(id, set, keys.toArray(new String[0]));
    } 
    
    /**
     * Remove one or more filed(column) of an entity.
     * @param t the entity to operate
     * @param keys the field's name
     * @return 
     */
    public WriteResult unset(T t, String... keys){
        BuguEntity ent = (BuguEntity)t;
        return unset(ent.getId(), keys);
    }
    
    /**
     * Remove one or more filed(column) of an entity
     * @param id the entity's id
     * @param keys the field's name
     * @return 
     */
    public WriteResult unset(String id, String... keys){
        DBObject query = new BasicDBObject();
        for(String key : keys){
            query.put(key, 1);
        }
        DBObject unset = new BasicDBObject(Operator.UNSET, query);
        return updateOne(id, unset, keys);
    }
    
    /**
     * Remove one or more filed(column).
     * @param query mathcing conditon
     * @param keys the field's name
     * @return 
     */
    public WriteResult unset(BuguQuery query, String... keys){
        DBObject dbo = new BasicDBObject();
        for(String key : keys){
            dbo.put(key, 1);
        }
        DBObject unset = new BasicDBObject(Operator.UNSET, dbo);
        return updateMulti(query.getCondition(), unset, keys);
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return inc(ent.getId(), key, value);
    }
    
    /**
     * Increase a numeric field of an entity.
     * @param id the entity's id
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(String id, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject(Operator.INC, dbo);
        return updateOne(id, inc, key);
    }
    
    /**
     * Increase a numberic field of some entities.
     * @param query the query condition
     * @param key the field's name
     * @param value the numeric value to be added. It can be positive or negative integer, long, float, double.
     * @return 
     */
    public WriteResult inc(BuguQuery query, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject inc = new BasicDBObject(Operator.INC, dbo);
        return updateMulti(query.getCondition(), inc, key);
    }
    
    /**
     * Multiply the value of a field by a number. 
     * @param t the entity to update
     * @param key the field's name
     * @param value the numeric value to multiply
     * @return 
     */
    public WriteResult mul(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return mul(ent.getId(), key, value);
    }
    
    /**
     * Multiply the value of a field by a number. 
     * @param id the entity's id
     * @param key the field's name
     * @param value the numeric value to multiply
     * @return 
     */
    public WriteResult mul(String id, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject mul = new BasicDBObject(Operator.MUL, dbo);
        return updateOne(id, mul, key);
    }
    
    /**
     * Multiply the value of a field by a number. 
     * @param query the query condition
     * @param key the field's name
     * @param value the numeric value to multiply
     * @return 
     */
    public WriteResult mul(BuguQuery query, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject mul = new BasicDBObject(Operator.MUL, dbo);
        return updateMulti(query.getCondition(), mul, key);
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the element to add
     * @return 
     */
    public WriteResult push(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return push(ent.getId(), key, value);
    }
    
    /**
     * Add an element to an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to add
     * @return 
     */
    public WriteResult push(String id, String key, Object value){
        value = checkArrayValue(key, value);
        DBObject dbo = new BasicDBObject(key, value);
        DBObject push = new BasicDBObject(Operator.PUSH, dbo);
        return updateOne(id, push, key);
    }
    
    /**
     * Add an element to array/list/set field.
     * @param query the query condition
     * @param key the field's name
     * @param value the element to add
     * @return 
     */
    public WriteResult push(BuguQuery query, String key, Object value){
        value = checkArrayValue(key, value);
        DBObject dbo = new BasicDBObject(key, value);
        DBObject push = new BasicDBObject(Operator.PUSH, dbo);
        return updateMulti(query.getCondition(), push, key);
    }
    
    /**
     * Add each element in a list to the specified field.
     * @param t the entity to update
     * @param key the field's name
     * @param valueList the list contains each element
     * @return 
     */
    public WriteResult pushEach(T t, String key, List valueList){
        BuguEntity ent = (BuguEntity)t;
        return pushEach(ent.getId(), key, valueList);
    }
    
    /**
     * Add each element in a list to the specified field.
     * @param id the entity's id
     * @param key the field's name
     * @param valueList the list contains each element
     * @return 
     */
    public WriteResult pushEach(String id, String key, List valueList){
        int len = valueList.size();
        Object[] values = new Object[len];
        for(int i=0; i<len; i++){
            values[i] = checkArrayValue(key, valueList.get(i));
        }
        DBObject each = new BasicDBObject(Operator.EACH, values);
        DBObject dbo = new BasicDBObject(key, each);
        DBObject push = new BasicDBObject(Operator.PUSH, dbo);
        return updateOne(id, push, key);
    }
    
    /**
     * Add each element in a list to the specified field.
     * @param query the query condition
     * @param key the field's name
     * @param valueList the list contains each element
     * @return 
     */
    public WriteResult pushEach(BuguQuery query, String key, List valueList){
        int len = valueList.size();
        Object[] values = new Object[len];
        for(int i=0; i<len; i++){
            values[i] = checkArrayValue(key, valueList.get(i));
        }
        DBObject each = new BasicDBObject(Operator.EACH, values);
        DBObject dbo = new BasicDBObject(key, each);
        DBObject push = new BasicDBObject(Operator.PUSH, dbo);
        return updateMulti(query.getCondition(), push, key);
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the element to remove
     * @return 
     */
    public WriteResult pull(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return pull(ent.getId(), key, value);
    }
    
    /**
     * Remove an element of an entity's array/list/set field.
     * @param id the entity's id
     * @param key the field's name
     * @param value the element to remove
     * @return 
     */
    public WriteResult pull(String id, String key, Object value){
        value = checkArrayValue(key, value);
        DBObject dbo = new BasicDBObject(key, value);
        DBObject pull = new BasicDBObject(Operator.PULL, dbo);
        return updateOne(id, pull, key);
    }
    
    /**
     * Remove an element from array/list/set field.
     * @param query the query condition
     * @param key the field's name
     * @param value the element to remove
     * @return 
     */
    public WriteResult pull(BuguQuery query, String key, Object value){
        value = checkArrayValue(key, value);
        DBObject dbo = new BasicDBObject(key, value);
        DBObject pull = new BasicDBObject(Operator.PULL, dbo);
        return updateMulti(query.getCondition(), pull, key);
    }
    
    /**
     * Remove the first element from the array/list/set field
     * @param t the entity to update
     * @param key the field's name
     * @return 
     */
    public WriteResult popFirst(T t, String key){
        BuguEntity ent = (BuguEntity)t;
        return popFirst(ent.getId(), key);
    }
    
    /**
     * Remove the first element from the array/list/set field
     * @param id the entity's id
     * @param key the field's name
     * @return 
     */
    public WriteResult popFirst(String id, String key){
        DBObject dbo = new BasicDBObject(key, -1);
        DBObject pop = new BasicDBObject(Operator.POP, dbo);
        return updateOne(id, pop, key);
    }
    
    /**
     * Remove the first element from the array/list/set field
     * @param query the query condition
     * @param key the field's name
     * @return 
     */
    public WriteResult popFirst(BuguQuery query, String key){
        DBObject dbo = new BasicDBObject(key, -1);
        DBObject pop = new BasicDBObject(Operator.POP, dbo);
        return updateMulti(query.getCondition(), pop, key);
    }
    
    /**
     * Remove the last element from the array/list/set field
     * @param t the entity to update
     * @param key the field's name
     * @return 
     */
    public WriteResult popLast(T t, String key){
        BuguEntity ent = (BuguEntity)t;
        return popLast(ent.getId(), key);
    }
    
    /**
     * Remove the last element from the array/list/set field
     * @param id the entity's id
     * @param key the field's name
     * @return 
     */
    public WriteResult popLast(String id, String key){
        DBObject dbo = new BasicDBObject(key, 1);
        DBObject pop = new BasicDBObject(Operator.POP, dbo);
        return updateOne(id, pop, key);
    }
    
    /**
     * Remove the last element from the array/list/set field
     * @param query the query condition
     * @param key the field's name
     * @return 
     */
    public WriteResult popLast(BuguQuery query, String key){
        DBObject dbo = new BasicDBObject(key, 1);
        DBObject pop = new BasicDBObject(Operator.POP, dbo);
        return updateMulti(query.getCondition(), pop, key);
    }   
    
    /**
     * Update the value of the field to a specified value if the specified value is less than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult min(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return min(ent.getId(), key, value);
    }
    
    /**
     * Update the value of the field to a specified value if the specified value is less than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param id the entity's id
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult min(String id, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject min = new BasicDBObject(Operator.MIN, dbo);
        return updateOne(id, min, key);
    }
    
    /**
     * Update the value of the field to a specified value if the specified value is less than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param query the query conditon
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult min(BuguQuery query, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject min = new BasicDBObject(Operator.MIN, dbo);
        return updateMulti(query.getCondition(), min, key);
    }
    
    /**
     * updates the value of the field to a specified value if the specified value is greater than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param t the entity needs to update
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult max(T t, String key, Object value){
        BuguEntity ent = (BuguEntity)t;
        return max(ent.getId(), key, value);
    }
    
    /**
     * updates the value of the field to a specified value if the specified value is greater than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param id the entity's id
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult max(String id, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject max = new BasicDBObject(Operator.MAX, dbo);
        return updateOne(id, max, key);
    }
    
    /**
     * updates the value of the field to a specified value if the specified value is greater than the current value of the field.
     * If the field does not exists, this operation sets the field to the specified value. 
     * @param query the query condition
     * @param key the field's name
     * @param value the specified value
     * @return 
     */
    public WriteResult max(BuguQuery query, String key, Object value){
        DBObject dbo = new BasicDBObject(key, value);
        DBObject max = new BasicDBObject(Operator.MAX, dbo);
        return updateMulti(query.getCondition(), max, key);
    }
    
    /**
     * Performs a bitwise update of a field
     * @param t the entity to update
     * @param key the field's name
     * @param value the bitwise value
     * @param bitwise the enum type of bitwise operation: AND,OR,XOR
     * @return 
     */
    public WriteResult bitwise(T t, String key, int value, Bitwise bitwise){
        BuguEntity ent = (BuguEntity)t;
        return bitwise(ent.getId(), key, value, bitwise);
    }
    
    /**
     * Performs a bitwise update of a field
     * @param id the entity's id
     * @param key the field's name
     * @param value the bitwise value
     * @param bitwise the enum type of bitwise operation: AND,OR,XOR
     * @return 
     */
    public WriteResult bitwise(String id, String key, int value, Bitwise bitwise){
        DBObject logic = new BasicDBObject(checkBitwise(bitwise), value);
        DBObject dbo = new BasicDBObject(key, logic);
        DBObject bit = new BasicDBObject(Operator.BIT, dbo);
        return updateOne(id, bit, key);
    }
    
    /**
     * Performs a bitwise update of a field
     * @param query the query condition
     * @param key the field's name
     * @param value the bitwise value
     * @param bitwise the enum type of bitwise operation: AND,OR,XOR
     * @return 
     */
    public WriteResult bitwise(BuguQuery query, String key, int value, Bitwise bitwise){
        DBObject logic = new BasicDBObject(checkBitwise(bitwise), value);
        DBObject dbo = new BasicDBObject(key, logic);
        DBObject bit = new BasicDBObject(Operator.BIT, dbo);
        return updateMulti(query.getCondition(), bit, key);
    }
    
    private String checkBitwise(Bitwise bitwise){
        String result = null;
        switch(bitwise){
            case AND:
                result = "and";
                break;
            case OR:
                result = "or";
                break;
            case XOR:
                result = "xor";
                break;
            default:
                break;
        }
        return result;
    }
    
    public enum Bitwise { AND, OR, XOR }
    
}
