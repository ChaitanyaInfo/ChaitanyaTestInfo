package com.user.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.SocietyProfile;
import com.apartment.util.Utils;
import com.user.operations.GroupzOperations;
import com.user.utils.PropertiesUtil;
import com.user.utils.IPAddressCheck;
import com.user.utils.RestUtils;

public class SelectGroupzManager {

	private IPAddressCheck ipCheck = new IPAddressCheck();
	private List<String> ipAddressList;
	private RestUtils restUtils = new RestUtils();
	private String groupzRespList = "";	
	private String serviceType = "";
	private String functionType = "";
	private String ipAddress = "";

	public String getGroupzAccount(String groupzListRequest,
			HttpServletRequest req) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject json = new JSONObject();		
		try {
			json = (JSONObject) xmlSerializer.read(groupzListRequest);
			serviceType = json.getJSONObject("request").getString("servicetype");
			functionType = json.getJSONObject("request").getString("functiontype");
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				groupzRespList = processError(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return groupzRespList;
			}
			if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("listofgroupzforivrenquirymobile")))) {
				System.out.println("Calling groupzlist for ivr enquiry mobile");
				groupzRespList = getGroupzListForIVREnquiryMobile(json,req);
			}
			else if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("listofgroupzforivrenquirylandline")))) {
				System.out.println("Calling groupzlist for ivr enquiry landline");
				groupzRespList = getGroupzListForIVREnquiryLandline(json,req);
			}
			else if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("listofgroupzforivrrecord")))) {
				System.out.println("Calling groupzlist for ivr record");
				groupzRespList = getGroupzListForIVRRecording(json,req);
			}
			else{
				System.out.println("Invalid Function Type");
				groupzRespList = processError(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return groupzRespList;
			}
		
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			groupzRespList = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return groupzRespList;
	}
	
	
	//getting groupz list for IVR enquiry mobile number
	public String getGroupzListForIVREnquiryMobile(JSONObject jsonObj,HttpServletRequest req){
		String groupzIVREnquiryMobileResponse="";
		String countryCode = "";
		String mobileNumber = "";
		String moduleCode = "";		
		try{
			countryCode = jsonObj.getJSONObject("request").getJSONObject("mobile").getString("countrycode");
			mobileNumber = jsonObj.getJSONObject("request").getJSONObject("mobile").getString(
					"mobilenumber");
			moduleCode = jsonObj.getJSONObject("request").getString("modulecode");
			if(restUtils.isEmpty(moduleCode) == false){
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("moduleempty_code"),
						PropertiesUtil.getProperty("moduleempty_message"));
				return groupzIVREnquiryMobileResponse;
			}		
			if (moduleCode.equalsIgnoreCase(PropertiesUtil
					.getProperty("issues_enabled"))==false) {
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("invalidmodule_code"),
						PropertiesUtil.getProperty("invalidmodule_message"));
				return groupzIVREnquiryMobileResponse;
			}
			if (restUtils.isEmpty(countryCode) == false) {
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return groupzIVREnquiryMobileResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return groupzIVREnquiryMobileResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return groupzIVREnquiryMobileResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return groupzIVREnquiryMobileResponse;
			}
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			String query = " person.mobile='" + completeMobile + "'";					
			List<Apartment> groupzList = GroupzOperations
					.getGroupzListForGroupz(query);		
			ipAddress = req.getRemoteAddr();
			System.out.println("Ipaddress:" + ipAddress);
			ipAddressList = new ArrayList<String>();
			StringBuffer gpzList = new StringBuffer();
			int countIPAddress = 0;
			if(groupzList!=null && groupzList.isEmpty()==false && groupzList.size()>0){
			System.out.println("Groupz list:" + groupzList.size());
			for (Apartment apt : groupzList) {
				String stList = (String) DBOperations.getIpAddresses(
						apt.getId()).get(0);
				if (stList != null) {
					StringTokenizer st = new StringTokenizer(stList, "\n");
					while (st.hasMoreTokens()) {
						ipAddressList.add(st.nextToken());
					}
				}
				if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
					SocietyProfile sp = (SocietyProfile) DBOperations.getSingleDatabaseObject(SocietyProfile.class, "issues_enabled=true and apartmentid="+apt.getId());
					if(sp!=null){
					String st = "";
					String aptLogoURL = "";
					
					//aptLogoURL = restUtils.createAptLogoURL(req, apt.getId());	
					aptLogoURL = restUtils.createGroupzLogo(req, apt.getSocietyCode());
						st = "<element><groupzcode>" + apt.getSocietyCode()
								+ "</groupzcode><groupzname>" + apt.getName()
								+ "</groupzname><groupzid>" + apt.getId()
								+ "</groupzid><groupzurl>"+Utils.encode(aptLogoURL)+"</groupzurl></element>";
					gpzList.append(st);
					}
				} else {
					countIPAddress++;
					if (countIPAddress == groupzList.size()) {
						groupzIVREnquiryMobileResponse = processError(PropertiesUtil
								.getProperty("accessdenied_code"),
								PropertiesUtil
										.getProperty("accessdenied_message"));
						return groupzIVREnquiryMobileResponse;
					}

				}
				stList = "";
				ipAddressList = new ArrayList<String>();
			}
			}
			else{
				System.out.println("Empty groupz list");
				groupzIVREnquiryMobileResponse = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return groupzIVREnquiryMobileResponse;
			}
			String finalResp = "<groupzlist>" + gpzList.toString()
					+ "</groupzlist>";
			groupzIVREnquiryMobileResponse = processSucess(finalResp);
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			groupzIVREnquiryMobileResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return groupzIVREnquiryMobileResponse;
	}
	
	// getting groupz list IVR enquiry landline number 
	public String getGroupzListForIVREnquiryLandline(JSONObject jsonObj,HttpServletRequest req){
		String groupzIVREnquiryLandlineResponse="";
		String countryCode = "";
		String stateCode = "";
		String landlineNumber = "";
		String moduleCode="";
		try{
		countryCode = jsonObj.getJSONObject("request").getJSONObject("landline").getString("countrycode");
		stateCode = jsonObj.getJSONObject("request").getJSONObject("landline").getString("statecode");	
		landlineNumber = jsonObj.getJSONObject("request").getJSONObject("landline").getString(
				"landlinenumber");
		moduleCode = jsonObj.getJSONObject("request").getString("modulecode");
		if(restUtils.isEmpty(moduleCode) == false){
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("moduleempty_code"),
					PropertiesUtil.getProperty("moduleempty_message"));
			return groupzIVREnquiryLandlineResponse;
		}		
		if (moduleCode.equalsIgnoreCase(PropertiesUtil
				.getProperty("issues_enabled"))==false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("invalidmodule_code"),
					PropertiesUtil.getProperty("invalidmodule_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (restUtils.isEmpty(countryCode) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("countryempty_code"),
					PropertiesUtil.getProperty("countryempty_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (Utils.isNumber(countryCode) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("invalidcountry_code"),
					PropertiesUtil.getProperty("invalidcountry_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (restUtils.isEmpty(stateCode) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("stateempty_code"),
					PropertiesUtil.getProperty("stateempty_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (Utils.isNumber(stateCode) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("invalidstate_code"),
					PropertiesUtil.getProperty("invalidstate_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (restUtils.isEmpty(landlineNumber) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("landlineempty_code"),
					PropertiesUtil.getProperty("landlineempty_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		if (Utils.isNumber(landlineNumber) == false) {
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("invalidlandline_code"),
					PropertiesUtil.getProperty("invalidlandline_message"));
			return groupzIVREnquiryLandlineResponse;
		}
		String completeLandline = "+" + countryCode + "." + stateCode + "."+landlineNumber;
		String query = " person.phone='" + completeLandline + "'";					
		List<Apartment> groupzList = GroupzOperations
				.getGroupzListForGroupz(query);		
		ipAddress = req.getRemoteAddr();
		System.out.println("Ipaddress:" + ipAddress);
		ipAddressList = new ArrayList<String>();
		StringBuffer gpzList = new StringBuffer();
		int countIPAddress = 0;
		if(groupzList!=null && groupzList.isEmpty()==false && groupzList.size()>0){
		System.out.println("Groupz list:" + groupzList.size());
		for (Apartment apt : groupzList) {
			String stList = (String) DBOperations.getIpAddresses(
					apt.getId()).get(0);
			if (stList != null) {
				StringTokenizer st = new StringTokenizer(stList, "\n");
				while (st.hasMoreTokens()) {
					ipAddressList.add(st.nextToken());
				}
			}
			if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
				SocietyProfile sp = (SocietyProfile) DBOperations.getSingleDatabaseObject(SocietyProfile.class, "issues_enabled=true and apartmentid="+apt.getId());
				if(sp!=null){
				String st = "";
				String aptLogoURL = "";
				//aptLogoURL = restUtils.createAptLogoURL(req, apt.getId());	
				aptLogoURL = restUtils.createGroupzLogo(req, apt.getSocietyCode());
					st = "<element><groupzcode>" + apt.getSocietyCode()
							+ "</groupzcode><groupzname>" + apt.getName()
							+ "</groupzname><groupzid>" + apt.getId()
							+ "</groupzid><groupzurl>"+Utils.encode(aptLogoURL)+"</groupzurl></element>";
				gpzList.append(st);
				}
			} else {
				countIPAddress++;
				if (countIPAddress == groupzList.size()) {
					groupzIVREnquiryLandlineResponse = processError(PropertiesUtil
							.getProperty("accessdenied_code"),
							PropertiesUtil
									.getProperty("accessdenied_message"));
					return groupzIVREnquiryLandlineResponse;
				}

			}
			stList = "";
			ipAddressList = new ArrayList<String>();
		}
		}
		else{
			System.out.println("Empty groupz list");
			groupzIVREnquiryLandlineResponse = processError(
					PropertiesUtil.getProperty("invalidlandline_code"),
					PropertiesUtil.getProperty("invalidlandline_message"));
			return groupzIVREnquiryLandlineResponse;
			
		}
		String finalResp = "<groupzlist>" + gpzList.toString()
				+ "</groupzlist>";
		groupzIVREnquiryLandlineResponse = processSucess(finalResp);
	}
	catch (Exception e) {
		// TODO: handle exception
		e.printStackTrace();
		groupzIVREnquiryLandlineResponse = processError(
				PropertiesUtil.getProperty("XMLRequest_code"),
				PropertiesUtil.getProperty("XMLRequest_message"));
	}
		return groupzIVREnquiryLandlineResponse;
	}
	
	//getting groupz list for IVR recording
	public String getGroupzListForIVRRecording(JSONObject jsonObj,HttpServletRequest req){
		System.out.println("Getting groupz list for IVR Record");
		String groupzIVRRecordResponse="";
		String countryCode = "";
		String mobileNumber = "";	
		StringBuffer stbuff = new StringBuffer();
		StringBuffer roleqry = new StringBuffer();
		try{
		countryCode = jsonObj.getJSONObject("request").getJSONObject("mobile").getString("countrycode");
		mobileNumber = jsonObj.getJSONObject("request").getJSONObject("mobile").getString(
				"mobilenumber");
		if (restUtils.isEmpty(countryCode) == false) {
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("countryempty_code"),
					PropertiesUtil.getProperty("countryempty_message"));
			return groupzIVRRecordResponse;
		}
		if (Utils.isNumber(countryCode) == false) {
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("invalidcountry_code"),
					PropertiesUtil.getProperty("invalidcountry_message"));
			return groupzIVRRecordResponse;
		}
		if (restUtils.isEmpty(mobileNumber) == false) {
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("Mobileempty_code"),
					PropertiesUtil.getProperty("Mobileempty_message"));
			return groupzIVRRecordResponse;
		}
		if (Utils.isNumber(mobileNumber) == false) {
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("invalidmobile_code"),
					PropertiesUtil.getProperty("invalidmobile_message"));
			return groupzIVRRecordResponse;
		}
		int finalOffsetValue = 31;
		JSONObject roleValueObj = jsonObj.getJSONObject("request").optJSONObject("roleoffsetlist");
		JSONArray roleOffsetList = null;
		if (roleValueObj == null) {
			roleOffsetList = jsonObj.getJSONObject("request").optJSONArray("roleoffsetlist");
			for (int i = 0; i < roleOffsetList.size(); i++) {
				JSONObject jO = roleOffsetList.getJSONObject(i);
				System.out.println("Role json:" + jO.toString(2));
				String s = jO.getString("roleoffsetvalue");
				if (restUtils.isEmpty(s) == false) {
					groupzIVRRecordResponse = processError(
							PropertiesUtil
									.getProperty("rolevalueempty_code"),
							PropertiesUtil
									.getProperty("rolevalueempty_message"));
					return groupzIVRRecordResponse;
				}
				stbuff.append(s + ",");
			}
			String[] stOff = stbuff.toString().split(",");
			for (int i = 0; i < stOff.length; i++) {
				int rolevalue = (int) Math.pow(2, finalOffsetValue
						- Integer.parseInt(stOff[i]));
				String offCal = " roledefinition.rolevalue&" + rolevalue;
				if (i < stOff.length - 1) {
					roleqry.append(offCal + " and ");
				} else {
					roleqry.append(offCal);
				}
			}
		} else {
			String s = jsonObj.getJSONObject("request").getJSONObject("roleoffsetlist")
					.getJSONObject("element").getString("roleoffsetvalue");
			if (restUtils.isEmpty(s) == false) {
				groupzIVRRecordResponse = processError(
						PropertiesUtil.getProperty("rolevalueempty_code"),
						PropertiesUtil
								.getProperty("rolevalueempty_message"));
				return groupzIVRRecordResponse;
			}
			System.out.println("Role offset value:" + s);
			int singlerolevalue = (int) Math.pow(2, finalOffsetValue
					- Integer.parseInt(s));
			String offCal = " roledefinition.rolevalue&" + singlerolevalue;
			roleqry.append(offCal);
		}

		String completeMobile = "+" + countryCode + "." + mobileNumber;
		String query = " person.mobile='" + completeMobile + "' and "
				+ roleqry.toString();
		List<Apartment> groupzList = GroupzOperations
				.getGroupzListForGroupz(query);		
		ipAddress = req.getRemoteAddr();
		System.out.println("Ipaddress:" + ipAddress);
		ipAddressList = new ArrayList<String>();
		StringBuffer gpzList = new StringBuffer();
		int count = 0;
		if(groupzList!=null && groupzList.isEmpty()==false && groupzList.size()>0){
		System.out.println("Groupz list:" + groupzList.size());
		for (Apartment apt : groupzList) {
			String stList = (String) DBOperations.getIpAddresses(
					apt.getId()).get(0);
			if (stList != null) {
				StringTokenizer st = new StringTokenizer(stList, "\n");
				while (st.hasMoreTokens()) {
					ipAddressList.add(st.nextToken());
				}
			}
			if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
				String st = "";
			/*	st = "<element><groupzcode>"
						+ apt.getSocietyCode()
						+ "</groupzcode><groupzname>"
						+ apt.getName()
						+ "</groupzname><groupznameurl>http://www.groupz.in/audio_ivrs/Testapart.wav</groupznameurl>"
						+ "<hangupmessage>Thanks for calling us</hangupmessage>"
						+ "<hangupaudio>http://www.groupz.in/audio_ivrs/Testapart.wav</hangupaudio>"
						+ "<recordhangupmessage>Your recorded message has been sent sucessfully</recordhangupmessage>"
						+ "<recordhangupaudio>http://www.groupz.in/audio_ivrs/Testapart.wav</recordhangupaudio>"
						+"<errormessage>Technical error occured please contact Administrator</errormessage>"												
						+ "<erroraudio>http://www.groupz.in/audio_ivrs/Testapart.wav</erroraudio></element>";*/
						st = "<element><groupzcode>" + apt.getSocietyCode()
								+ "</groupzcode><groupzname>" + apt.getName()
								+ "</groupzname><groupzid>" + apt.getId()
								+ "</groupzid></element>";
				gpzList.append(st);
			} else {
				count++;
				if (count == groupzList.size()) {
					groupzIVRRecordResponse = processError(PropertiesUtil
							.getProperty("accessdenied_code"),
							PropertiesUtil
									.getProperty("accessdenied_message"));
					return groupzIVRRecordResponse;
				}

			}
			stList = "";
			ipAddressList = new ArrayList<String>();
		}
		}
		else{
			System.out.println("Empty groupz list");
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("invalidmobile_code"),
					PropertiesUtil.getProperty("invalidmobile_message"));
			return groupzIVRRecordResponse;
		}
		System.out.println("Final response:" + gpzList.toString());
		String finalResp = "<groupzlist>" + gpzList.toString()
				+ "</groupzlist>";
		System.out.println("Response xml:" + finalResp);
		groupzIVRRecordResponse = processSucess(finalResp);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			groupzIVRRecordResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return groupzIVRRecordResponse;
	}

	// Invalid message response
	public String processError(String statusCode, String message) {
		StringBuffer errorXMLString = new StringBuffer();
		errorXMLString.append("<xml>");
		errorXMLString.append("<response>");
		errorXMLString.append("<statuscode>");
		errorXMLString.append(statusCode);
		errorXMLString.append("</statuscode>");
		errorXMLString.append("<statusmessage>");
		if (message != null) {
			errorXMLString.append(message);
		}
		errorXMLString.append("</statusmessage>");
		errorXMLString.append("</response>");
		errorXMLString.append("</xml>");
		return errorXMLString.toString();

	}

	public String processSucess(String message) {
		StringBuffer sucessXMLString = new StringBuffer();
		sucessXMLString.append("<xml>");
		sucessXMLString.append("<response>");
		sucessXMLString.append("<servicetype>");
		sucessXMLString.append(serviceType);
		sucessXMLString.append("</servicetype>");
		sucessXMLString.append("<functiontype>");
		sucessXMLString.append(functionType);
		sucessXMLString.append("</functiontype>");
		sucessXMLString.append("<statuscode>");
		sucessXMLString.append(PropertiesUtil
				.getProperty("statuscodesuccessvalue"));
		sucessXMLString.append("</statuscode>");
		sucessXMLString.append("<statusmessage>");
		sucessXMLString.append(PropertiesUtil
				.getProperty("statusmessagesuccessvalue"));
		sucessXMLString.append("</statusmessage>");
		sucessXMLString.append(message);
		sucessXMLString.append("</response>");
		sucessXMLString.append("</xml>");
		return sucessXMLString.toString();

	}			
}
