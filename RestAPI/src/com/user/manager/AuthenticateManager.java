package com.user.manager;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.SocietyGroup;
import com.apartment.database.tables.SocietyGroupNetworkAdminMapping;
import com.apartment.database.tables.SocietyProfile;
import com.apartment.database.tables.User;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.session.AuthenticationManager;
import com.apartment.util.Utils;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

public class AuthenticateManager {

	RestUtils restUtils = new RestUtils();

	public String authenticate(HttpServletRequest request, String authRequest) {
		String authResponse = "";
		JSONObject requestJson = JSONObject.fromObject(authRequest);
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
				authResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return authResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				authResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return authResponse;
			}
			if (functionType.equalsIgnoreCase(PropertiesUtil
					.getProperty("authentication")) == true) {
				if (requestJson.getJSONObject("json").getJSONObject("request")
						.containsKey("userdata") == true) {
					JSONObject userData = requestJson.getJSONObject("json")
							.getJSONObject("request").getJSONObject("userdata");
					authResponse = checkValid(serviceType, functionType,
							userData);
					System.out.println("Authentication Response:"
							+ authResponse.toString());
				} else {
					authResponse = processErrorJSONString(
							PropertiesUtil
									.getProperty("invalidfunctionType_code"),
							PropertiesUtil
									.getProperty("invalidfunctionType_message"));
					return authResponse;
				}

			} else {
				authResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return authResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in json formation");
			authResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));

		}
		return authResponse;
	}

	public String checkValid(String serviceType, String functionType,
			JSONObject userData) {
		String validResponse = "";
		try {
			String username = userData.getString("username");
			String password = userData.getString("password");
			System.out.println("Calling authentication");
			User user = AuthenticationManager.authenticate(username, password);
			System.out.println("After authenticating");
			if (user != null && user.isEnabled() && user.getPassword() != null
					&& user.getPassword().equals(password)) {
				List<UserFlatMapping> ufmList = DBOperations.getUserFlats(user);
				if ((ufmList == null || ufmList.size() == 0)) {
					System.out.println("Invalid username and passowrd");
					validResponse = processErrorJSONString(
							PropertiesUtil.getProperty("auth_failed_code"),
							PropertiesUtil.getProperty("auth_failed_message"));
					System.out.println("Valid Response :" + validResponse);
					return validResponse;
				} else {
					JSONArray userArray = new JSONArray();
					JSONArray groupzArray = new JSONArray();
					JSONArray jsonArray5 = new JSONArray();
					JSONArray jsonArray6 = new JSONArray();
					JSONObject userObj = new JSONObject();
					JSONObject gpzObj = new JSONObject();
					JSONObject jo = new JSONObject();
					JSONObject job = new JSONObject();
					for (UserFlatMapping ufm : ufmList) {
						String responseJsonString = "";
						if (ufm.getFlat().getApartment().isEnabled() == false
								&& responseJsonString.trim().length() == 0) {
							System.out.println("APT is disabled");
							validResponse = processErrorJSONString(
									PropertiesUtil
											.getProperty("auth_failed_code"),
									PropertiesUtil
											.getProperty("auth_failed_message"));
							System.out.println("Valid Response :"
									+ validResponse);
							responseJsonString = "failure";
						}
						if (ufm.getFlat().getStartDate() != null
								&& responseJsonString.trim().length() == 0) {
							if (ufm.getFlat().getStartDate().after(new Date())) {
								System.out.println("Login date is not correct");
								validResponse = processErrorJSONString(
										PropertiesUtil
												.getProperty("auth_failed_date_code"),
										PropertiesUtil.getProperty("Your access will be automatically enabled on "
												+ Utils.formatDateOnly(ufm
														.getFlat()
														.getStartDate())
												+ ", you can contact support@groupz.in for any queries"));
								System.out.println("Valid Response :"
										+ validResponse);
								responseJsonString = "failure";
							}
						}
						if (ufm.isEnabled() == false
								&& responseJsonString.trim().length() == 0) {
							System.out.println("UFM is disabled");
							validResponse = processErrorJSONString(
									PropertiesUtil
											.getProperty("auth_failed_code"),
									PropertiesUtil
											.getProperty("auth_failed_message"));
							System.out.println("Valid Response :"
									+ validResponse);
							responseJsonString = "failure";
						}
						if (responseJsonString.trim().length() == 0) {
							userObj.put("groupzid", ufm.getFlat()
									.getApartment().getId());
							userObj.put("groupzcode", ufm.getFlat()
									.getApartment().getSocietyCode());
							userObj.put("userflatmappingid", ufm.getId());
							userObj.put("groupztype", ufm.getFlat()
									.getApartment().getApartmentType());
							userObj.put("flatid", ufm.getFlat().getId());
							userObj.put("division", ufm.getFlat()
									.getBlockStreetDetails());
							if (ufm.getFlat().getSubDivision() == null
									|| ufm.getFlat().getSubDivision().length() == 0
									|| ufm.getFlat().getSubDivision()
											.equalsIgnoreCase("null")) {
								userObj.put("subdivision", "");

							} else {
								userObj.put("subdivision", ufm.getFlat()
										.getSubDivision());
							}
							userObj.put("registeredpersonname", ufm.getFlat()
									.getRegisteredPerson().getName());
							userObj.put("rolename", ufm.getRole().getRoleName());
							userObj.put("rolevalue", ufm.getRole()
									.getRoleValue());
							// newly added fields starts on 09-10-2013
							userObj.put("roleid", ufm.getRole().getId());
							userObj.put("groupzadmin", ufm.getRole()
									.isManageUsers());
							List<SocietyGroup> groups = DBOperations
									.getSocietyGroups(ufm.getFlat()
											.getApartment());
							if (groups != null && groups.size() > 0) {
								String groupzString = getSocietyGroupsInString(groups);
								System.out.println("Groupz List:"
										+ groupzString);
								List<SocietyGroupNetworkAdminMapping> networkAdminList = DBOperations
										.getSocietyGroupNetworkAdminMappingsList(
												groupzString, ufm.getId());
								for (SocietyGroup sg : groups) {

									if (sg != null) {
										jo.put("networkgroupid", sg.getId());

									} else {
										jo.put("networkgroupid", "");
									}
									jsonArray5.add(jo);
								}
								if (networkAdminList != null
										&& networkAdminList.size() > 0) {
									for (SocietyGroupNetworkAdminMapping sgn : networkAdminList) {
										if (sgn != null) {
											job.put("networkgroupadminid", sgn
													.getSocietyGroup().getId());
											// job.put("NetworkGroupAdminid",
											// sgn
											// .getSocietyGroup().getId());
										} else {
											job.put("networkgroupadminid", "");
										}
										jsonArray6.add(job);
									}
								} else {
									job.put("networkgroupadminid", "");
									jsonArray6.add(job);
								}
							} else {
								job.put("networkgroupadminid", "");
								jsonArray6.add(job);
								jo.put("networkgroupid", "");
								jsonArray5.add(jo);
							}
							userObj.put("networkgrouplist",
									jsonArray5.toString());
							jsonArray5.clear();
							userObj.put("networkadminlist",
									jsonArray6.toString());
							jsonArray6.clear();
							userObj.put("profilephotoid", ufm.getFlat()
									.getRegisteredPerson().getId());
							userArray.add(userObj);
							SocietyProfile sp = DBOperations
									.getSocietyProfile(ufm.getFlat()
											.getApartment().getId());
							if (sp != null) {
								gpzObj.put("groupzid", sp.getApartmentId());
								gpzObj.put("groupzcode", ufm.getFlat()
										.getApartment().getSocietyCode());
								gpzObj.put("advertisementsenabled",
										sp.isAdvertisementsEnabled());
								gpzObj.put("albumsenabled",
										sp.isAlbumsEnabled());
								gpzObj.put("announcementsenabled",
										sp.isAnnouncementsEnabled());
								gpzObj.put("banneradsenabled",
										sp.isBannerAdsEnabled());
								gpzObj.put("classifiedsearchenabled",
										sp.isClassifiedSearchEnabled());
								gpzObj.put("classifiedsenabled",
										sp.isClassifiedsEnabled());
								gpzObj.put("contactsenabled",
										sp.isContactsEnabled());
								gpzObj.put("contactsharingenabled",
										sp.isContactSharingEnabled());
								gpzObj.put("displayproductlogo",
										sp.isDisplayProductLogo());
								gpzObj.put("documentssharingenabled",
										sp.isDocumentsSharingEnabled());
								gpzObj.put("duesenabled", sp.isDuesEnabled());
								gpzObj.put("emailenabled", sp.isEmailEnabled());
								gpzObj.put("familyinformationenabled",
										sp.isFamilyInformationEnabled());
								gpzObj.put("helpersenabled",
										sp.isHelpersEnabled());
								gpzObj.put("issuesenabled",
										sp.isIssuesEnabled());
								gpzObj.put("meetingsenabled",
										sp.isMeetingsEnabled());
								gpzObj.put(
										"memberssearchacrosssocietyenabled",
										sp.isMemberSearchAccrossSocietyEnabled());
								gpzObj.put("memberssearchenabled",
										sp.isMemberSearchEnabled());
								gpzObj.put("noticesenabled",
										sp.isNoticesEnabled());
								gpzObj.put("plannerenabled",
										sp.isPlannerEnabled());
								gpzObj.put("scrollingadsenabled",
										sp.isScrollingAdsEnabled());
								gpzObj.put("smsenabled", sp.isSmsEnabled());
								gpzObj.put("surveysenabled",
										sp.isSurveysEnabled());
								groupzArray.add(gpzObj);
							}
						}
					}
					if (userArray.size() > 0 && groupzArray.size() > 0) {
						validResponse = processSuccessJSONString(serviceType,
								functionType, userArray, groupzArray);
						return validResponse;
					} else {
						return validResponse;
					}

				}
			} else {
				System.out.println("Auth failed");
				validResponse = processErrorJSONString(
						PropertiesUtil.getProperty("auth_failed_code"),
						PropertiesUtil.getProperty("auth_failed_message"));
				System.out.println("Valid Response :" + validResponse);
				return validResponse;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error in json formation");
			validResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));

		}
		return validResponse;
	}

	private String getSocietyGroupsInString(List<SocietyGroup> SGList) {

		if (SGList == null)
			return null;

		String aptsList;
		aptsList = "(";
		boolean firstApt = true;
		for (SocietyGroup sgp : SGList) {

			if (firstApt == true) {
				firstApt = false;
				aptsList += sgp.getId();
				continue;
			}
			aptsList += ("," + sgp.getId());
		}

		aptsList += ")";
		return aptsList;
	}

	public String processSuccessJSONString(String serviceType,
			String functionType, JSONArray userArray, JSONArray gpzArray) {
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
		contentJSON.put("user", userArray);
		contentJSON.put("groupz", gpzArray);
		sucessRespJSON.put("response", contentJSON);
		sucessJSON.put("json", sucessRespJSON);
		sucessJSONString = sucessJSON.toString();
		return sucessJSONString;
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

}
