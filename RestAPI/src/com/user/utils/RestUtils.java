package com.user.utils;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class RestUtils {

	// bit offset calculation
	public static boolean checkBitOffset(int value, int offsetFromLeft) {

		boolean retValue = false;
		if (offsetFromLeft < 0)
			return false;
		Integer val = new Integer(value);
		int bitOffset = (offsetFromLeft % 32);
		byte reqByte = (byte) (value >>> (31 - bitOffset));

		if ((reqByte & (byte) 0x01) != 0)
			return true;
		return false;
	}

	// checking null
	public boolean isEmpty(String test) {
		if (test == null || test.trim().isEmpty() == true
				|| test.equalsIgnoreCase("[]") || test == "") {
			return false;
		}
		return true;
	}

	// checking valid email
	public boolean isValidEmail(String email) {
		String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

		if (email.matches(EMAIL_REGEX) == true) {
			return true;
		}

		return false;
	}

	// checking time between 1 hour
	public boolean checktimeWithinHour(Date d1, Date d2) {
		try {
			// in milliseconds
			long diff = d2.getTime() - d1.getTime();
			long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);
			if ((diffDays == 0 && diffHours == 0 && diffMinutes <= 59 && diffSeconds <= 59)
					|| (diffDays == 0 && diffHours == 1 && diffMinutes == 0 && diffSeconds == 0)) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String createAptLogoURL(HttpServletRequest request, int aptid) {
		String st = "/apartmentLogo.jsf?id=" + aptid;
		String announcementURL = HttpHelper.getServerURL(request, st);
		return announcementURL;
	}
	
	public String createGroupzLogo(HttpServletRequest request,String code){
		String url = "/GroupzLogo/"+code;
		String gpzLogoURL = HttpHelper.getServerURL(request, url);
		return gpzLogoURL;
	}
	
	public String createProfilePhotoLogo(HttpServletRequest request,int personId){
		String url = "/ProfilePhoto/"+personId;
		String profileURL = HttpHelper.getServerURL(request, url);
		return profileURL;
	}
	
	// remove portnumber from url
	
	public String getValidURL(String url){
		String validURL ="";
		Pattern pattern = Pattern.compile("(http[s]?://)([^:^/]*)(:\\d*)?(.*)?");
		Matcher matcher = pattern.matcher(url);

		matcher.find();

		String protocol = matcher.group(1);            
		String domain   = matcher.group(2);
		String port     = matcher.group(3);
		String uri      = matcher.group(4);
		System.out.println("Protocol:"+protocol);
		System.out.println("Domain:"+domain);
		System.out.println("Port:"+port);
		System.out.println("URI:"+uri);
		validURL = protocol + domain +uri; 
		return validURL;
	}
	
	
	
	public int extractId( String exprString){
		String pattern = "(.*/ProfilePhoto/)(\\d+)([/.*]*)";
		Pattern pat = Pattern.compile(pattern) ;
		Matcher match = pat.matcher(exprString) ;
		if( match.find()){			
			String s = match.group(2 );
			int a = Integer.parseInt(s);
			System.out.println("Value is :"+a);
			return a;
			//System.out.println(match.group(3)) ;
		}else{
			System.out.println("No Match") ;
			return 0;
		}				
	}

}
