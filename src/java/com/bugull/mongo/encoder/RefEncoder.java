package com.bugull.mongo.encoder;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.Ref;
import java.lang.reflect.Field;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefEncoder extends AbstractEncoder{
    
    public RefEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        Ref ref = field.getAnnotation(Ref.class);
        if(ref != null){
            String name = ref.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        BuguEntity entity = (BuguEntity)value;
        return new BuguMapper().toDBRef(entity);
    }
    
}
