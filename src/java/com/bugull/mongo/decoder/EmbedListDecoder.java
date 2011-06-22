package com.bugull.mongo.decoder;

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.EmbedList;
import com.mongodb.DBObject;
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
public class EmbedListDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(EmbedListDecoder.class);
    
    public EmbedListDecoder(Field field, DBObject dbo){
        super(field, dbo);
    }

    @Override
    public void decode(Object obj) {
        List list = (List)value;
        List result = new LinkedList();
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        Class clazz = (Class)types[0];
        for(Object o : list){
            Object embedObj = new BuguMapper().fromDBObject(clazz, (DBObject)o);
            result.add(embedObj);
        }
        try{
            field.set(obj, result);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }

    @Override
    public String getFieldName() {
        String fieldName = field.getName();
        EmbedList embedList = field.getAnnotation(EmbedList.class);
        if(embedList != null){
            String name = embedList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
