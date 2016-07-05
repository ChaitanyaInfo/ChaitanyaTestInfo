package com.user.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Person;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.util.Utils;
import com.user.utils.IPAddressCheck;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

public class GCMUpdateManager {
	RestUtils restUtils = new RestUtils();
	List<String> ipAddressList;
	IPAddressCheck ipCheck = new IPAddressCheck();
	//String ipAddress="";

	public String updateGCMId(HttpServletRequest request, String GCMResquest) {
		String GCMResponse = "";

		String serviceType = "";
		String functionType = "";
		String gpzCode = "";
		String countryCode = "";
		String mobileNumber;
		int memberId = 0;
		String gcmId = "";

		JSONObject requestJson = JSONObject.fromObject(GCMResquest);
		System.out.println("Request:" + GCMResquest);
		String ipAddress = request.getRemoteAddr();
		try {
			// json = (JSONObject) xmlSerializer.read(userSelectionRequest);
			serviceType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("servicetype");
			functionType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("functiontype");
			System.out.println("ST:" + serviceType + ",FT:" + functionType);
			gpzCode = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("groupzcode");
			countryCode = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			memberId = requestJson.getJSONObject("json")
					.getJSONObject("request").getInt("memberid");
			System.out.println("1");
			mobileNumber = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("mobile")
					.getString("mobilenumber");
			gcmId = requestJson.getJSONObject("json").getJSONObject("request")
					.getString("gcmid");
			JSONArray personArray = null;
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return GCMResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return GCMResponse;
			}
			if (restUtils.isEmpty(gpzCode) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil
								.getProperty("invalidgroupzcodeempty_code"),
						PropertiesUtil
								.getProperty("invalidgroupzcodeempty_message"));
				return GCMResponse;
			}

			if (memberId <= 0) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("emptymemberId_code"),
						PropertiesUtil.getProperty("emptymemberId_message"));
				return GCMResponse;
			}
			if (restUtils.isEmpty(countryCode) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return GCMResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return GCMResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return GCMResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return GCMResponse;
			}
			String completeMobileNumber = "+" + countryCode + "."
					+ mobileNumber;
			if (requestJson.getJSONObject("json").getJSONObject("request")
					.containsKey("persons") == true) {
				personArray = requestJson.getJSONObject("json")
						.getJSONObject("request").getJSONArray("persons");
			} else {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("persontagmissing_code"),
						PropertiesUtil.getProperty("persontagmissing_message"));
				return GCMResponse;
			}
			if (restUtils.isEmpty(gcmId) == false) {
				GCMResponse = processErrorJSONString(
						PropertiesUtil.getProperty("gcmid_empty_code"),
						PropertiesUtil.getProperty("gcmid_empty_message"));
				return GCMResponse;
			}
			if (functionType.equalsIgnoreCase(PropertiesUtil
					.getProperty("gcmidupdate")) == true) {
			GCMResponse = updateData(serviceType, functionType, gpzCode,
					memberId, completeMobileNumber, gcmId, personArray,ipAddress);					
			return GCMResponse;
			}else{
				GCMResponse = processErrorJSONString(
						PropertiesUtil
								.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return GCMResponse;
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error in json formation");
			GCMResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return GCMResponse;
	}

	public String updateData(String serviceType, String functionType,
			String groupzCode, int memberId, String mobileNumber, String gcmID,
			JSONArray personArray,String ipAddress) {
		String updateResponse = "";
		try {
			Apartment apt = DBOperations.getApartmentByCode(groupzCode);
			UserFlatMapping ufm = DBOperations.getUserFlatMappingForId(memberId);
			if (apt != null) {
				if (apt.isEnabled() == true) {
					ipAddressList = new ArrayList<String>();
					String stList = (String) DBOperations.getIpAddresses(
							apt.getId()).get(0);
					System.out.println("ST LIST:"+stList);
					if (stList != null) {
						StringTokenizer st = new StringTokenizer(stList, "\n");
						while (st.hasMoreTokens()) {
							ipAddressList.add(st.nextToken());
						}
					}
					if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {

					} else {
						updateResponse = processErrorJSONString(PropertiesUtil
								.getProperty("accessdenied_code"),
								PropertiesUtil
										.getProperty("accessdenied_message"));
						return updateResponse;
					}
					if (ufm != null) {
						if (ufm.isEnabled() == true) {
							if ((ufm.getFlat().getRegisteredPerson()
									.getMobile().equalsIgnoreCase(mobileNumber) == false)) {
								System.out.println("Mobile number not matched");
								updateResponse = processErrorJSONString(
										PropertiesUtil
												.getProperty("invalidmemberId_code"),
										PropertiesUtil
												.getProperty("invalidmemberId_message"));
								return updateResponse;
							}
							if (personArray != null && personArray.size() > 0) {
								int personArraySize = personArray.size();
								int count = 0;
								for (int p = 0; p < personArray.size(); p++) {
									JSONObject perObj = personArray
											.getJSONObject(p);
									int id = perObj.getInt("personid");
									Person per = DBOperations.getPersonById(id);
									if (per != null) {
										if(per.getMobile().equalsIgnoreCase(mobileNumber)==true){
										per.setGcmId(gcmID);
										per.save();
										}
									} else {
										count++;
									}
								}
								if (count == personArraySize) {
									System.out.println("Invalid Person id");
									updateResponse = processErrorJSONString(
											PropertiesUtil
													.getProperty("personid_invalid_code"),
											PropertiesUtil
													.getProperty("personid_invalid_message"));
									return updateResponse;
								}else{
									updateResponse = processSuccessJSONString(serviceType, functionType);
									return updateResponse;
								}
							}

						} else {
							updateResponse = processErrorJSONString(
									PropertiesUtil
											.getProperty("invalidmemberId_code"),
									PropertiesUtil
											.getProperty("invalidmemberId_message"));
							return updateResponse;
						}
					} else {
						updateResponse = processErrorJSONString(
								PropertiesUtil
										.getProperty("invalidmemberId_code"),
								PropertiesUtil
										.getProperty("invalidmemberId_message"));
						return updateResponse;
					}
				} else {
					updateResponse = processErrorJSONString(
							PropertiesUtil
									.getProperty("invalidgroupzcode_code"),
							PropertiesUtil
									.getProperty("invalidgroupzcode_message"));
					return updateResponse;
				}
			} else {
				updateResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidgroupzcode_code"),
						PropertiesUtil.getProperty("invalidgroupzcode_message"));
				return updateResponse;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error in json formation");
			updateResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return updateResponse;
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
