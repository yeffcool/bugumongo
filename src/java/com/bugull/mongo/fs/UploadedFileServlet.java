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

package com.bugull.mongo.fs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class UploadedFileServlet extends HttpServlet {
    
    private final static Logger logger = Logger.getLogger(UploadedFileServlet.class);
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        int second = url.indexOf("/", 1);
        url = url.substring(second);
        int last = url.lastIndexOf("/");
        String filename = url.substring(last+1).toLowerCase();
        DBObject query = new BasicDBObject("filename", filename);
        int first = url.indexOf("/");
        if(first != last){
            String sub = url.substring(first+1, last);
            String[] arr = sub.split("/");
            for(int i=0; i<arr.length; i+=2){
                query.put(arr[i], arr[i+1]);
            }
        }
        GridFSDBFile f = BuguFS.getInstance().getFS().findOne(query);
        if(f != null){
            int index = filename.lastIndexOf(".");
            String ext = filename.substring(index+1);
            if(ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("bmp")){
                response.setContentType("image/" + ext);
                String modifiedSince = request.getHeader("If-Modified-Since");
                DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                if(modifiedSince != null){
                    Date uploadDate = f.getUploadDate();
                    Date sinceDate = null;
                    try{
                        sinceDate = df.parse(modifiedSince);
                    }catch(ParseException e){
                        logger.error(e.getMessage());
                    }
                    if(uploadDate.compareTo(sinceDate) <= 0){
                        response.setStatus(304);
                        return;
                    }
                }
                long maxAge = 365L * 24L * 60L * 60L;    //one year, in seconds
                response.setHeader("Cache-Control", "max-age=" + maxAge);
                Date now = new Date();
                String lastModified = df.format(now);
                response.setHeader("Last-Modified", lastModified);
                response.setDateHeader("Expires", now.getTime() + maxAge * 1000L);
            }else{
                response.setContentType("application/octet-stream");
                response.setHeader("Pragma","no-cache");
                response.setHeader("Cache-Control","no-cache");
                response.setDateHeader("Expires", 0);
            }
            f.writeTo(response.getOutputStream());
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
}
