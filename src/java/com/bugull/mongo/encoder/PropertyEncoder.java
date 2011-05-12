package com.bugull.mongo.encoder;

import com.bugull.mongo.annotations.Property;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class PropertyEncoder extends AbstractEncoder{
    
    public PropertyEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        Property property = field.getAnnotation(Property.class);
        if(property != null){
            String name = property.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        return value;
    }
    
}
