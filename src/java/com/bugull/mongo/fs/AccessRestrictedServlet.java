/*
 * Copyright (c) www.bugull.com
 * 
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

import com.bugull.mongo.utils.StringUtil;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Total threads accessing file in GridFS by this servlet is restricted.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class AccessRestrictedServlet extends UploadedFileServlet {
    
    private static final String DEFAULT_RESOURCE_NAME = "bugu";
    private static final String DEFAULT_MAX_ACCESS = "20";
    private static final String DEFAULT_REDIRECT_TO = "/";
    
    private String resourceName;
    private int maxAccess;
    private String redirectTo;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        resourceName = config.getInitParameter("resourceName");
        if(StringUtil.isEmpty(resourceName)){
            resourceName = DEFAULT_RESOURCE_NAME;
        }
        String strMaxAccess = config.getInitParameter("maxAccess");
        if(StringUtil.isEmpty(strMaxAccess)){
            strMaxAccess = DEFAULT_MAX_ACCESS;
        }
        maxAccess = Integer.parseInt(strMaxAccess);
        redirectTo = config.getInitParameter("redirectTo");
        if(StringUtil.isEmpty(redirectTo)){
            redirectTo = DEFAULT_REDIRECT_TO;
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccessCount ac = AccessCount.getInstance();
        if(ac.getCount(resourceName) > maxAccess){
            response.sendRedirect(redirectTo);
        }
        else{
            ac.increaseCount(resourceName);
            try{
                processRequest(request, response);
            }finally{
                ac.descreaseCount(resourceName);
            }
        }
    }

}
