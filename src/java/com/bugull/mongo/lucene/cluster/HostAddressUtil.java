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

package com.bugull.mongo.lucene.cluster;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Frank Wen(xbwen@hotmail.com)
 */
public final class HostAddressUtil {
    
    private final static Logger logger = Logger.getLogger(HostAddressUtil.class);
    
    public static List<String> getLocalAddresses() {
        List<String> list = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaceList = null;
        try {
            interfaceList = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            logger.error("Error when getting local network interfaces", ex);
        }
        if(interfaceList != null){
            while(interfaceList.hasMoreElements()){
                NetworkInterface face = interfaceList.nextElement();
                Enumeration<InetAddress> addressList = face.getInetAddresses();
                if(addressList == null){
                    continue;
                }
                while(addressList.hasMoreElements()){
                    InetAddress address = addressList.nextElement();  //both Inet4Address and Inet6Address
                    list.add(address.getHostAddress());
                }
            }
        }
        return list;
    }

}
