package com.user.manager;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.SocietyGroup;
import com.apartment.database.tables.SocietyGroupConnection;

import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class NetworkAccountManager {
	RestUtils restUtils = new RestUtils();

	public String getNetworkAccounts(String networkRequest,
			HttpServletRequest request) {
		String networkResponse = "";
		JSONObject requestJson = JSONObject.fromObject(networkRequest);
		String serviceType = "";
		String functionType = "";
		String networkGroupId = "";
		try {
			serviceType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("servicetype");
			functionType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("functiontype");
			networkGroupId = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("networkgroupid");
			System.out.println("ST:" + serviceType + ",FT:" + functionType);
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				networkResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return networkResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				networkResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return networkResponse;
			}
			if (restUtils.isEmpty(networkGroupId) == false) {
				networkResponse = processErrorJSONString(
						PropertiesUtil.getProperty("groupid_empty_code"),
						PropertiesUtil.getProperty("groupid_empty_message"));
				return networkResponse;
			}
			if (functionType.equalsIgnoreCase(PropertiesUtil
					.getProperty("networkAccountlist")) == true) {
				networkResponse = getGroupzListForNetworkGroupId(serviceType,
						functionType, networkGroupId);
				System.out.println("Network Group Response:"
						+ networkResponse.toString());

			} else {
				networkResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return networkResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in json formation");
			networkResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));

		}
		return networkResponse;
	}

	public String getGroupzListForNetworkGroupId(String serviceType,
			String functionType, String networkGroupId) {
		String groupzListResponse = "";
		SocietyGroup sg = DBOperations.getSocietyGroup(Integer
				.parseInt(networkGroupId));
		if (sg != null) {
			List<SocietyGroupConnection> sgcList = DBOperations
					.getSocietyGroupConnections(sg);
			if (sgcList != null) {
				JSONObject groupzObj = new JSONObject();
				JSONArray groupzArray = new JSONArray();
				for (SocietyGroupConnection sgc : sgcList) {
					System.out.println("GroupzId:" + sgc.getApartment().getId()
							+ "GroupzName:" + sgc.getApartment().getName()
							+ " & GroupzCode:"
							+ sgc.getApartment().getSocietyCode());
					JSONObject sgcObj = new JSONObject();
					sgcObj.put("groupzcode", sgc.getApartment()
							.getSocietyCode());
					sgcObj.put("groupzid", sgc.getApartment().getId());
					sgcObj.put("groupzname", sgc.getApartment().getName());
					groupzArray.add(sgcObj);
				}				
				groupzListResponse = processSuccessJSONString(serviceType,
						functionType, groupzArray);
			} else {
				groupzListResponse = processErrorJSONString(
						PropertiesUtil.getProperty("groupid_noaccount_code"),
						PropertiesUtil.getProperty("groupid_noaccount_message"));
			}
		} else {
			groupzListResponse = processErrorJSONString(
					PropertiesUtil.getProperty("groupid_invalid_code"),
					PropertiesUtil.getProperty("groupid_invalid_message"));
		}
		return groupzListResponse;
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
			String functionType, JSONArray dataArray) {
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
		contentJSON.put("groupzlist", dataArray);
		sucessRespJSON.put("response", contentJSON);
		sucessJSON.put("json", sucessRespJSON);
		sucessJSONString = sucessJSON.toString();
		return sucessJSONString;
	}

}
