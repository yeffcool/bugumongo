package com.bugull.mongo.encoder;

import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.EmbedList;
import com.mongodb.DBObject;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class EmbedListEncoder extends AbstractEncoder{
    
    private final static Logger logger = Logger.getLogger(EmbedListEncoder.class);
    
    public EmbedListEncoder(Object obj, Field field){
        super(obj, field);
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

    @Override
    public Object encode() {
        List list = (List)value;
        List<DBObject> result = new LinkedList<DBObject>();
        for(Object o : list){
            result.add(new BuguMapper().toDBObject(o));
        }
        return result;
    }
    
}
