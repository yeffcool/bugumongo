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

package com.bugull.mongo.lucene.handler;

import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.mapper.DataType;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class PropertyFieldHandler extends AbstractFieldHandler{
    
    public PropertyFieldHandler(Object obj, java.lang.reflect.Field field, String prefix){
        super(obj, field, prefix);
    }

    @Override
    public void handle(Document doc) throws Exception{
        IndexProperty ip = field.getAnnotation(IndexProperty.class);
        process(doc, ip.analyze(), ip.store(), ip.boost());
    }
    
    protected void process(Document doc, boolean analyze, boolean store, float boost) throws Exception {
        Class<?> type = field.getType();
        if(type.isArray()){
            processArray(doc, analyze, store, boost);
        }else{
            processPrimitive(doc, analyze, store, boost);
        }
    }
    
    private void processArray(Document doc, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = field.getType();
        String typeName = type.getComponentType().getName();
        String fieldName = prefix + field.getName();
        Field f = new Field(fieldName, getArrayString(field.get(obj), typeName),
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        f.setBoost(boost);
        doc.add(f);
    }
    
    private void processPrimitive(Document doc, boolean analyze, boolean store, float boost) throws Exception{
        Class<?> type = field.getType();
        String fieldName = prefix + field.getName();
        String typeName = type.getName();
        Fieldable f = null;
        if(DataType.isString(typeName)){
            String fieldValue = field.get(obj).toString();
            f = new Field(fieldName, fieldValue,
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isBoolean(typeName)){
            String fieldValue = field.getBoolean(obj) ? "true" : "false";
            f = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isChar(typeName)){
            String fieldValue = String.valueOf(field.getChar(obj));
            f = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
        }
        else if(DataType.isInteger(typeName)){
            f = new NumericField(fieldName).setIntValue(field.getInt(obj));
        }
        else if(DataType.isLong(typeName)){
            f = new NumericField(fieldName).setLongValue(field.getLong(obj));
        }
        else if(DataType.isShort(typeName)){
            f = new NumericField(fieldName).setIntValue(field.getShort(obj));
        }
        else if(DataType.isFloat(typeName)){
            f = new NumericField(fieldName).setFloatValue(field.getFloat(obj));
        }
        else if(DataType.isDouble(typeName)){
            f = new NumericField(fieldName).setDoubleValue(field.getDouble(obj));
        }
        else if(DataType.isDate(typeName)){
            Date date = (Date)field.get(obj);
            f = new NumericField(fieldName).setLongValue(date.getTime());
        }
        else if(DataType.isTimestamp(typeName)){
            Timestamp ts = (Timestamp)field.get(obj);
            f = new NumericField(fieldName).setLongValue(ts.getTime());
        }
        else if(DataType.isSet(typeName) || DataType.isList(typeName)){
            Collection coll = (Collection)field.get(obj);
            StringBuilder sb = new StringBuilder();
            for(Object o : coll){
                sb.append(o).append(JOIN);
            }
            f = new Field(fieldName, sb.toString(),
                    store ? Field.Store.YES : Field.Store.NO,
                    analyze ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
        }
        f.setBoost(boost);
        doc.add(f);
    }
    
}
