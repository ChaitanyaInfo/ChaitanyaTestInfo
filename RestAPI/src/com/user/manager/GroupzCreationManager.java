package com.user.manager;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.ApartmentSettings;
import com.apartment.database.tables.Builder;
import com.apartment.database.tables.RoleDefinition;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

public class GroupzCreationManager {
	RestUtils restUtils = new RestUtils();
	
	public String createGroupz(String groupzRequest,HttpServletRequest request){
		String groupzResponse="";
		JSONObject requestJson = JSONObject.fromObject(groupzRequest);
		String serviceType = "";
		String functionType = "";
		try {
			serviceType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("servicetype");
			functionType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("functiontype");			
			System.out.println("ST:" + serviceType + ",FT:" + functionType);
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				groupzResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return groupzResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				groupzResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return groupzResponse;
			}
			if (functionType.equalsIgnoreCase(PropertiesUtil
					.getProperty("groupzcreation")) == true) {
				if( requestJson.getJSONObject("json")
						.getJSONObject("request").containsKey("data")==true){
					JSONObject dataPart = requestJson.getJSONObject("json")
							.getJSONObject("request").getJSONObject("data");
					groupzResponse = saveGroupz(serviceType,
							functionType, dataPart);
					System.out.println("Groupz Creation Response:"
							+ groupzResponse.toString());
				}else{
					groupzResponse = processErrorJSONString(
							PropertiesUtil.getProperty("invalidfunctionType_code"),
							PropertiesUtil
									.getProperty("invalidfunctionType_message"));
					return groupzResponse;
				}
				

			} else {
				groupzResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return groupzResponse;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in json formation");
			groupzResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));

		}
		return groupzResponse;
	}
	
	public String saveGroupz(String serviceType,String functionType,JSONObject dataPart){
		String responseStr="";
		String name="";int type=-1;String address="";String city="";String area="";String state="";String country="";String postalCode="";
		String landline="";String mobile="";String fbLink="";String twLink="";String blogLink="";String gpzCode="";String defaultRole="";
		String senderSms="";String senderEmail="";String registrationno="";String panno="";String albumSize="";String groupzBase="";
		String gpzType="";String maxCount="";String loginURL="";String desc="";String latitude="";String longitude="";String metatagsDesc="";
		String metatagsKeywords="";String smsCost="";String receiptPrefix="";String others="";
		try{
			Apartment apt = new Apartment();
			ApartmentSettings aptSettings = new ApartmentSettings();
			if(dataPart.containsKey("name")==true){
				name = dataPart.getString("name");
				if(restUtils.isEmpty(name)==false){
					System.out.println("Name cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("Nameempty_code"),
							PropertiesUtil
									.getProperty("Nameempty_message"));
					return responseStr;
				}
				Apartment ap = DBOperations.getApartmentByName(name);
				if(ap!=null){
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("gpzname_invalid_code"),
							PropertiesUtil
									.getProperty("gpzname_invalid_message"));
					return responseStr;
				}
				apt.setName(name);
			}else{
				System.out.println("Name key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("name_nokey_code"),
						PropertiesUtil
								.getProperty("name_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("type")==true){
				type=dataPart.getInt("type");
				if(type<0){
					System.out.println("Type cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("Typeempty_code"),
							PropertiesUtil
									.getProperty("Typeempty_message"));
					return responseStr;
				}
				apt.setApartmentType(type);
			}else{
				System.out.println("Type key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("type_nokey_code"),
						PropertiesUtil
								.getProperty("type_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("address")==true){
				address = dataPart.getString("address");
				if(restUtils.isEmpty(address)==false){
					System.out.println("address cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("address_empty_code"),
							PropertiesUtil
									.getProperty("address_empty_message"));
					return responseStr;
				}
				apt.setStreetAddress(address);
				
			}else{
				System.out.println("Address key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("address_nokey_code"),
						PropertiesUtil
								.getProperty("address_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("city")==true){
				city = dataPart.getString("city");
				if(restUtils.isEmpty(city)==false){
					System.out.println("City cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("city_empty_code"),
							PropertiesUtil
									.getProperty("city_empty_message"));
					return responseStr;
				}
				apt.setCity(city);

			}else{
				System.out.println("City key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("city_nokey_code"),
						PropertiesUtil
								.getProperty("city_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("area")==true){
				area = dataPart.getString("area");
				if(restUtils.isEmpty(area)==false){
					System.out.println("Area cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("area_empty_code"),
							PropertiesUtil
									.getProperty("area_empty_message"));
					return responseStr;
				}
				apt.setArea(area);
			}else{
				System.out.println("Area key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("area_nokey_code"),
						PropertiesUtil
								.getProperty("area_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("state")==true){
				state = dataPart.getString("state");
				if(restUtils.isEmpty(state)==false){
					System.out.println("State cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("state_empty_code"),
							PropertiesUtil
									.getProperty("state_empty_message"));
					return responseStr;
				}
				apt.setState(state);
			}else{
				System.out.println("State key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("state_nokey_code"),
						PropertiesUtil
								.getProperty("state_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("country")==true){
				country = dataPart.getString("country");
				if(restUtils.isEmpty(country)==false){
					System.out.println("Country cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("country_empty_code"),
							PropertiesUtil
									.getProperty("country_empty_message"));
					return responseStr;
				}
				apt.setCountry(country);
			}else{
				System.out.println("Country key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("country_nokey_code"),
						PropertiesUtil
								.getProperty("country_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("postalcode")==true){
				postalCode = dataPart.getString("postalcode");	
				if(restUtils.isEmpty(postalCode)==false){
					System.out.println("Postal code cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("postal_empty_code"),
							PropertiesUtil
									.getProperty("postal_empty_message"));
					return responseStr;
				}
				apt.setPostalCode(postalCode);
			}else{
				System.out.println("Postalcode key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("postal_nokey_code"),
						PropertiesUtil
								.getProperty("postal_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("landline")==true){
				landline=dataPart.getString("landline");
				if(restUtils.isEmpty(landline)==true){
					apt.setLandLine(landline);
				}
			}
			if(dataPart.containsKey("mobile")==true){
				mobile=dataPart.getString("mobile");
				if(restUtils.isEmpty(mobile)==true){
					apt.setMobile(mobile);
				}
			}
			if(dataPart.containsKey("facebooklink")==true){
				fbLink=dataPart.getString("facebooklink");
				if(restUtils.isEmpty(fbLink)==true){
					apt.setFacebookLink(fbLink);
				}
			}
			if(dataPart.containsKey("twitterlink")==true){
				twLink=dataPart.getString("twitterlink");
				if(restUtils.isEmpty(twLink)==true){
					apt.setTwitterLink(twLink);
				}
			}
			if(dataPart.containsKey("bloglink")==true){
				blogLink=dataPart.getString("bloglink");
				if(restUtils.isEmpty(blogLink)==true){
					apt.setBlogLink(blogLink);
				}
			}			
			if(dataPart.containsKey("groupzcode")==true){
				gpzCode = dataPart.getString("groupzcode");
				if(restUtils.isEmpty(gpzCode)==false){
					System.out.println("Groupz code cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("invalidgroupzcodeempty_code"),
							PropertiesUtil
									.getProperty("invalidgroupzcodeempty_message"));
					return responseStr;
				}
				Apartment groupz = DBOperations.getApartmentByCode(gpzCode);
				if(groupz!=null){
					if(restUtils.isEmpty(gpzCode)==false){
						System.out.println("Groupz code already exists");
						responseStr = processErrorJSONString(
								PropertiesUtil.getProperty("gpzcode_exists_code"),
								PropertiesUtil
										.getProperty("gpzcode_exists_message"));
						return responseStr;
					}
				}
				apt.setSocietyCode(gpzCode);
			}else{
				System.out.println("Groupz key key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("gpzcode_nokey_code"),
						PropertiesUtil
								.getProperty("gpzcode_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("defaultuserrole")==true){
				defaultRole=dataPart.getString("defaultuserrole");
				if(restUtils.isEmpty(defaultRole)==true){					
					aptSettings.setDefaultRoleNameStr(defaultRole);
				}
			}
			if(dataPart.containsKey("sendersms")==true){
				senderSms=dataPart.getString("sendersms");
				if(restUtils.isEmpty(senderSms)==true){					
					aptSettings.setSenderSms(senderSms);
				}
			}
			if(dataPart.containsKey("senderemail")==true){
				senderEmail=dataPart.getString("senderemail");
				if(restUtils.isEmpty(senderEmail)==true){					
					aptSettings.setSenderEmail(senderEmail);
				}
			}
			if(dataPart.containsKey("registrationno")==true){
				registrationno=dataPart.getString("registrationno");
				if(restUtils.isEmpty(registrationno)==true){					
					aptSettings.setServiceTaxRegistrationNumber(registrationno);
				}
			}
			if(dataPart.containsKey("panno")==true){
				panno=dataPart.getString("panno");
				if(restUtils.isEmpty(panno)==true){					
					aptSettings.setPanNumber(panno);
				}
			}
			if(dataPart.containsKey("albumsize")==true){
				albumSize=dataPart.getString("albumsize");
				if(restUtils.isEmpty(albumSize)==true){					
					aptSettings.setMaximumAlbumSize(albumSize);
				}else{
					aptSettings.setMaximumAlbumSize("100");
				}
			}
			if(dataPart.containsKey("groupzbase")==true){
				groupzBase = dataPart.getString("groupzbase");
				if(restUtils.isEmpty(groupzBase)==false){
					System.out.println("Groupz base cannot be empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("gpzcode_empty_code"),
							PropertiesUtil
									.getProperty("gpzcode_empty_message"));
					return responseStr;
				}
				Builder builder = (Builder) DBOperations.getSingleDatabaseObject(Builder.class, "name='"+groupzBase+"'");
				if(builder==null){
					System.out.println("Groupz base Invalid");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("gpzcode_invalid_code"),
							PropertiesUtil
									.getProperty("gpzcode_invalid_message"));
					return responseStr;
				}
				apt.setBuilder(builder);
			}else{
				System.out.println("Groupzbase key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("gpzbase_nokey_code"),
						PropertiesUtil
								.getProperty("gpzbase_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("segments")==true){
				JSONArray segmentsArray = dataPart.getJSONArray("segments");
				if(segmentsArray!=null && segmentsArray.size()>0){
					StringBuilder b = new StringBuilder();
					for(int i=0;i<segmentsArray.size();i++){
						JSONObject segmentObj = segmentsArray.getJSONObject(i);
						String segment = segmentObj.getString("segment");
						b.append(segment + "\n");						
					}
					apt.setSegments(b.toString());
				}
			}
			if(dataPart.containsKey("userareas")==true){
				JSONArray userAreaArray = dataPart.getJSONArray("userareas");
				if(userAreaArray!=null && userAreaArray.size()>0){
					StringBuilder b = new StringBuilder();
					for(int i=0;i<userAreaArray.size();i++){
						JSONObject segmentObj = userAreaArray.getJSONObject(i);
						String usrarea = segmentObj.getString("userarea");
						b.append(usrarea + "\n");						
					}
					apt.setUserarea(b.toString());
				}
			}
			if(dataPart.containsKey("groupztype")==true){
				gpzType=dataPart.getString("groupztype");
				if(restUtils.isEmpty(gpzType)==true){					
				apt.setGroupzType(gpzType);
				}
			}
			if(dataPart.containsKey("blocks")==true){
				JSONArray blocksArray = dataPart.getJSONArray("blocks");
				if(blocksArray!=null && blocksArray.size()>0){
					StringBuilder b = new StringBuilder();
					for(int i=0;i<blocksArray.size();i++){
						JSONObject divObj = blocksArray.getJSONObject(i);
						String div = divObj.getString("block");
						b.append(div+"\n");
					}
					apt.setBlockStreets(b.toString());
				}else{
					System.out.println("Blocks array is empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("Blockempty_code"),
							PropertiesUtil
									.getProperty("Blockempty_message"));
					return responseStr;
				}
			}else{
				System.out.println("Blocks key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("block_nokey_code"),
						PropertiesUtil
								.getProperty("block_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("subdivisions")==true){	
				JSONArray subBlocksArray = dataPart.getJSONArray("subdivisions");
				if(subBlocksArray!=null && subBlocksArray.size()>0){
					StringBuilder b = new StringBuilder();
					for(int i=0;i<subBlocksArray.size();i++){
						JSONObject subDivObj = subBlocksArray.getJSONObject(i);
						String subDiv = subDivObj.getString("subdivision");
						b.append(subDiv+"\n");
					}
					apt.setSubDivisions(b.toString());
				}else{
					System.out.println("SubBlocksArray  is empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("subblock_empty_code"),
							PropertiesUtil
									.getProperty("subblock_empty_message"));
					return responseStr;
				}
			}else{
				System.out.println("Subdivisions  key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("subblock_nokey_code"),
						PropertiesUtil
								.getProperty("subblock_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("maxcount")==true){
				maxCount=dataPart.getString("maxcount");
				if(restUtils.isEmpty(maxCount)==true){					
				apt.setMaxCountStr(maxCount);
				}
			}
			if(dataPart.containsKey("loginurl")==true){
				loginURL=dataPart.getString("loginurl");
				if(restUtils.isEmpty(loginURL)==true){					
				aptSettings.setLoginUrl(loginURL);
				}
			}
			if(dataPart.containsKey("descrption")==true){
				desc=dataPart.getString("descrption");
				if(restUtils.isEmpty(desc)==true){					
				apt.setDescription(desc);
				}
			}
			if(dataPart.containsKey("latitude")==true){
				latitude=dataPart.getString("latitude");
				if(restUtils.isEmpty(latitude)==true){					
				apt.setLatitude(latitude);
				}
			}
			if(dataPart.containsKey("longitude")==true){
				longitude=dataPart.getString("longitude");
				if(restUtils.isEmpty(longitude)==true){					
				apt.setLongitude(longitude);
				}
			}
			if(dataPart.containsKey("metatagdesc")==true){
				metatagsDesc=dataPart.getString("metatagdesc");
				if(restUtils.isEmpty(metatagsDesc)==true){					
				apt.setMetaTagsDesc(metatagsDesc);
				}
			}
			if(dataPart.containsKey("metatagkeywords")==true){
				metatagsKeywords=dataPart.getString("metatagkeywords");
				if(restUtils.isEmpty(metatagsKeywords)==true){					
				apt.setMetaTagsKeywords(metatagsKeywords);
				}
			}
			if(dataPart.containsKey("ipaddresses")==true){		
				
				JSONArray ipAddressArray = dataPart.getJSONArray("ipaddresses");
				if(ipAddressArray!=null && ipAddressArray.size()>0){
					StringBuilder ip = new StringBuilder();
					for(int i=0;i<ipAddressArray.size();i++){
						JSONObject ipAddrObj = ipAddressArray.getJSONObject(i);
						String ipAddr = ipAddrObj.getString("ipaddress");
						ip.append(ipAddr+"\n");
					}
					aptSettings.setIpAddress(ip.toString());
				}else{
					System.out.println("IpaddressArray is empty");
					responseStr = processErrorJSONString(
							PropertiesUtil.getProperty("ipaddress_empy_code"),
							PropertiesUtil
									.getProperty("ipaddress_empty_message"));
					return responseStr;
				}
			}else{
				System.out.println("Ipaddresses  key not found");
				responseStr = processErrorJSONString(
						PropertiesUtil.getProperty("ipaddress_nokey_code"),
						PropertiesUtil
								.getProperty("ipaddress_nokey_message"));
				return responseStr;
			}
			if(dataPart.containsKey("smscost")==true){
				smsCost =dataPart.getString("smscost");	
				if(restUtils.isEmpty(receiptPrefix)==true){						
				aptSettings.setSmsCost(Float.parseFloat(smsCost));			
				}
			}
			if(dataPart.containsKey("receiptnoprefix")==true){
				receiptPrefix=dataPart.getString("receiptnoprefix");
				if(restUtils.isEmpty(receiptPrefix)==true){					
				aptSettings.setReceiptNoPrefix(receiptPrefix);
				}
			}
			if(dataPart.containsKey("others")==true){
				others=dataPart.getString("others");
				if(restUtils.isEmpty(others)==true){					
				apt.setGroupzDescription(others);
				}
			}
			apt.save();
			aptSettings.setApartmentId(apt.getId());
			aptSettings.save();
			responseStr = processSuccessJSONString(serviceType, functionType);
			return responseStr;
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in json formation");
			responseStr = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));

		}
		return responseStr;
	}
	
	public String processErrorJSONString(String statusCode, String message) {
		String errorJSONString = new String();
		JSONObject errorJSON = new JSONObject();
		JSONObject errorRespJSON = new JSONObject();
		JSONObject statusJSON = new JSONObject();
		statusJSON.put("statuscode", statusCode);
		statusJSON.put("statusmessage", message);
		errorRespJSON.put("response", statusJSON);
		errorJSON.put("json", errorRespJSON);
		errorJSONString = errorJSON.toString();
		return errorJSONString;

	}

	public String processSuccessJSONString(String serviceType,
			String functionType) {
		String sucessJSONString = new String();
		JSONObject sucessJSON = new JSONObject();
		JSONObject sucessRespJSON = new JSONObject();
		JSONObject contentJSON = new JSONObject();
		contentJSON.put("servicetype", serviceType);
		contentJSON.put("functiontype", functionType);
		contentJSON.put("statuscode",
				PropertiesUtil.getProperty("statuscodesuccessvalue"));
		contentJSON.put("statusmessage",
				PropertiesUtil.getProperty("statusmessagesuccessvalue"));		
		sucessRespJSON.put("response", contentJSON);
		sucessJSON.put("json", sucessRespJSON);
		sucessJSONString = sucessJSON.toString();
		return sucessJSONString;
	}	

}
