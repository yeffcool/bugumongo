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

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Accessing file in GridFS by this servlet is countable.
 * 
 * @author Frank Wen(xbwen@hotmail.com)
 */
public class AccessCountableServlet extends UploadedFileServlet{
    
    private String resourceName;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        resourceName = config.getInitParameter("resourceName");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AccessCount ac = AccessCount.getInstance();
        ac.increaseCount(resourceName);
        try{
            processRequest(request, response);
        }finally{
            ac.descreaseCount(resourceName);
        }
    }

}
