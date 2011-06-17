package com.bugull.mongo.decoder;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguDao;
import com.bugull.mongo.annotations.RefList;
import com.bugull.mongo.cache.ConstructorCache;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
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
public class RefListDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(RefListDecoder.class);
    
    private RefList refList;
    
    public RefListDecoder(Field field, DBObject dbo){
        super(field, dbo);
        refList = field.getAnnotation(RefList.class);
    }
    
    @Override
    public void decode(Object obj){
        Object o = dbo.get(getFieldName());
        if(o == null){
            try{
                field.set(obj, null);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
            return;
        }
        List<DBRef> list = (List<DBRef>)o;
        List result = new LinkedList();
        ParameterizedType type = (ParameterizedType)field.getGenericType();
        Type[] types = type.getActualTypeArguments();
        Class clazz = (Class)types[0];
        if(refList.lazy()){
            for(DBRef dbRef : list){
                BuguEntity refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
                refObj.setId(dbRef.getId().toString());
                result.add(refObj);
            } 
        }else{
            for(DBRef dbRef : list){
                BuguDao dao = new BuguDao(clazz);
                BuguEntity refObj = (BuguEntity)dao.findOne(dbRef.getId().toString());
                result.add(refObj);
            }
        }
        try{
            field.set(obj, result);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private String getFieldName(){
        String fieldName = field.getName();
        if(refList != null){
            String name = refList.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
