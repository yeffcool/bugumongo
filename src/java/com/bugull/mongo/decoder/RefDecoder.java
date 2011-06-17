package com.bugull.mongo.decoder;

import com.bugull.mongo.BuguEntity;
import com.bugull.mongo.BuguDao;
import com.bugull.mongo.annotations.Ref;
import com.bugull.mongo.cache.ConstructorCache;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.lang.reflect.Field;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class RefDecoder extends AbstractDecoder{
    
    private final static Logger logger = Logger.getLogger(RefDecoder.class);
    
    private Ref ref;
    
    public RefDecoder(Field field, DBObject dbo){
        super(field, dbo);
        ref = field.getAnnotation(Ref.class);
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
        DBRef dbRef = (DBRef)o;
        String refId = dbRef.getId().toString();
        Class<?> clazz = field.getType();
        BuguEntity refObj = null;
        if(ref.lazy()){
            refObj = (BuguEntity)ConstructorCache.getInstance().create(clazz);
            refObj.setId(refId);
        }else{
            BuguDao buguDao = new BuguDao(clazz);
            refObj = (BuguEntity)buguDao.findOne(refId);
        }
        try{
            field.set(obj, refObj);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
    }
    
    private String getFieldName(){
        String fieldName = field.getName();
        if(ref != null){
            String name = ref.name();
            if(!name.equals("")){
                fieldName = name;
            }
        }
        return fieldName;
    }
    
}
