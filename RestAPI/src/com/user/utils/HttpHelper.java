package com.user.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;



public class HttpHelper {

	 private static String serverURL = null;
	 private static String finalServerUrl = null;
	 
	    public static void setServerURL(HttpServletRequest request) { 
	        if (serverURL == null) {
	            StringBuffer url = request.getRequestURL();
	            if( request != null)
	                url  = request.getRequestURL(); 
	            url.delete(url.lastIndexOf("/RestAPI"), url.length());
	            serverURL = url.toString();
	            System.out.println("Server Url :" +serverURL);
	        }
	    } 

	    public static synchronized String getServerURL( HttpServletRequest request , String pageRef ) {
	        String pageRefToReturn = null ;
	        if (serverURL == null) {
	            setServerURL( request ) ;
	        }
	        if( pageRef != null && pageRef.isEmpty() == false) {
	        	finalServerUrl = removePortNumber(serverURL);
	        	System.out.println("Final Server URL:"+finalServerUrl);
	        	if(finalServerUrl.trim().endsWith("/")){
	        		//pageRefToReturn = serverURL + "society" + pageRef ;
	        		
	        		
	        		pageRefToReturn = finalServerUrl +"RestAPI"+pageRef;
	        	}
	        	else{
	           // pageRefToReturn = serverURL + "/society" + pageRef ;
	        		pageRefToReturn = finalServerUrl +"/RestAPI"+pageRef;
	        }
	        }
	     //   System.out.println("Page Return Url :" +pageRefToReturn);
	        return pageRefToReturn;
	    }

	    public static void setServerURL(String serverURL) {
	    	HttpHelper.serverURL = serverURL;
	    }
	    	   
	    public static String removePortNumber(String url){
	    	//System.out.println("URL in remove port number:"+url);
			String finalUrl=null;
			//Pattern pattern = Pattern.compile("(http[s]?://)([^:^/]*)(:\\d*)");
			Pattern pattern = Pattern.compile("(http[s]?://)([^:^/]*)(:\\d*)?(.*)?");			
			Matcher matcher = pattern.matcher(url);		
			matcher.find();			
			String protocol = matcher.group(1);  			
			String domain   = matcher.group(2);			
			finalUrl = protocol + domain; 
			//System.out.println("Final URL in remove port number:");
			return finalUrl;
		}
}
