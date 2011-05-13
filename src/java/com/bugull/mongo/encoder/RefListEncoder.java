package com.bugull.mongo.encoder;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguMapper;
import com.bugull.mongo.annotations.RefList;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefListEncoder extends AbstractEncoder{
    
    public RefListEncoder(Object obj, Field field){
        super(obj, field);
    }
    
    @Override
    public String getFieldName(){
        String fieldName = field.getName();
        RefList refList = field.getAnnotation(RefList.class);
        if(refList != null){
            String name = refList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
    @Override
    public Object encode(){
        List<BuguEntity> list = (List<BuguEntity>)value;
        List<DBRef> result = new LinkedList<DBRef>();
        for(BuguEntity entity : list){
            result.add(new BuguMapper().toDBRef(entity));
        }
        return result;
    }
    
}
