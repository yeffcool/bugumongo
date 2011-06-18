package com.bugull.mongo.lucene;

import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class BuguParser {
    
    private final static Logger logger = Logger.getLogger(BuguParser.class);
    
    public Query parse(String field, String value){
        QueryParser parser = new QueryParser(Version.LUCENE_32, field, BuguIndex.getInstance().getAnalyzer());
        return parse(parser, value);
    }
    
    public Query parse(String[] fields, String value){
        QueryParser parser = new MultiFieldQueryParser(Version.LUCENE_32, fields, BuguIndex.getInstance().getAnalyzer());
        return parse(parser, value);
    }
    
    private Query parse(QueryParser parser, String value){
        Query query = null;
        try{
            query = parser.parse(value);
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        return query;
    }
    
}
