package com.bugull.mongo.lucene.backend;

import com.bugull.mongo.BuguDao;
import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.annotations.Id;
import com.bugull.mongo.cache.FieldsCache;
import com.bugull.mongo.lucene.annotations.IndexEmbed;
import com.bugull.mongo.lucene.annotations.IndexProperty;
import com.bugull.mongo.lucene.annotations.IndexRef;
import java.util.Date;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class IndexCreater {
    
    private final static Logger logger = Logger.getLogger(IndexCreater.class);
    
    private Object obj;
    private String prefix;
    
    public IndexCreater(Object obj){
        this.obj = obj;
        prefix = "";
    }
    
    public IndexCreater(Object obj, String prefix){
        this.obj = obj;
        this.prefix = prefix + ".";
    }
    
    public void process(Document doc){
        process(doc, null);
    }
    
    public void process(Document doc, String id){
        Class<?> clazz = obj.getClass();
        java.lang.reflect.Field[] fields = FieldsCache.getInstance().get(clazz);
        for(java.lang.reflect.Field f : fields){
            try{
                processField(doc, f, id);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }
    
    private void processField(Document doc, java.lang.reflect.Field f, String id) throws Exception{
        if(id != null && f.getAnnotation(Id.class) != null){
            doc.add(new Field(f.getName(), id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        }
        else if(f.getAnnotation(IndexProperty.class) != null){
            processIndexProperty(doc, f);
        }
        else if(f.getAnnotation(IndexEmbed.class) != null){
            processIndexEmbed(doc, f);
        }
        else if(f.getAnnotation(IndexRef.class) != null){
            processIndexRef(doc, f);
        }
    }
    
    private void processIndexProperty(Document doc, java.lang.reflect.Field f) throws Exception{
        Class<?> type = f.getType();
        String fieldName = prefix + f.getName();
        String typeName = type.getName();
        if(typeName.equals("java.lang.String")){
            IndexProperty ip = f.getAnnotation(IndexProperty.class);
            String fieldValue = f.get(obj).toString();
            Field field = new Field(fieldName, fieldValue,
                    ip.store() ? Field.Store.YES : Field.Store.NO,
                    ip.analyze() ? Field.Index.ANALYZED : Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("boolean") || typeName.equals("java.lang.Boolean")){
            String fieldValue = f.getBoolean(obj) ? "true" : "false";
            Field field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("char") || typeName.equals("java.lang.Character")){
            String fieldValue = String.valueOf(f.getChar(obj));
            Field field = new Field(fieldName, fieldValue, Field.Store.NO, Field.Index.NOT_ANALYZED);
            doc.add(field);
        }
        else if(typeName.equals("int") || typeName.equals("java.lang.Integer")){
            NumericField field = new NumericField(fieldName).setIntValue(f.getInt(obj));
            doc.add(field);
        }
        else if(typeName.equals("long") || typeName.equals("java.lang.Long")){
            NumericField field = new NumericField(fieldName).setLongValue(f.getLong(obj));
            doc.add(field);
        }
        else if(typeName.equals("float") || typeName.equals("java.lang.Float")){
            NumericField field = new NumericField(fieldName).setFloatValue(f.getFloat(obj));
            doc.add(field);
        }
        else if(typeName.equals("double") || typeName.equals("java.lang.Double")){
            NumericField field = new NumericField(fieldName).setDoubleValue(f.getDouble(obj));
            doc.add(field);
        }
        else if(typeName.equals("java.util.Date")){
            Date date = (Date)f.get(obj);
            NumericField field = new NumericField(fieldName).setLongValue(date.getTime());
            doc.add(field);
        }
    }
    
    private void processIndexEmbed(Document doc, java.lang.reflect.Field f) throws Exception{
        Object embedObj = f.get(obj);
        String fieldName = prefix + f.getName();
        IndexCreater creater = new IndexCreater(embedObj, fieldName);
        creater.process(doc);
    }
    
    private void processIndexRef(Document doc, java.lang.reflect.Field f) throws Exception{
        BuguEntity entity = (BuguEntity)f.get(obj);
        String refId = entity.getId();
        BuguDao buguDao = new BuguDao(f.getType());
        Object refObj = buguDao.findOne(refId);
        String fieldName = prefix + f.getName();
        IndexCreater creater = new IndexCreater(refObj, fieldName);
        creater.process(doc);
    }
    
}
