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

import com.bugull.mongo.mapper.StreamUtil;
import com.bugull.mongo.mapper.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * A convenient Servlet for getting a file in GridFS via http.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class UploadedFileServlet extends HttpServlet {
    
    private final static Logger logger = Logger.getLogger(UploadedFileServlet.class);
    
    private final static String SLASH = "/";
    
    private String allowBucket;
    private String forbidBucket;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        allowBucket = config.getInitParameter("allowBucket");
        forbidBucket = config.getInitParameter("forbidBucket");
        if(!StringUtil.isEmpty(allowBucket) && !StringUtil.isEmpty(forbidBucket)){
            throw new ServletException("You can set only one param between allowBucket and forbidBucket.");
        }
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        int second = url.indexOf(SLASH, 1);
        url = url.substring(second);
        int last = url.lastIndexOf(SLASH);
        String filename = url.substring(last+1);
        DBObject query = new BasicDBObject(BuguFS.FILENAME, filename);
        query.put(ImageUploader.DIMENSION, null);
        String bucketName = BuguFS.DEFAULT_BUCKET;
        int first = url.indexOf(SLASH);
        if(first != last){
            String sub = url.substring(first+1, last);
            String[] arr = sub.split(SLASH);
            for(int i=0; i<arr.length; i+=2){
                if(arr[i].equals(BuguFS.BUCKET)){
                    bucketName = arr[i+1];
                }else{
                    query.put(arr[i], arr[i+1]);
                }
            }
        }
        //check if the bucket is allowed to access by this servlet
        if(!StringUtil.isEmpty(allowBucket) && !allowBucket.equalsIgnoreCase(bucketName)){
            return;
        }
        if(!StringUtil.isEmpty(forbidBucket) && forbidBucket.equalsIgnoreCase(bucketName)){
            return;
        }
        BuguFS fs = new BuguFS(bucketName);
        GridFSDBFile f = fs.findOne(query);
        if(f == null){
            return;
        }
        OutputStream os = response.getOutputStream();
        int fileLength = (int)f.getLength();
        String ext = StringUtil.getExtention(filename);
        response.setContentType(getContentType(ext));
        String range = request.getHeader("Range");
        //normal http request, no "range" in header.
        if(StringUtil.isEmpty(range)){
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLength(fileLength);
            if(needCache(ext)){
                String modifiedSince = request.getHeader("If-Modified-Since");
                DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                df.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date uploadDate = f.getUploadDate();
                String lastModified = df.format(uploadDate);
                if(modifiedSince != null){
                    Date modifiedDate = null;
                    Date sinceDate = null;
                    try{
                        modifiedDate = df.parse(lastModified);
                        sinceDate = df.parse(modifiedSince);
                    }catch(ParseException ex){
                        logger.error("Can not parse the Date", ex);
                    }
                    if(modifiedDate.compareTo(sinceDate) <= 0){
                        response.setStatus(304);    //Not Modified
                        return;
                    }
                }
                long maxAge = 365L * 24L * 60L * 60L;    //one year, in seconds
                response.setHeader("Cache-Control", "max-age=" + maxAge);
                response.setHeader("Last-Modified", lastModified);
                response.setDateHeader("Expires", uploadDate.getTime() + maxAge * 1000L);
            }else{
                response.setHeader("Pragma","no-cache");
                response.setHeader("Cache-Control","no-cache");
                response.setDateHeader("Expires", 0);
            }
            f.writeTo(os);
        }
        //has "range" in header
        else{
            range = range.substring("bytes=".length());
            if(StringUtil.isEmpty(range)){
                return;
            }
            int begin = 0;
            int end = fileLength - 1;
            String[] rangeArray = range.split("-");
            if(rangeArray.length == 1){
                begin = Integer.parseInt(rangeArray[0]);
            }else if(rangeArray.length == 2){
                begin = Integer.parseInt(rangeArray[0]);
                end = Integer.parseInt(rangeArray[1]);
            }
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            int contentLength = end - begin + 1;
            response.setContentLength(contentLength);
            response.setHeader("Content-Range", "bytes " + begin + "-" + end + "/" + contentLength);
            InputStream is = f.getInputStream();
            is.skip(begin);
            int read = -1;
            int bufferSize = (int)f.getChunkSize();
            byte[] buffer = new byte[bufferSize];
            int remain = contentLength;
            int readSize = Math.min(bufferSize, remain);
            while( (read = is.read(buffer, 0, readSize)) != -1 ){
                os.write(buffer, 0, read);
                remain -= read;
                if(remain <= 0){
                    break;
                }
                readSize = Math.min(bufferSize, remain);
            }
            StreamUtil.safeClose(is);
        }
        StreamUtil.safeClose(os);
    }
    
    /**
     * If it's an image file, cache it in browser
     * @param ext
     * @return 
     */
    private boolean needCache(String ext){
        if(StringUtil.isEmpty(ext)){
            return false;
        }
        ext = ext.toLowerCase();
        boolean need = false;
        String[] arr = {"jpg", "jpeg", "png", "gif", "bmp"};
        for(String s : arr){
            if(s.equals(ext)){
                need = true;
                break;
            }
        }
        return need;
    }
    
    private String getContentType(String ext){
        if(StringUtil.isEmpty(ext)){
            return "application/octet-stream";
        }
        ext = ext.toLowerCase();
        String type = "application/octet-stream";
        if(ext.equals("jpg")){
            type = "image/jpeg";
        }
        else if(ext.equals("jpeg") || ext.equals("png") || ext.equals("gif") || ext.equals("bmp")){
            type = "image/" + ext;
        }
        else if(ext.equals("swf")){
            type = "application/x-shockwave-flash";
        }
        else if(ext.equals("flv")){
            type = "video/x-flv";
        }
        else if(ext.equals("mp3")){
            type = "audio/mpeg";
        }
        else if(ext.equals("mp4")){
            type = "video/mp4";
        }
        else if(ext.equals("3gp")){
            type = "video/3gpp";
        }
        else if(ext.equals("pdf")){
            type = "application/pdf";
        }
        else if(ext.equals("html") || ext.equals("htm")){
            type = "text/html";
        }
        return type;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
    
}
