package com.user.manager;

import java.security.acl.Permission;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Announcement;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.ApartmentSettings;
import com.apartment.database.tables.Group;
import com.apartment.database.tables.Permissions;
import com.apartment.database.tables.RoleDefinition;
import com.apartment.database.tables.SocietyGroup;
import com.apartment.database.tables.SocietyGroupReference;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.events.NotificationEvent;
import com.apartment.events.NotificationManager;
import com.apartment.modules.societygroup.SocietyGroupManager;
import com.apartment.modules.usermanagement.UserGroupManager;
import com.apartment.ui.jsf.beans.UserBean;
import com.apartment.util.DatabaseXmlHelper;
import com.apartment.util.Utils;
import com.user.operations.UserOperations;
import com.user.utils.IPAddressCheck;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class PostingDataManager {
	private RestUtils restUtils = new RestUtils();
	DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	String dataXMLResponse = "";

	public String getPostingDataResponse(String postingDataRequest,
			HttpServletRequest req) {
		String postingDataResponse = "";
		String serviceType = "";
		String functionType = "";
		String groupzCode = "";
		String ipAddress = "";
		String mobileNumber = "";
		String countryCode = "";
		String memberCode = "";
		String memberId = "";
		String includeContacts = "false";
		List<String> ipAddressList;
		IPAddressCheck ipCheck = new IPAddressCheck();
		JSONObject requestJson = JSONObject.fromObject(postingDataRequest);
		System.out.println("Posting Data Request:" + postingDataRequest);
		try {
			// json = (JSONObject) xmlSerializer.read(userSelectionRequest);
			serviceType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("servicetype");
			functionType = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("functiontype");
			System.out.println("ST:" + serviceType + ",FT:" + functionType);
			countryCode = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			System.out.println("1");
			mobileNumber = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("mobile")
					.getString("mobilenumber");
			System.out.println("2");
			memberCode = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("membercode");
			System.out.println("3");
			memberId = requestJson.getJSONObject("json")
					.getJSONObject("request").getString("memberid");
			System.out.println("4");
			if (requestJson.getJSONObject("json").getJSONObject("request")
					.containsKey("includecontacts") == true) {
				includeContacts = requestJson.getJSONObject("json")
						.getJSONObject("request").getString("includecontacts");
				System.out.println("5");
			}
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return postingDataResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return postingDataResponse;
			}
			if (restUtils.isEmpty(memberCode) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("emptymemberCode_code"),
						PropertiesUtil.getProperty("emptymemberCode_message"));
				return postingDataResponse;
			}
			if (restUtils.isEmpty(memberId) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("emptymemberId_code"),
						PropertiesUtil.getProperty("emptymemberId_message"));
				return postingDataResponse;
			}
			if (restUtils.isEmpty(countryCode) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return postingDataResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return postingDataResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return postingDataResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return postingDataResponse;
			}

			// newly added for groupz code list
			JSONObject gpzCodesObj = new JSONObject();
			JSONArray groupzCodesList = null;
			if (requestJson.getJSONObject("json").getJSONObject("request")
					.containsKey("groupzlist") == true) {
				gpzCodesObj = requestJson.getJSONObject("json")
						.getJSONObject("request").optJSONObject("groupzlist");
				System.out.println("6");

			} else {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("XMLRequest_code"),
						PropertiesUtil.getProperty("XMLRequest_message"));
			}
			System.out.println("7");
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			UserFlatMapping member = DBOperations
					.getUserFlatMappingById(memberId);
			if (member != null) {
				if (member.getRole().isCanRecordMessages() == false
						|| member.isEnabled() == false) {
					System.out.println("Cannot record");
					postingDataResponse = processErrorJSONString(
							PropertiesUtil.getProperty("invalidmemberId_code"),
							PropertiesUtil
									.getProperty("invalidmemberId_message"));
					return postingDataResponse;
				}
				if ((member.getFlat().getRegisteredPerson().getMobile()
						.equalsIgnoreCase(completeMobile) == false)
						|| (member.getFlat().getDoorNo()
								.equalsIgnoreCase(memberCode) == false)) {
					System.out.println("Mobile number not matched");
					postingDataResponse = processErrorJSONString(
							PropertiesUtil.getProperty("invalidmemberId_code"),
							PropertiesUtil
									.getProperty("invalidmemberId_message"));
					return postingDataResponse;
				}
			} else {
				System.out.println("Member id not matched");
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidmemberId_code"),
						PropertiesUtil.getProperty("invalidmemberId_message"));
				return postingDataResponse;
			}
			System.out.println("8");
			if (requestJson.getJSONObject("json").getJSONObject("request")
					.containsKey("data") == false) {
				System.out.println("No Data part");
				dataXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("urlList_tag_code"),
						PropertiesUtil.getProperty("urlList_tag_message"));
				System.out.println("Inside data part:" + dataXMLResponse);
				return dataXMLResponse;
			}
			if (requestJson.getJSONObject("json").getJSONObject("request")
					.getString("data") == null
					|| requestJson.getJSONObject("json")
							.getJSONObject("request").getString("data")
							.isEmpty() == true
					|| requestJson.getJSONObject("json")
							.getJSONObject("request").getString("data")
							.equalsIgnoreCase("[]") == true) {
				System.out.println("Data part IS EMPTY");
				dataXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("urlList_tag_code"),
						PropertiesUtil.getProperty("urlList_tag_message"));
				System.out.println("Inside data part:" + dataXMLResponse);
				return dataXMLResponse;
			}
			JSONObject dataPartJSON = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("data");

			if (requestJson.getJSONObject("json").getJSONObject("request")
					.containsKey("selection") == false) {
				System.out.println("No selection part");
				dataXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("urlList_tag_code"),
						PropertiesUtil.getProperty("urlList_tag_message"));
				System.out.println("Inside data part:" + dataXMLResponse);
				return dataXMLResponse;
			}
			JSONObject selectionJSON = requestJson.getJSONObject("json")
					.getJSONObject("request").getJSONObject("selection");

			// ipaddress validation
			ipAddress = req.getRemoteAddr();
			System.out.println("Ipaddress:" + ipAddress);
			ipAddressList = new ArrayList<String>();
			Apartment apt = member.getFlat().getApartment();
			if (apt != null) {
				System.out.println("Inside APT:" + apt.getName());
				String stList = (String) DBOperations.getIpAddresses(
						apt.getId()).get(0);
				if (stList != null) {
					StringTokenizer st = new StringTokenizer(stList, "\n");
					while (st.hasMoreTokens()) {
						ipAddressList.add(st.nextToken());
					}
				}
				if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
					if (serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == true
							&& functionType
									.equalsIgnoreCase(PropertiesUtil
											.getProperty("submitselectionforivrrecord")) == true) {
						SocietyGroup networkAdmin = SocietyGroupManager
								.getAdministeredGroup(member);
						if (networkAdmin != null) {
							System.out.println("Network shared recording");
							Collection<Apartment> selectedGroupzList = new ArrayList<Apartment>();
							JSONArray groupzList = null;
							Object groupzObject = requestJson
									.getJSONObject("json")
									.getJSONObject("request").get("groupzlist");
							System.out.println("Inside ntwork sharged 1");
							if (groupzObject instanceof JSONArray) {
								groupzList = requestJson.getJSONObject("json")
										.getJSONObject("request")
										.getJSONArray("groupzlist");
								System.out.println("Groupz JSON Aray:"
										+ groupzList.toString(3));
								for (int i = 0; i < groupzList.size(); i++) {
									System.out.println("groupz code:"
											+ groupzList.getJSONObject(i)
													.getString("groupzcode"));
									Apartment selectedApt = DBOperations
											.getApartmentByCode(groupzList
													.getJSONObject(i)
													.getString("groupzcode"));
									if (selectedApt != null) {
										selectedGroupzList.add(selectedApt);
									}
								}
							} else {
								System.out.println("Inside ntwork sharged 2");
								String gpzCode = requestJson
										.getJSONObject("json")
										.getJSONObject("request")
										.getJSONObject("groupzlist")
										.getString("groupzcode");
								Apartment selectedApt = DBOperations
										.getApartmentByCode(gpzCode);
								if (selectedApt != null) {
									selectedGroupzList.add(selectedApt);
								}
							}
							System.out.println("Inside ntwork sharged 3");
							Announcement ann = new Announcement();
							Permissions permissions = new Permissions();
							ann.setApartmentId(member.getFlat().getApartment()
									.getId());
							ann.setPostedByUserMappingId(member.getId());
							ann.setModifiedByUserMappingId(member.getId());
							ann.setSharedByUserMappingId(member.getId());
							ann.setApproval(true);
							ann.setApprovalDate(new Date());
							ann.setMedia(true);
							Date postedDate = new Date();
							String postedTimeStr = Utils
									.padThisTo24HrFormat(postedDate.getHours()
											+ ":" + postedDate.getMinutes());
							ann.setPostedDate(postedDate);
							ann.setPostedTime(postedTimeStr);
							SocietyGroupReference sgr = new SocietyGroupReference();
							sgr.setFreeFlowToGroupzMembers(true);
							sgr.setSocietyGroup(networkAdmin);
							sgr.setSocities(selectedGroupzList);
							sgr.setRawXml(postingDataRequest);
							sgr.setSharedByUserMappingId(member.getId());
							sgr.save();
							// ann.setSocietyGroupReference(sgr);
							System.out.println("Member APT:"
									+ member.getFlat().getApartment()
									+ "hascode value:"
									+ member.getFlat().getApartment()
											.hashCode());
							Iterator<Apartment> itr = selectedGroupzList
									.iterator();
							while (itr.hasNext()) {
								Apartment ap = itr.next();
								System.out.println("AP:" + ap.getId());
								if (ap.getId() == member.getFlat()
										.getApartment().getId()) {
									String jsonString = userGroupJSONString(
											member.getFlat().getApartment(),
											selectionJSON);
									System.out
											.println("JSON STRING IN NETWROK:"
													+ jsonString);
									// ann.setXmlUserGroups(xmlString);
									//permissions.setUserSelectionXML(jsonString);
									permissions.setUserSelectionJSON(jsonString);
									itr.remove();
									break;
								}
							}
							System.out.println("Inside ntwork sharged 4");
							Announcement annDataPart = setDatapartFromJSON(dataPartJSON);
							System.out.println("Inside ntwork sharged 5");
							if (annDataPart == null) {
								System.out
										.println("Data XML Response in local shared:"
												+ dataXMLResponse);
								return dataXMLResponse;
							}
							ann.setTitle("RecordedAnnouncement-"
									+ Utils.formatDate(new Date()));
							ann.setLink(annDataPart.getLink());
							ann.setEndDate(annDataPart.getEndDate());
							ann.setEndTime(annDataPart.getEndTime());
							ann.setSharedByUserMappingId(member.getId());
							ann.save();
							// ann.setRootAnnouncement(ann);
							// ann.save();
							permissions.setSocietyGroupReference(sgr);
							permissions.setGroupz(apt);
							permissions.setModuleReferenceId(ann.getId());
							permissions
									.setModuleType(permissions.MODULE_ANNOUCEMENTS);
							permissions.save();
							String smsMessage = "Testing recorded sms message";
							String emailMessageTitle = "Testing recorded email title";
							String emailMessage = "Testing recorded email message";
							List<UserFlatMapping> selectedUserMembers = (List<UserFlatMapping>) getUserFlatMappingsFromUserGroupsJSON(
									permissions.getUserSelectionJSON(),
									DBOperations.getUfm(ann
											.getPostedByUserMappingId()), apt);
							System.out
									.println("Selected USER MEMBERS in network shared:"
											+ selectedUserMembers.size());
							if (selectedUserMembers != null
									&& selectedUserMembers.isEmpty() == false
									&& selectedUserMembers.size() > 0) {
								System.out
										.println("Sending remainders to posted user xml groups starts");
								sendRemainder(selectedUserMembers, member,
										includeContacts, smsMessage,
										emailMessageTitle, emailMessage);
								System.out
										.println("Sending remainders to posted user xml groups ends");
							}
							System.out.println("Selected groupz List:"
									+ selectedGroupzList.size());
							if (selectedGroupzList != null
									&& selectedGroupzList.isEmpty() == false) {

								for (Apartment selectedGroupz : selectedGroupzList) {
									System.out
											.println("Groupz name in for loop for network recording:"
													+ selectedGroupz.getName());
									publishXMLForNetworkIVRRecord(serviceType,
											functionType, member,
											selectedGroupz, includeContacts,
											ann, selectionJSON, sgr);
								}
							}
							EmailAndSmsManager ems = new EmailAndSmsManager();
							System.out
									.println("---------------->Sending remainder for posted user also"
											+ member);
							ems.sendEmailAndSms(emailMessageTitle,
									emailMessage, smsMessage, member.getFlat()
											.getRegisteredPerson().getName(),
									member.getUser().getEmail(), member
											.getFlat().getRegisteredPerson()
											.getMobile(), member, member);
							System.out
									.println("--===========>>>After sending remainder to posted user"
											+ member);
							postingDataResponse = processSuccessJSONString(
									serviceType, functionType);
							return postingDataResponse;
						} else {
							System.out.println("Local shared recording");
							Object groupzObject = requestJson
									.getJSONObject("json")
									.getJSONObject("request").get("groupzlist");
							System.out.println("Inside ntwork sharged 1");
							JSONArray groupzList=null;
							if (groupzObject instanceof JSONArray) {
								groupzList = requestJson.getJSONObject("json")
										.getJSONObject("request")
										.getJSONArray("groupzlist");
								System.out.println("Groupz JSON Aray:"
										+ groupzList.toString(3));
								for (int i = 0; i < groupzList.size(); i++) {
									System.out.println("groupz code:"
											+ groupzList.getJSONObject(i)
													.getString("groupzcode"));
									groupzCode=groupzList.getJSONObject(i)
											.getString("groupzcode");
									break;
								}
							}else{															
							groupzCode = (String) requestJson
									.getJSONObject("json")
									.getJSONObject("request")
									.getJSONObject("groupzlist")
									.getString("groupzcode");
							}
							postingDataResponse = publishXMLForLocalIVRRecord(
									serviceType, functionType, member,
									groupzCode, includeContacts, selectionJSON,
									dataPartJSON);
							return postingDataResponse;
						}

					} else {
						postingDataResponse = processErrorJSONString(
								PropertiesUtil
										.getProperty("invalidfunctionType_code"),
								PropertiesUtil
										.getProperty("invalidfunctionType_message"));
						return postingDataResponse;
					}
				} else {
					postingDataResponse = processErrorJSONString(
							PropertiesUtil.getProperty("accessdenied_code"),
							PropertiesUtil.getProperty("accessdenied_message"));
					return postingDataResponse;
				}
			} else {
				postingDataResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidmemberId_code"),
						PropertiesUtil.getProperty("invalidmemberId_message"));
				return postingDataResponse;
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("Error in json formation");
			postingDataResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return postingDataResponse;
	}

	/*
	 * public String getPostingdataResponses(String userSelectionRequest,
	 * HttpServletRequest req) { String postingDataResponse = ""; String
	 * serviceType = ""; String functionType = ""; String groupzCode = "";
	 * String ipAddress = ""; String mobileNumber = ""; String countryCode = "";
	 * String memberCode = ""; String memberId = ""; String includeContacts =
	 * "false"; List<String> ipAddressList; IPAddressCheck ipCheck = new
	 * IPAddressCheck();
	 * 
	 * XMLSerializer xmlSerializer = new XMLSerializer(); JSONObject json = new
	 * JSONObject(); try { json = (JSONObject)
	 * xmlSerializer.read(userSelectionRequest); serviceType =
	 * json.getJSONObject("request") .getString("servicetype"); functionType =
	 * json.getJSONObject("request").getString( "functiontype");
	 * 
	 * countryCode = json.getJSONObject("request").getJSONObject("mobile")
	 * .getString("countrycode"); mobileNumber = json.getJSONObject("request")
	 * .getJSONObject("mobile").getString("mobilenumber"); memberCode =
	 * json.getJSONObject("request").getString("membercode"); memberId =
	 * json.getJSONObject("request").getString("memberid"); if
	 * (json.getJSONObject("request").containsKey("includecontacts") == true) {
	 * includeContacts = json.getJSONObject("request").getString(
	 * "includecontacts"); } if (restUtils.isEmpty(serviceType) == false ||
	 * serviceType.equalsIgnoreCase(PropertiesUtil .getProperty("ivrselection"))
	 * == false) { postingDataResponse = processError(
	 * PropertiesUtil.getProperty("invalidserviceType_code"), PropertiesUtil
	 * .getProperty("invalidserviceType_message")); return postingDataResponse;
	 * } if (restUtils.isEmpty(functionType) == false) { postingDataResponse =
	 * processError( PropertiesUtil.getProperty("invalidfunctionType_code"),
	 * PropertiesUtil .getProperty("invalidfunctionType_message")); return
	 * postingDataResponse; } if (restUtils.isEmpty(memberCode) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("emptymemberCode_code"),
	 * PropertiesUtil.getProperty("emptymemberCode_message")); return
	 * postingDataResponse; } if (restUtils.isEmpty(memberId) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("emptymemberId_code"),
	 * PropertiesUtil.getProperty("emptymemberId_message")); return
	 * postingDataResponse; } if (restUtils.isEmpty(countryCode) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("countryempty_code"),
	 * PropertiesUtil.getProperty("countryempty_message")); return
	 * postingDataResponse; } if (Utils.isNumber(countryCode) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("invalidcountry_code"),
	 * PropertiesUtil.getProperty("invalidcountry_message")); return
	 * postingDataResponse; } if (restUtils.isEmpty(mobileNumber) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("Mobileempty_code"),
	 * PropertiesUtil.getProperty("Mobileempty_message")); return
	 * postingDataResponse; } if (Utils.isNumber(mobileNumber) == false) {
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("invalidmobile_code"),
	 * PropertiesUtil.getProperty("invalidmobile_message")); return
	 * postingDataResponse; }
	 * 
	 * // newly added for groupz code list JSONObject gpzCodesObj = new
	 * JSONObject(); JSONArray groupzCodesList = null; if
	 * (json.getJSONObject("request").containsKey("groupzlist") == true) {
	 * gpzCodesObj = json.getJSONObject("request").optJSONObject( "groupzlist");
	 * 
	 * } else { postingDataResponse = processError(
	 * PropertiesUtil.getProperty("XMLRequest_code"),
	 * PropertiesUtil.getProperty("XMLRequest_message")); } String
	 * completeMobile = "+" + countryCode + "." + mobileNumber; UserFlatMapping
	 * member = DBOperations .getUserFlatMappingById(memberId); if (member !=
	 * null) { if (member.getRole().isCanRecordMessages() == false ||
	 * member.isEnabled() == false) { System.out.println("Cannot record");
	 * postingDataResponse = processError(
	 * PropertiesUtil.getProperty("invalidmemberId_code"), PropertiesUtil
	 * .getProperty("invalidmemberId_message")); return postingDataResponse; }
	 * if ((member.getFlat().getRegisteredPerson().getMobile()
	 * .equalsIgnoreCase(completeMobile) == false) ||
	 * (member.getFlat().getDoorNo() .equalsIgnoreCase(memberCode) == false)) {
	 * System.out.println("Mobile number not matched"); postingDataResponse =
	 * processError( PropertiesUtil.getProperty("invalidmemberId_code"),
	 * PropertiesUtil .getProperty("invalidmemberId_message")); return
	 * postingDataResponse; } } else {
	 * System.out.println("Member id not matched"); postingDataResponse =
	 * processError( PropertiesUtil.getProperty("invalidmemberId_code"),
	 * PropertiesUtil.getProperty("invalidmemberId_message")); return
	 * postingDataResponse; } // ipaddress validation ipAddress =
	 * req.getRemoteAddr(); System.out.println("Ipaddress:" + ipAddress);
	 * ipAddressList = new ArrayList<String>(); Apartment apt =
	 * member.getFlat().getApartment(); if (apt != null) {
	 * System.out.println("Inside APT:" + apt.getName()); String stList =
	 * (String) DBOperations.getIpAddresses( apt.getId()).get(0); if (stList !=
	 * null) { StringTokenizer st = new StringTokenizer(stList, "\n"); while
	 * (st.hasMoreTokens()) { ipAddressList.add(st.nextToken()); } } if
	 * (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) { if
	 * (serviceType.equalsIgnoreCase(PropertiesUtil
	 * .getProperty("ivrselection")) == true && functionType
	 * .equalsIgnoreCase(PropertiesUtil
	 * .getProperty("submitselectionforivrrecord")) == true) { SocietyGroup
	 * networkAdmin = SocietyGroupManager .getAdministeredGroup(member); if
	 * (networkAdmin != null) { System.out.println("Network shared recording");
	 * Collection<Apartment> selectedGroupzList = new ArrayList<Apartment>();
	 * JSONArray groupzList = null; Object groupzObject =
	 * json.getJSONObject("request") .getJSONObject("groupzlist")
	 * .get("groupzcode"); if (groupzObject instanceof JSONArray) { groupzList =
	 * json.getJSONObject("request") .getJSONObject("groupzlist")
	 * .getJSONArray("groupzcode"); System.out.println("Groupz JSON Aray:" +
	 * groupzList.toString(3)); for (int i = 0; i < groupzList.size(); i++) {
	 * System.out.println("groupz code:" + groupzList.getString(i)); Apartment
	 * selectedApt = DBOperations .getApartmentByCode(groupzList .getString(i));
	 * if (selectedApt != null) { selectedGroupzList.add(selectedApt); } } }
	 * else { String gpzCode = json.getJSONObject("request")
	 * .getJSONObject("groupzlist") .getJSONObject("groupzcode")
	 * .getString("element"); Apartment selectedApt = DBOperations
	 * .getApartmentByCode(gpzCode); if (selectedApt != null) {
	 * selectedGroupzList.add(selectedApt); } }
	 * 
	 * Announcement ann = new Announcement(); Permissions permissions = new
	 * Permissions(); ann.setApartmentId(member.getFlat().getApartment()
	 * .getId()); ann.setPostedByUserMappingId(member.getId());
	 * ann.setModifiedByUserMappingId(member.getId());
	 * ann.setSharedByUserMappingId(member.getId()); ann.setApproval(true);
	 * ann.setApprovalDate(new Date()); ann.setMedia(true); Date postedDate =
	 * new Date(); String postedTimeStr = Utils
	 * .padThisTo24HrFormat(postedDate.getHours() + ":" +
	 * postedDate.getMinutes()); ann.setPostedDate(postedDate);
	 * ann.setPostedTime(postedTimeStr); SocietyGroupReference sgr = new
	 * SocietyGroupReference(); sgr.setFreeFlowToGroupzMembers(true);
	 * sgr.setSocietyGroup(networkAdmin); sgr.setSocities(selectedGroupzList);
	 * sgr.setRawXml(userSelectionRequest);
	 * sgr.setSharedByUserMappingId(member.getId()); sgr.save(); //
	 * ann.setSocietyGroupReference(sgr); System.out.println("Member APT:" +
	 * member.getFlat().getApartment() + "hascode value:" +
	 * member.getFlat().getApartment() .hashCode()); for (Apartment at :
	 * selectedGroupzList) { System.out.println("ataap:" + at + "hascode:" +
	 * at.hashCode()); } Iterator<Apartment> itr = selectedGroupzList
	 * .iterator(); while (itr.hasNext()) { Apartment ap = itr.next();
	 * System.out.println("AP:" + ap.getId()); if (ap.getId() ==
	 * member.getFlat() .getApartment().getId()) { String xmlString =
	 * userGroupXMLString( member.getFlat().getApartment(), json); //
	 * ann.setXmlUserGroups(xmlString);
	 * permissions.setUserSelectionXML(xmlString); itr.remove(); break; } }
	 * Announcement annDataPart = setDatapart(json); if (annDataPart == null) {
	 * System.out .println("Data XML Response in local shared:" +
	 * dataXMLResponse); return dataXMLResponse; }
	 * ann.setTitle("RecordedAnnouncement-" + Utils.formatDate(new Date()));
	 * ann.setLink(annDataPart.getLink());
	 * ann.setEndDate(annDataPart.getEndDate());
	 * ann.setEndTime(annDataPart.getEndTime());
	 * ann.setSharedByUserMappingId(member.getId()); ann.save(); //
	 * ann.setRootAnnouncement(ann); // ann.save();
	 * permissions.setSocietyGroupReference(sgr); permissions.setApartment(apt);
	 * permissions.setModuleReferenceId(ann.getId()); permissions
	 * .setModuleType(permissions.MODULE_ANNOUCEMENTS); permissions.save();
	 * String smsMessage = "Testing recorded sms message"; String
	 * emailMessageTitle = "Testing recorded email title"; String emailMessage =
	 * "Testing recorded email message"; List<UserFlatMapping>
	 * selectedUserMembers = (List<UserFlatMapping>) UserGroupManager
	 * .getUserFlatMappingsFromUserGroupsRawXML(
	 * permissions.getUserSelectionXML(), DBOperations.getUfm(ann
	 * .getPostedByUserMappingId()), apt); System.out
	 * .println("Selected USER MEMBERS in network shared:" +
	 * selectedUserMembers.size()); if (selectedUserMembers != null &&
	 * selectedUserMembers.isEmpty() == false) { System.out
	 * .println("Sending remainders to posted user xml groups starts");
	 * sendRemainder(selectedUserMembers, member, includeContacts, smsMessage,
	 * emailMessageTitle, emailMessage); System.out
	 * .println("Sending remainders to posted user xml groups ends"); }
	 * System.out.println("Selected groupz List:" + selectedGroupzList.size());
	 * if (selectedGroupzList != null && selectedGroupzList.isEmpty() == false)
	 * {
	 * 
	 * for (Apartment selectedGroupz : selectedGroupzList) { System.out
	 * .println("Groupz name in for loop for network recording:" +
	 * selectedGroupz.getName()); publishXMLForNetworkIVRRecord(serviceType,
	 * functionType, member, selectedGroupz, includeContacts, ann, json, sgr); }
	 * } EmailAndSmsManager ems = new EmailAndSmsManager(); System.out
	 * .println("---------------->Sending remainder for posted user also" +
	 * member); ems.sendEmailAndSms(emailMessageTitle, emailMessage, smsMessage,
	 * member.getFlat() .getRegisteredPerson().getName(),
	 * member.getUser().getEmail(), member .getFlat().getRegisteredPerson()
	 * .getMobile(), member, member); System.out
	 * .println("--===========>>>After sending remainder to posted user" +
	 * member); postingDataResponse = processSuccessString( serviceType,
	 * functionType); return postingDataResponse; } else {
	 * System.out.println("Local shared recording"); groupzCode = (String)
	 * json.getJSONObject("request") .getJSONObject("groupzlist")
	 * .getJSONObject("groupzcode") .getString("element"); postingDataResponse =
	 * publishXMLForLocalIVRRecord( serviceType, functionType, member,
	 * groupzCode, includeContacts, json); return postingDataResponse; }
	 * 
	 * } else { postingDataResponse = processError( PropertiesUtil
	 * .getProperty("invalidfunctionType_code"), PropertiesUtil
	 * .getProperty("invalidfunctionType_message")); return postingDataResponse;
	 * } } else { postingDataResponse = processError(
	 * PropertiesUtil.getProperty("accessdenied_code"),
	 * PropertiesUtil.getProperty("accessdenied_message")); return
	 * postingDataResponse; } } else { postingDataResponse = processError(
	 * PropertiesUtil.getProperty("invalidmemberId_code"),
	 * PropertiesUtil.getProperty("invalidmemberId_message")); return
	 * postingDataResponse; } } catch (Exception e) { // TODO: handle exception
	 * e.printStackTrace(); postingDataResponse = processError(
	 * PropertiesUtil.getProperty("XMLRequest_code"),
	 * PropertiesUtil.getProperty("XMLRequest_message")); } return
	 * postingDataResponse; }
	 */
	/*
	 * // publish IVR record for network public String
	 * publishXMLForNetworkIVRRecordss(String serviceType, String functionType,
	 * UserFlatMapping member, Apartment apt, String includeContacts,
	 * Announcement ann, JSONObject json, SocietyGroupReference sgr) { String
	 * publishResponse = ""; try { String userXMLGroups =
	 * userGroupXMLString(apt, json); System.out.println("User XML groups:" +
	 * userXMLGroups); // newly started for saving starts Announcement
	 * recordedAnnouncement = new Announcement();
	 * recordedAnnouncement.setApartmentId(apt.getId());
	 * recordedAnnouncement.setXmlUserGroups(userXMLGroups);
	 * recordedAnnouncement.setRootAnnouncement(ann); //
	 * recordedAnnouncement.save(); Permissions perm = new Permissions();
	 * perm.setSocietyGroupReference(sgr); perm.setApartment(apt);
	 * perm.setModuleReferenceId(ann.getId());
	 * perm.setModuleType(perm.MODULE_ANNOUCEMENTS);
	 * perm.setUserSelectionXML(userXMLGroups); perm.save(); String smsMessage =
	 * "Testing recorded sms message"; String emailMessageTitle =
	 * "Testing recorded email title"; String emailMessage =
	 * "Testing recorded email message"; List<UserFlatMapping>
	 * selectedUserMembers = (List<UserFlatMapping>) UserGroupManager
	 * .getUserFlatMappingsFromUserGroupsRawXML( perm.getUserSelectionXML(),
	 * DBOperations.getUfm(ann.getPostedByUserMappingId()), apt);
	 * System.out.println("Selected USER MEMBERS:" +
	 * selectedUserMembers.size()); sendRemainder(selectedUserMembers, member,
	 * includeContacts, smsMessage, emailMessageTitle, emailMessage); } catch
	 * (Exception e) { // TODO: handle exception e.printStackTrace();
	 * publishResponse = processError(
	 * PropertiesUtil.getProperty("XMLRequest_code"),
	 * PropertiesUtil.getProperty("XMLRequest_message")); } return
	 * publishResponse; }
	 */

	/*
	 * public Announcement setDatapart(JSONObject dataJSON) { Announcement ann =
	 * new Announcement(); String endDateStr = ""; try { if
	 * (dataJSON.getJSONObject("request").getString("data") == null ||
	 * dataJSON.getJSONObject("request").getString("data") .isEmpty() == true ||
	 * dataJSON.getJSONObject("request").getString("data")
	 * .equalsIgnoreCase("[]") == true) {
	 * System.out.println("Data part IS EMPTY"); dataXMLResponse = processError(
	 * PropertiesUtil.getProperty("urlList_tag_code"),
	 * PropertiesUtil.getProperty("urlList_tag_message"));
	 * System.out.println("Inside data part:" + dataXMLResponse); return null; }
	 * if (dataJSON.getJSONObject("request").getJSONObject("data")
	 * .containsKey("enddate") == true) { endDateStr =
	 * dataJSON.getJSONObject("request")
	 * .getJSONObject("data").getString("enddate"); if
	 * (restUtils.isEmpty(endDateStr) == false) { dataXMLResponse =
	 * processError( PropertiesUtil.getProperty("emptyenddate_code"),
	 * PropertiesUtil.getProperty("emptyenddate_message")); return null; } Date
	 * endDate = formatter.parse(endDateStr); if (endDate.before(new Date())) {
	 * dataXMLResponse = processError(
	 * PropertiesUtil.getProperty("enddate_invalid_code"), PropertiesUtil
	 * .getProperty("enddate_invalid_message")); return null; } } else {
	 * dataXMLResponse = processError(
	 * PropertiesUtil.getProperty("enddate_tag_code"),
	 * PropertiesUtil.getProperty("enddate_tag_message")); return null; }
	 * 
	 * if (dataJSON.getJSONObject("request").getJSONObject("data")
	 * .containsKey("urlsList") == true) {
	 * System.out.println("URL LIST Tag present"); System.out.println("URL TAG:"
	 * + dataJSON.getJSONObject("request")
	 * .getJSONObject("data").getString("urlsList")); if
	 * (dataJSON.getJSONObject("request").getJSONObject("data")
	 * .getString("urlsList") == null || dataJSON.getJSONObject("request")
	 * .getJSONObject("data").getString("urlsList") .isEmpty() == true ||
	 * dataJSON.getJSONObject("request")
	 * .getJSONObject("data").getString("urlsList") .equalsIgnoreCase("[]") ==
	 * true) { System.out.println("URL TAG LIST IS EMPTY"); dataXMLResponse =
	 * processError( PropertiesUtil.getProperty("urlList_empty_code"),
	 * PropertiesUtil.getProperty("urlList_empty_message")); return null; } }
	 * else { dataXMLResponse = processError(
	 * PropertiesUtil.getProperty("urlList_tag_code"),
	 * PropertiesUtil.getProperty("urlList_tag_message")); return null; }
	 * JSONObject langObj = new JSONObject(); langObj.put("language",
	 * dataJSON.getJSONObject("request")
	 * .getJSONObject("data").getString("urlsList")); String languageURL =
	 * langObj.toString(); Date endDate = formatter.parse(endDateStr);
	 * System.out.println("End Date after  parsing:" + endDate); String
	 * endDateTimeStr = Utils.padThisTo24HrFormat(endDate .getHours() + ":" +
	 * endDate.getMinutes()); ann.setLink(languageURL); ann.setEndDate(endDate);
	 * ann.setEndTime(endDateTimeStr); return ann; } catch (Exception e) { //
	 * TODO: handle exception e.printStackTrace(); dataXMLResponse =
	 * processError( PropertiesUtil.getProperty("XMLRequest_code"),
	 * PropertiesUtil.getProperty("XMLRequest_message")); } return ann; }
	 */

	// publishing IVR record for local
	/*
	 * public String publishXMLForLocalIVRRecordss(String serviceType, String
	 * functionType, UserFlatMapping member, String gpzCode, String
	 * includeContacts, JSONObject json) { String publishXMLResponse = "";
	 * StringBuffer publishXMLBuffer = new StringBuffer(); try { Apartment apt =
	 * DBOperations.getApartmentByCode(gpzCode); if (apt != null) { Announcement
	 * annDataPart = setDatapart(json); if (annDataPart == null) {
	 * System.out.println("Data XML Response in local shared:" +
	 * dataXMLResponse); return dataXMLResponse; }
	 * System.out.println("Ann data Part : End date:" + annDataPart.getEndDate()
	 * + ",EnddateStr:" + annDataPart.getEndTime() + ",Link:" +
	 * annDataPart.getLink()); String userXMLGroups = userGroupXMLString(apt,
	 * json); System.out.println("User XML groups:" + userXMLGroups);
	 * Announcement recordedAnnouncement = new Announcement();
	 * recordedAnnouncement.setApartmentId(apt.getId());
	 * recordedAnnouncement.setLink(annDataPart.getLink()); //
	 * recordedAnnouncement.setXmlUserGroups(userXMLGroups);
	 * recordedAnnouncement.setPostedByUserMappingId(member.getId()); Date
	 * postedDate = new Date(); String postedTimeStr =
	 * Utils.padThisTo24HrFormat(postedDate .getHours() + ":" +
	 * postedDate.getMinutes()); recordedAnnouncement.setPostedDate(postedDate);
	 * recordedAnnouncement.setPostedTime(postedTimeStr);
	 * recordedAnnouncement.setEndDate(annDataPart.getEndDate());
	 * recordedAnnouncement.setEndTime(annDataPart.getEndTime());
	 * recordedAnnouncement.setModifiedByUserMappingId(member.getId());
	 * recordedAnnouncement.setPostedByUserMappingId(member.getId()); if
	 * (member.getRole().isCanApprove() == true) {
	 * recordedAnnouncement.setApproval(true);
	 * recordedAnnouncement.setApprovalDate(new Date()); } else {
	 * recordedAnnouncement.setApproval(false); }
	 * recordedAnnouncement.setTitle("RecordedAnnouncement-" +
	 * Utils.formatDate(new Date())); recordedAnnouncement.setMedia(true);
	 * recordedAnnouncement.save(); Permissions permissions = new Permissions();
	 * permissions.setApartment(apt);
	 * permissions.setUserSelectionXML(userXMLGroups);
	 * permissions.setModuleReferenceId(recordedAnnouncement.getId());
	 * permissions.setModuleType(permissions.MODULE_ANNOUCEMENTS);
	 * permissions.save();
	 * 
	 * // recordedAnnouncement.setRootAnnouncement(recordedAnnouncement); //
	 * recordedAnnouncement.save(); // newly started for saving ends
	 * EmailAndSmsManager ems = new EmailAndSmsManager(); String smsMessage =
	 * "Testing recorded sms message"; String emailMessageTitle =
	 * "Testing recorded email title"; String emailMessage =
	 * "Testing recorded email message";
	 * 
	 * List<UserFlatMapping> selectedUserMembers = (List<UserFlatMapping>)
	 * UserGroupManager .getUserFlatMappingsFromUserGroupsRawXML(
	 * recordedAnnouncement.getXmlUserGroups(),
	 * DBOperations.getUfm(recordedAnnouncement .getPostedByUserMappingId()),
	 * apt); System.out.println("Selected USER MEMBERS:" +
	 * selectedUserMembers.size()); if (selectedUserMembers != null &&
	 * selectedUserMembers.isEmpty() == false) {
	 * sendRemainder(selectedUserMembers, member, includeContacts, smsMessage,
	 * emailMessageTitle, emailMessage); ems.sendEmailAndSms(emailMessageTitle,
	 * emailMessage, smsMessage, member.getFlat().getRegisteredPerson()
	 * .getName(), member.getUser().getEmail(),
	 * member.getFlat().getRegisteredPerson().getMobile(), member, member); }
	 * publishXMLResponse = processSuccessString(serviceType, functionType); }
	 * else { publishXMLResponse = processError(
	 * PropertiesUtil.getProperty("invalidgroupzcode_code"),
	 * PropertiesUtil.getProperty("invalidgroupzcode_message")); return
	 * publishXMLResponse; } } catch (Exception e) { // TODO: handle exception
	 * e.printStackTrace(); publishXMLResponse = processError(
	 * PropertiesUtil.getProperty("XMLRequest_code"),
	 * PropertiesUtil.getProperty("XMLRequest_message")); }
	 * 
	 * return publishXMLResponse; }
	 */

	/*
	 * public String userGroupXMLString(Apartment apt, JSONObject json) { String
	 * allUsers = json.getJSONObject("request").getString("allusers"); String
	 * filter = json.getJSONObject("request").getString("filter"); List<Object>
	 * selectedGroups = new ArrayList<Object>(); List<Object> selectedRoles =
	 * new ArrayList<Object>(); List<Object> selectedDivisions = new
	 * ArrayList<Object>(); List<Object> selectedSubDivisions = new
	 * ArrayList<Object>(); boolean allUsersFlag = false; boolean filterFlag =
	 * false; if (filter.equalsIgnoreCase("true") == true) { filterFlag = true;
	 * } else { filterFlag = false; }
	 * System.out.println("========All users Starts=========");
	 * System.out.println("All users=" + allUsers);
	 * System.out.println("========All users ends========="); if
	 * (allUsers.equalsIgnoreCase("true") == true) {
	 * System.out.println("ALL USERS IS TRUE"); allUsersFlag = true; } else {
	 * System.out.println("ALL USERS IS FALSE"); allUsersFlag = false; // for
	 * roles if (json.getJSONObject("request").containsKey("roles") == true) {
	 * String role = null; JSONArray rolesList = null; Object roleObject =
	 * json.getJSONObject("request") .getJSONObject("roles").get("role");
	 * System.out.println("========Roles Starts=========");
	 * System.out.println("Role OBJ:" + roleObject); if (roleObject instanceof
	 * JSONArray) { rolesList = json.getJSONObject("request")
	 * .getJSONObject("roles").getJSONArray("role");
	 * System.out.println("Role JSON Aray:" + rolesList.toString(3)); for (int i
	 * = 0; i < rolesList.size(); i++) { System.out.println("Roles:" +
	 * rolesList.getString(i)); String roleQry = "RoleName = '" +
	 * rolesList.getString(i) + "' and SocietyId=" + apt.getId(); RoleDefinition
	 * multipleRoles = (RoleDefinition) DBOperations
	 * .getSingleDatabaseObject(RoleDefinition.class, roleQry); if
	 * (multipleRoles != null) {
	 * selectedRoles.add(Integer.toString(multipleRoles .getId())); } } } else {
	 * System.out.println("Role  JSON:" + roleObject); role =
	 * json.getJSONObject("request").getJSONObject("roles")
	 * .getJSONObject("role").getString("element"); if
	 * (role.equalsIgnoreCase("all") == true) {
	 * System.out.println("All roles selected"); List<RoleDefinition>
	 * allRolesList = DBOperations .getRolesForSelectedUserSociety(apt); if
	 * (allRolesList != null) { for (RoleDefinition allRoles : allRolesList) {
	 * selectedRoles.add(Integer.toString(allRoles .getId())); } } } else {
	 * System.out.println("Only one role selected"); String roleQry =
	 * "RoleName = '" + role + "' and SocietyId=" + apt.getId(); RoleDefinition
	 * singeRole = (RoleDefinition) DBOperations
	 * .getSingleDatabaseObject(RoleDefinition.class, roleQry); if (singeRole !=
	 * null) { selectedRoles.add(Integer.toString(singeRole .getId())); } } }
	 * 
	 * if (selectedRoles == null || selectedRoles.isEmpty() == true ||
	 * selectedRoles.size() == 0) { System.out.println("Role list is empty");
	 * selectedRoles = null; }
	 * System.out.println("=======Roles Ends=========="); } // for divisions if
	 * (json.getJSONObject("request").containsKey("divisions") == true) { String
	 * division = null; Object divisionObject = json.getJSONObject("request")
	 * .getJSONObject("divisions").get("division"); JSONArray divisionsList =
	 * null; List<String> validDivList = (List<String>) apt
	 * .getAllDistinctBlocks(); if (validDivList == null || validDivList.size()
	 * == 0) { validDivList = new ArrayList<String>(); }
	 * System.out.println("======Divisions starts=========="); if
	 * (divisionObject instanceof JSONArray) { divisionsList =
	 * json.getJSONObject("request") .getJSONObject("divisions")
	 * .getJSONArray("division"); System.out.println("Division array json:" +
	 * divisionsList.toString(3)); for (int i = 0; i < divisionsList.size();
	 * i++) { String div = divisionsList.getString(i);
	 * System.out.println("Divisions:" + div); if (validDivList.contains(div) ==
	 * true) { System.out.println("Division Present--add");
	 * selectedDivisions.add(div); } else {
	 * System.out.println("Division Absent-- Don't add"); } } } else { division
	 * = json.getJSONObject("request") .getJSONObject("divisions")
	 * .getJSONObject("division").getString("element");
	 * System.out.println("Division Obj json:" + divisionObject); if
	 * (division.equalsIgnoreCase("all") == true) {
	 * System.out.println("All divisions selected"); Collection<String>
	 * allDivisionsList = apt .getDistinctBlocks(); if (allDivisionsList !=
	 * null) { selectedDivisions.addAll(allDivisionsList); } } else {
	 * System.out.println("Only one division selected"); if
	 * (validDivList.contains(division) == true) {
	 * System.out.println("Division Present--add");
	 * selectedDivisions.add(division); } else {
	 * System.out.println("Division Absent-- Don't add"); } } }
	 * 
	 * if (selectedDivisions == null || selectedDivisions.isEmpty() == true ||
	 * selectedDivisions.size() == 0) {
	 * System.out.println("Division list is empty"); selectedDivisions = null; }
	 * System.out.println("=======Divisions ends=========="); } // for groups if
	 * (json.getJSONObject("request").containsKey("groups") == true) { String
	 * group = null; Object groupsObject = json.getJSONObject("request")
	 * .getJSONObject("groups").get("group"); JSONArray groupsList = null;
	 * System.out.println("======Groups starts==========="); if (groupsObject
	 * instanceof JSONArray) { groupsList = json.getJSONObject("request")
	 * .getJSONObject("groups").getJSONArray("group");
	 * System.out.println("Groups json array:" + groupsList.toString(3));
	 * System.out.println("Groups JSON:" + groupsList.toString(3)); for (int i =
	 * 0; i < groupsList.size(); i++) { System.out.println("Groups:" +
	 * groupsList.getString(i)); String grpQry = "GroupName ='" +
	 * groupsList.getString(i) + "' and ApartmentId=" + apt.getId(); Group
	 * multipleGroups = (Group) DBOperations
	 * .getSingleDatabaseObject(Group.class, grpQry); if (multipleGroups !=
	 * null) { selectedGroups.add(Integer.toString(multipleGroups .getId())); }
	 * }
	 * 
	 * } else { System.out.println("Group json obj:" + groupsObject); group =
	 * json.getJSONObject("request")
	 * .getJSONObject("groups").getJSONObject("group") .getString("element"); if
	 * (group.equalsIgnoreCase("all") == true) {
	 * System.out.println("All groups selected"); List<Group> allGroupsList =
	 * DBOperations.getGroups(apt); if (allGroupsList != null) { for (Group
	 * allGroups : allGroupsList) {
	 * selectedGroups.add(Integer.toString(allGroups .getId())); } } } else {
	 * String grpQry = "GroupName ='" + group + "' and ApartmentId=" +
	 * apt.getId(); Group singleGroup = (Group) DBOperations
	 * .getSingleDatabaseObject(Group.class, grpQry); if (singleGroup != null) {
	 * selectedGroups.add(Integer.toString(singleGroup .getId())); }
	 * System.out.println("Only one group selected"); } } if (selectedGroups ==
	 * null || selectedGroups.isEmpty() == true || selectedGroups.size() == 0) {
	 * System.out.println("Groups list is empty"); selectedGroups = null; }
	 * System.out.println("=======Groups ends=========="); } // for subdivisions
	 * if (json.getJSONObject("request").containsKey("subdivisions") == true) {
	 * String subDivision = null; Object subDivisionObject =
	 * json.getJSONObject("request")
	 * .getJSONObject("subdivisions").get("subdivision"); JSONArray
	 * subDivisionsList = null;
	 * System.out.println("======SubDivisions starts=========="); List<String>
	 * validSubDivList = (List<String>) apt .getAllSubDivisions(); if
	 * (validSubDivList == null || validSubDivList.size() == 0) {
	 * validSubDivList = new ArrayList<String>(); } if (subDivisionObject
	 * instanceof JSONArray) { subDivisionsList = json.getJSONObject("request")
	 * .getJSONObject("subdivisions") .getJSONArray("subdivision");
	 * System.out.println("SubDivision array json:" +
	 * subDivisionsList.toString(3)); for (int i = 0; i <
	 * subDivisionsList.size(); i++) { String subDiv =
	 * subDivisionsList.getString(i); System.out.println("SubDivisions:" +
	 * subDiv); if (validSubDivList.contains(subDiv) == true) {
	 * System.out.println("Subdiv valid-- add");
	 * selectedSubDivisions.add(subDiv); } else {
	 * System.out.println("Sub div invalid--don't add"); }
	 * 
	 * } } else { subDivision = json.getJSONObject("request")
	 * .getJSONObject("subdivisions")
	 * .getJSONObject("subdivision").getString("element");
	 * System.out.println("SubDivision Obj json:" + subDivisionObject); if
	 * (subDivision.equalsIgnoreCase("all") == true) {
	 * System.out.println("All subdivisions selected"); Collection<String>
	 * allSubDivisionsList = apt .getAllSubDivisions(); if (allSubDivisionsList
	 * != null) { selectedSubDivisions.addAll(allSubDivisionsList); } } else {
	 * System.out.println("Only one subdivision selected"); if
	 * (validSubDivList.contains(subDivision) == true) {
	 * System.out.println("Subdiv valid-- add");
	 * selectedSubDivisions.add(subDivision); } else {
	 * System.out.println("Sub div invalid--don't add"); } } } if
	 * (selectedSubDivisions == null || selectedSubDivisions.isEmpty() == true
	 * || selectedSubDivisions.size() == 0) {
	 * System.out.println("Sub division list is empty"); selectedSubDivisions =
	 * null; } System.out.println("=======SubDivisions ends=========="); } }
	 * String userXMLGroups = DatabaseXmlHelper.createRawXMLUserGroupString(
	 * selectedGroups, null, selectedRoles, selectedDivisions,
	 * selectedSubDivisions, allUsersFlag, false, filterFlag); return
	 * userXMLGroups; }
	 */

	public void sendRemainder(List<UserFlatMapping> selectedUserMembers,
			UserFlatMapping member, String includeContacts, String smsMessage,
			String emailMessageTitle, String emailMessage) {
		System.out.println("Include Contacts:" + includeContacts);
		EmailAndSmsManager ems = new EmailAndSmsManager();
		if (selectedUserMembers != null
				&& selectedUserMembers.isEmpty() == false) {
			for (UserFlatMapping toAddress : selectedUserMembers) {
				if (toAddress.getFlat().isContact() == false
						&& includeContacts.equalsIgnoreCase("false") == true) {
					System.out.println("No contacts included.user name:"
							+ toAddress.getFlat().getRegisteredPerson()
									.getName());
					ems.sendEmailAndSms(emailMessageTitle, emailMessage,
							smsMessage, member.getFlat().getRegisteredPerson()
									.getName(), member.getUser().getEmail(),
							member.getFlat().getRegisteredPerson().getMobile(),
							member, toAddress);
				} else if (includeContacts.equalsIgnoreCase("true") == true) {
					System.out.println("Included contacts also.Contact name:"
							+ toAddress.getFlat().getRegisteredPerson()
									.getName());
					ems.sendEmailAndSms(emailMessageTitle, emailMessage,
							smsMessage, member.getFlat().getRegisteredPerson()
									.getName(), member.getUser().getEmail(),
							member.getFlat().getRegisteredPerson().getMobile(),
							member, toAddress);
				}
			}
		}
	}

	// Error response
	/*
	 * public String processError(String statusCode, String message) {
	 * StringBuffer errorXMLString = new StringBuffer();
	 * errorXMLString.append("<xml>"); errorXMLString.append("<response>");
	 * errorXMLString.append("<statuscode>"); errorXMLString.append(statusCode);
	 * errorXMLString.append("</statuscode>");
	 * errorXMLString.append("<statusmessage>"); if (message != null) {
	 * errorXMLString.append(message); }
	 * errorXMLString.append("</statusmessage>");
	 * errorXMLString.append("</response>"); errorXMLString.append("</xml>");
	 * return errorXMLString.toString();
	 * 
	 * }
	 * 
	 * public String processSuccessString(String serviceType, String
	 * functionType) { StringBuffer successBuffer = new StringBuffer();
	 * successBuffer.append("<xml>"); successBuffer.append("<response>");
	 * successBuffer.append("<servicetype>"); successBuffer.append(serviceType);
	 * successBuffer.append("</servicetype>");
	 * successBuffer.append("<functiontype>");
	 * successBuffer.append(functionType);
	 * successBuffer.append("</functiontype>");
	 * successBuffer.append("<statuscode>"); successBuffer.append(PropertiesUtil
	 * .getProperty("statuscodesuccessvalue"));
	 * successBuffer.append("</statuscode>");
	 * successBuffer.append("<statusmessage>");
	 * successBuffer.append(PropertiesUtil
	 * .getProperty("statusmessagesuccessvalue"));
	 * successBuffer.append("</statusmessage>");
	 * successBuffer.append("</response>"); successBuffer.append("</xml>");
	 * return successBuffer.toString(); }
	 */
	// publishing IVR record for local
	public String publishXMLForLocalIVRRecord(String serviceType,
			String functionType, UserFlatMapping member, String gpzCode,
			String includeContacts, JSONObject selectionJSON,
			JSONObject dataPartJSON) {
		String publishXMLResponse = "";
		StringBuffer publishXMLBuffer = new StringBuffer();
		try {
			Apartment apt = DBOperations.getApartmentByCode(gpzCode);
			if (apt != null) {
				Announcement annDataPart = setDatapartFromJSON(dataPartJSON);
				if (annDataPart == null) {
					System.out.println("Data XML Response in local shared:"
							+ dataXMLResponse);
					return dataXMLResponse;
				}
				System.out.println("Ann data Part : End date:"
						+ annDataPart.getEndDate() + ",EnddateStr:"
						+ annDataPart.getEndTime() + ",Link:"
						+ annDataPart.getLink());
				String userXMLGroups = userGroupJSONString(apt, selectionJSON);
				System.out.println("User XML groups:" + userXMLGroups);
				Announcement recordedAnnouncement = new Announcement();
				recordedAnnouncement.setApartmentId(apt.getId());
				recordedAnnouncement.setLink(annDataPart.getLink());
				// recordedAnnouncement.setXmlUserGroups(userXMLGroups);
				recordedAnnouncement.setPostedByUserMappingId(member.getId());
				Date postedDate = new Date();
				String postedTimeStr = Utils.padThisTo24HrFormat(postedDate
						.getHours() + ":" + postedDate.getMinutes());
				recordedAnnouncement.setPostedDate(postedDate);
				recordedAnnouncement.setPostedTime(postedTimeStr);
				recordedAnnouncement.setEndDate(annDataPart.getEndDate());
				recordedAnnouncement.setEndTime(annDataPart.getEndTime());
				recordedAnnouncement.setModifiedByUserMappingId(member.getId());
				recordedAnnouncement.setPostedByUserMappingId(member.getId());
				if (member.getRole().isCanApprove() == true) {
					recordedAnnouncement.setApproval(true);
					recordedAnnouncement.setApprovalDate(new Date());
				} else {
					recordedAnnouncement.setApproval(false);
				}
				recordedAnnouncement.setTitle("RecordedAnnouncement-"
						+ Utils.formatDate(new Date()));
				recordedAnnouncement.setMedia(true);
				recordedAnnouncement.save();
				Permissions permissions = new Permissions();
				permissions.setGroupz(apt);
				//permissions.setUserSelectionXML(userXMLGroups);
				permissions.setUserSelectionJSON(userXMLGroups);
				permissions.setModuleReferenceId(recordedAnnouncement.getId());
				permissions.setModuleType(permissions.MODULE_ANNOUCEMENTS);
				permissions.save();

				// recordedAnnouncement.setRootAnnouncement(recordedAnnouncement);
				// recordedAnnouncement.save();
				// newly started for saving ends
				EmailAndSmsManager ems = new EmailAndSmsManager();
				String smsMessage = "Testing recorded sms message";
				String emailMessageTitle = "Testing recorded email title";
				String emailMessage = "Testing recorded email message";

				List<UserFlatMapping> selectedUserMembers = (List<UserFlatMapping>) getUserFlatMappingsFromUserGroupsJSON(
						permissions.getUserSelectionJSON(),
						DBOperations.getUfm(recordedAnnouncement
								.getPostedByUserMappingId()), apt);
				System.out.println("Selected USER MEMBERS:"
						+ selectedUserMembers.size());
				if (selectedUserMembers != null
						&& selectedUserMembers.isEmpty() == false) {
					sendRemainder(selectedUserMembers, member, includeContacts,
							smsMessage, emailMessageTitle, emailMessage);
					ems.sendEmailAndSms(emailMessageTitle, emailMessage,
							smsMessage, member.getFlat().getRegisteredPerson()
									.getName(), member.getUser().getEmail(),
							member.getFlat().getRegisteredPerson().getMobile(),
							member, member);
				}
				publishXMLResponse = processSuccessJSONString(serviceType,
						functionType);
			} else {
				publishXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("invalidgroupzcode_code"),
						PropertiesUtil.getProperty("invalidgroupzcode_message"));
				return publishXMLResponse;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			publishXMLResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}

		return publishXMLResponse;
	}

	public String publishXMLForNetworkIVRRecord(String serviceType,
			String functionType, UserFlatMapping member, Apartment apt,
			String includeContacts, Announcement ann, JSONObject selectionJSON,
			SocietyGroupReference sgr) {
		String publishResponse = "";
		try {

			String userXMLGroups = userGroupJSONString(apt, selectionJSON);
			System.out.println("User XML groups:" + userXMLGroups);
			// newly started for saving starts
			Announcement recordedAnnouncement = new Announcement();
			recordedAnnouncement.setApartmentId(apt.getId());
			recordedAnnouncement.setXmlUserGroups(userXMLGroups);
			recordedAnnouncement.setRootAnnouncement(ann);
			// recordedAnnouncement.save();
			Permissions perm = new Permissions();
			perm.setSocietyGroupReference(sgr);
			perm.setGroupz(apt);
			perm.setModuleReferenceId(ann.getId());
			perm.setModuleType(perm.MODULE_ANNOUCEMENTS);
			//perm.setUserSelectionXML(userXMLGroups);
			perm.setUserSelectionJSON(userXMLGroups);
			perm.save();
			String smsMessage = "Testing recorded sms message";
			String emailMessageTitle = "Testing recorded email title";
			String emailMessage = "Testing recorded email message";
			List<UserFlatMapping> selectedUserMembers = (List<UserFlatMapping>) getUserFlatMappingsFromUserGroupsJSON(
					perm.getUserSelectionJSON(),
					DBOperations.getUfm(ann.getPostedByUserMappingId()), apt);
			System.out.println("Selected USER MEMBERS:"
					+ selectedUserMembers.size());
			sendRemainder(selectedUserMembers, member, includeContacts,
					smsMessage, emailMessageTitle, emailMessage);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			publishResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return publishResponse;
	}

	public Announcement setDatapartFromJSON(JSONObject dataJSONPart) {
		Announcement ann = new Announcement();
		String endDateStr = "";
		System.out.println("DataPart JSON :" + dataJSONPart.toString(3));
		try {
			if (dataJSONPart.containsKey("enddate") == true) {
				System.out.println("Data part 1");
				endDateStr = dataJSONPart.getString("enddate");
				if (restUtils.isEmpty(endDateStr) == false) {
					System.out.println("Data part 2");
					dataXMLResponse = processErrorJSONString(
							PropertiesUtil.getProperty("emptyenddate_code"),
							PropertiesUtil.getProperty("emptyenddate_message"));
					return null;
				}
				Date endDate = formatter.parse(endDateStr);
				System.out.println("Data part 3");
				if (endDate.before(new Date())) {
					System.out.println("Data part 4");
					dataXMLResponse = processErrorJSONString(
							PropertiesUtil.getProperty("enddate_invalid_code"),
							PropertiesUtil
									.getProperty("enddate_invalid_message"));
					return null;
				}
			} else {
				System.out.println("Data part 5");
				dataXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("enddate_tag_code"),
						PropertiesUtil.getProperty("enddate_tag_message"));
				return null;
			}

			if (dataJSONPart.containsKey("urlslist") == true) {
				System.out.println("Data part 6");
				System.out.println("URL LIST Tag present");
				System.out.println("URL TAG:"
						+ dataJSONPart.getString("urlslist"));
				if (dataJSONPart.getString("urlslist") == null
						|| dataJSONPart.getString("urlslist").isEmpty() == true
						|| dataJSONPart.getString("urlslist").equalsIgnoreCase(
								"[]") == true) {
					System.out.println("URL TAG LIST IS EMPTY");
					dataXMLResponse = processErrorJSONString(
							PropertiesUtil.getProperty("urlList_empty_code"),
							PropertiesUtil.getProperty("urlList_empty_message"));
					return null;
				}
			} else {
				System.out.println("Data part 7");
				dataXMLResponse = processErrorJSONString(
						PropertiesUtil.getProperty("urlList_tag_code"),
						PropertiesUtil.getProperty("urlList_tag_message"));
				return null;
			}
			JSONObject langObj = new JSONObject();
			System.out.println("Data part 8:"
					+ dataJSONPart.getString("urlslist"));
			langObj.put("language", dataJSONPart.getString("urlslist"));
			String languageURL = langObj.toString();
			Date endDate = formatter.parse(endDateStr);
			System.out.println("End Date after  parsing:" + endDate);
			String endDateTimeStr = Utils.padThisTo24HrFormat(endDate
					.getHours() + ":" + endDate.getMinutes());
			ann.setLink(languageURL);
			ann.setEndDate(endDate);
			ann.setEndTime(endDateTimeStr);
			return ann;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			dataXMLResponse = processErrorJSONString(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return ann;
	}

	// getting valid usergroups from json
	public String userGroupJSONString(Apartment apt, JSONObject selectionJSON) {
		String allUsers = selectionJSON.getString("allusers");
		String filter = selectionJSON.getString("filter");
		List<Object> selectedGroups = new ArrayList<Object>();
		List<Object> selectedRoles = new ArrayList<Object>();
		List<Object> selectedDivisions = new ArrayList<Object>();
		List<Object> selectedSubDivisions = new ArrayList<Object>();
		List<Object> selectedUsers = new ArrayList<Object>();
		boolean allUsersFlag = false;
		boolean filterFlag = false;
		if (filter.equalsIgnoreCase("true") == true) {
			filterFlag = true;
		} else {
			filterFlag = false;
		}
		System.out.println("========All users Starts=========");
		System.out.println("All users=" + allUsers);
		System.out.println("========All users ends=========");
		if (allUsers.equalsIgnoreCase("true") == true) {
			System.out.println("ALL USERS IS TRUE");
			allUsersFlag = true;
		} else {
			System.out.println("ALL USERS IS FALSE");
			allUsersFlag = false;
			// for users
			if (selectionJSON.containsKey("users") == true) {
				String user = null;
				JSONArray usersList = null;
				Object userObject = selectionJSON.get("users");
				System.out.println("========Users Starts=======");
				System.out.println("User Obj:" + userObject);
				if (userObject instanceof JSONArray) {
					usersList = selectionJSON.getJSONArray("users");
					System.out.println("Users JSON Array:"
							+ usersList.toString(3));
					for (int i = 0; i < usersList.size(); i++) {
						System.out.println("Users:"
								+ usersList.getJSONObject(i)
										.getString("userid"));
						String userId = usersList.getJSONObject(i).getString(
								"userid");
						UserFlatMapping userMember = DBOperations
								.getUserFlatMappingById(userId);
						if (userMember != null) {
							if (userMember.isEnabled() == true) {
								if(userMember.getFlat().getApartment().getId()==apt.getId()){
								selectedUsers.add(Integer.toString(userMember
										.getId()));
								}
							}
						}
					}
				} else {
					System.out.println("User JSON Object:" + userObject);
					user = selectionJSON.getJSONObject("users").getString(
							"userid");
					if (user.equalsIgnoreCase("all") == true) {
						System.out.println("All users selected");
						List<UserFlatMapping> allUsersSelected = UserGroupManager
								.getEnabledUsers(apt);
						if (allUsersSelected != null
								&& allUsersSelected.size() > 0) {
							for (UserFlatMapping allMembers : allUsersSelected) {
								selectedUsers.add(Integer.toString(allMembers
										.getId()));
							}
						}
					} else {
						System.out.println("Only one user selected");
						UserFlatMapping singleMember = DBOperations
								.getUserFlatMappingById(user);
						if (singleMember != null) {
							if (singleMember.isEnabled() == true) {
								if(singleMember.getFlat().getApartment().getId()==apt.getId()){
									selectedUsers.add(Integer.toString(singleMember
											.getId()));
									}
							}
						}
					}
				}
				if (selectedUsers == null || selectedUsers.isEmpty() == true
						|| selectedUsers.size() == 0) {
					System.out.println("User list is empty");
					selectedUsers = null;
				}
				System.out.println("=======Users Ends==========");
			}

			// for roles
			if (selectionJSON.containsKey("roles") == true) {
				String role = null;
				JSONArray rolesList = null;
				Object roleObject = selectionJSON.get("roles");
				System.out.println("========Roles Starts=========");
				System.out.println("Role OBJ:" + roleObject);
				if (roleObject instanceof JSONArray) {
					rolesList = selectionJSON.getJSONArray("roles");
					System.out.println("Role JSON Aray:"
							+ rolesList.toString(3));
					for (int i = 0; i < rolesList.size(); i++) {
						System.out.println("Roles:"
								+ rolesList.getJSONObject(i).getString("role"));
						String roleQry = "RoleName = '"
								+ rolesList.getJSONObject(i).getString("role")
								+ "' and SocietyId=" + apt.getId();
						RoleDefinition multipleRoles = (RoleDefinition) DBOperations
								.getSingleDatabaseObject(RoleDefinition.class,
										roleQry);
						if (multipleRoles != null) {
							selectedRoles.add(Integer.toString(multipleRoles
									.getId()));
						}
					}
				} else {
					System.out.println("Role  JSON:" + roleObject);
					role = selectionJSON.getJSONObject("roles").getString(
							"role");
					if (role.equalsIgnoreCase("all") == true) {
						System.out.println("All roles selected");
						List<RoleDefinition> allRolesList = DBOperations
								.getRolesForSelectedUserSociety(apt);
						if (allRolesList != null) {
							for (RoleDefinition allRoles : allRolesList) {
								selectedRoles.add(Integer.toString(allRoles
										.getId()));
							}
						}
					} else {
						System.out.println("Only one role selected");
						String roleQry = "RoleName = '" + role
								+ "' and SocietyId=" + apt.getId();
						RoleDefinition singeRole = (RoleDefinition) DBOperations
								.getSingleDatabaseObject(RoleDefinition.class,
										roleQry);
						if (singeRole != null) {
							selectedRoles.add(Integer.toString(singeRole
									.getId()));
						}
					}
				}

				if (selectedRoles == null || selectedRoles.isEmpty() == true
						|| selectedRoles.size() == 0) {
					System.out.println("Role list is empty");
					selectedRoles = null;
				}
				System.out.println("=======Roles Ends==========");
			}
			// for divisions
			if (selectionJSON.containsKey("divisions") == true) {
				String division = null;
				Object divisionObject = selectionJSON.get("divisions");
				JSONArray divisionsList = null;
				List<String> validDivList = (List<String>) apt
						.getAllDistinctBlocks();
				if (validDivList == null || validDivList.size() == 0) {
					validDivList = new ArrayList<String>();
				}
				System.out.println("======Divisions starts==========");
				if (divisionObject instanceof JSONArray) {
					divisionsList = selectionJSON.getJSONArray("divisions");
					System.out.println("Division array json:"
							+ divisionsList.toString(3));
					for (int i = 0; i < divisionsList.size(); i++) {
						String div = divisionsList.getJSONObject(i).getString(
								"division");
						System.out.println("Divisions:" + div);
						if (validDivList.contains(div) == true) {
							System.out.println("Division Present--add");
							selectedDivisions.add(div);
						} else {
							System.out.println("Division Absent-- Don't add");
						}
					}
				} else {
					division = selectionJSON.getJSONObject("divisions")
							.getString("division");
					System.out.println("Division Obj json:" + divisionObject);
					if (division.equalsIgnoreCase("all") == true) {
						System.out.println("All divisions selected");
						Collection<String> allDivisionsList = apt
								.getDistinctBlocks();
						if (allDivisionsList != null) {
							selectedDivisions.addAll(allDivisionsList);
						}
					} else {
						System.out.println("Only one division selected");
						if (validDivList.contains(division) == true) {
							System.out.println("Division Present--add");
							selectedDivisions.add(division);
						} else {
							System.out.println("Division Absent-- Don't add");
						}
					}
				}

				if (selectedDivisions == null
						|| selectedDivisions.isEmpty() == true
						|| selectedDivisions.size() == 0) {
					System.out.println("Division list is empty");
					selectedDivisions = null;
				}
				System.out.println("=======Divisions ends==========");
			}
			// for groups
			if (selectionJSON.containsKey("groups") == true) {
				String group = null;
				Object groupsObject = selectionJSON.get("groups");
				JSONArray groupsList = null;
				System.out.println("======Groups starts===========");
				if (groupsObject instanceof JSONArray) {
					groupsList = selectionJSON.getJSONArray("groups");
					System.out.println("Groups json array:"
							+ groupsList.toString(3));
					System.out.println("Groups JSON:" + groupsList.toString(3));
					for (int i = 0; i < groupsList.size(); i++) {
						System.out.println("Groups:"
								+ groupsList.getJSONObject(i)
										.getString("group"));
						String grpQry = "GroupName ='"
								+ groupsList.getJSONObject(i)
										.getString("group")
								+ "' and ApartmentId=" + apt.getId();
						Group multipleGroups = (Group) DBOperations
								.getSingleDatabaseObject(Group.class, grpQry);
						if (multipleGroups != null) {
							selectedGroups.add(Integer.toString(multipleGroups
									.getId()));
						}
					}

				} else {
					System.out.println("Group json obj:" + groupsObject);
					group = selectionJSON.getJSONObject("groups").getString(
							"group");
					if (group.equalsIgnoreCase("all") == true) {
						System.out.println("All groups selected");
						List<Group> allGroupsList = DBOperations.getGroups(apt);
						if (allGroupsList != null) {
							for (Group allGroups : allGroupsList) {
								selectedGroups.add(Integer.toString(allGroups
										.getId()));
							}
						}
					} else {
						String grpQry = "GroupName ='" + group
								+ "' and ApartmentId=" + apt.getId();
						Group singleGroup = (Group) DBOperations
								.getSingleDatabaseObject(Group.class, grpQry);
						if (singleGroup != null) {
							selectedGroups.add(Integer.toString(singleGroup
									.getId()));
						}
						System.out.println("Only one group selected");
					}
				}
				if (selectedGroups == null || selectedGroups.isEmpty() == true
						|| selectedGroups.size() == 0) {
					System.out.println("Groups list is empty");
					selectedGroups = null;
				}
				System.out.println("=======Groups ends==========");
			}
			// for subdivisions
			if (selectionJSON.containsKey("subdivisions") == true) {
				String subDivision = null;
				Object subDivisionObject = selectionJSON.get("subdivisions");
				JSONArray subDivisionsList = null;
				System.out.println("======SubDivisions starts==========");
				List<String> validSubDivList = (List<String>) apt
						.getAllSubDivisions();
				if (validSubDivList == null || validSubDivList.size() == 0) {
					validSubDivList = new ArrayList<String>();
				}
				if (subDivisionObject instanceof JSONArray) {
					subDivisionsList = selectionJSON
							.getJSONArray("subdivisions");
					System.out.println("SubDivision array json:"
							+ subDivisionsList.toString(3));
					for (int i = 0; i < subDivisionsList.size(); i++) {
						String subDiv = subDivisionsList.getJSONObject(i)
								.getString("subdivision");
						System.out.println("SubDivisions:" + subDiv);
						if (validSubDivList.contains(subDiv) == true) {
							System.out.println("Subdiv valid-- add");
							selectedSubDivisions.add(subDiv);
						} else {
							System.out.println("Sub div invalid--don't add");
						}

					}
				} else {
					subDivision = selectionJSON.getJSONObject("subdivisions")
							.getString("subdivision");
					System.out.println("SubDivision Obj json:"
							+ subDivisionObject);
					if (subDivision.equalsIgnoreCase("all") == true) {
						System.out.println("All subdivisions selected");
						Collection<String> allSubDivisionsList = apt
								.getAllSubDivisions();
						if (allSubDivisionsList != null) {
							selectedSubDivisions.addAll(allSubDivisionsList);
						}
					} else {
						System.out.println("Only one subdivision selected");
						if (validSubDivList.contains(subDivision) == true) {
							System.out.println("Subdiv valid-- add");
							selectedSubDivisions.add(subDivision);
						} else {
							System.out.println("Sub div invalid--don't add");
						}
					}
				}
				if (selectedSubDivisions == null
						|| selectedSubDivisions.isEmpty() == true
						|| selectedSubDivisions.size() == 0) {
					System.out.println("Sub division list is empty");
					selectedSubDivisions = null;
				}
				System.out.println("=======SubDivisions ends==========");
			}
		}
		String userXMLGroups = createRawJSONUserGroupString(selectedGroups,
				selectedUsers, selectedRoles, selectedDivisions, selectedSubDivisions,
				allUsersFlag, false, filterFlag);
		return userXMLGroups;
	}

	// creating usergroup json

	public static String createRawJSONUserGroupString(List groupids,
			List userids, List roleids, List blocks, List subBlocks,
			boolean allUsers, boolean excludeMe, boolean filter) {
		String jsonString = null;
		JSONObject userGroupJSON = new JSONObject();
		JSONObject contentsJSON = new JSONObject();
		String allUsersFlag = "false";
		String filterFlag = "";
		String excludeFlag = "false";
		if (allUsers == true) {
			allUsersFlag = "true";
		}
		if (excludeMe == true) {
			excludeFlag = "true";
		}
		if (filter == true) {
			filterFlag = "true";
		}
		contentsJSON.put("allusers", allUsersFlag);
		// contentsJSON.put("excludeme", excludeFlag);
		contentsJSON.put("filter", filterFlag);
		System.out.println("Groupids:" + groupids);
		System.out.println("Roleids:" + roleids);
		System.out.println("Userids:" + userids);
		System.out.println("Divids:" + blocks);
		System.out.println("SubBlocks:" + subBlocks);
		if (!allUsers) {
			if (groupids != null) {
				JSONArray groupsArray = new JSONArray();
				for (int grpCount = 0; grpCount < groupids.size(); grpCount++) {
					JSONObject groupsObject = new JSONObject();
					groupsObject
							.put("groupid", (String) groupids.get(grpCount));
					System.out.println("Groupid:"
							+ (String) groupids.get(grpCount));
					groupsArray.add(groupsObject);
				}
				if (groupsArray != null && groupsArray.size() > 0) {
					contentsJSON.put("groupslist", groupsArray);
				}
			}
			if (userids != null) {
				JSONArray usersArray = new JSONArray();
				for (int userCount = 0; userCount < userids.size(); userCount++) {
					JSONObject usersObject = new JSONObject();
					usersObject.put("userid", (String) userids.get(userCount));
					System.out.println("Userid:"
							+ (String) userids.get(userCount));
					usersArray.add(usersObject);
				}
				if (usersArray != null && usersArray.size() > 0) {
					contentsJSON.put("userslist", usersArray);
				}
			}

			if (roleids != null) {
				JSONArray rolesArray = new JSONArray();
				for (int roleCount = 0; roleCount < roleids.size(); roleCount++) {
					JSONObject roleObject = new JSONObject();
					roleObject.put("roleid", (String) roleids.get(roleCount));
					System.out.println("roleid:"
							+ (String) roleids.get(roleCount));
					rolesArray.add(roleObject);
				}
				if (rolesArray != null && rolesArray.size() > 0) {
					contentsJSON.put("roleslist", rolesArray);
				}
			}

			if (blocks != null) {
				JSONArray blockArray = new JSONArray();
				for (int blockCount = 0; blockCount < blocks.size(); blockCount++) {
					JSONObject blockObject = new JSONObject();
					String block = (String) blocks.get(blockCount);
					blockObject.put("block", block);
					System.out.println("block:" + block);
					blockArray.add(blockObject);
				}
				if (blockArray != null && blockArray.size() > 0) {
					contentsJSON.put("blocklist", blockArray);
				}
			}

			if (subBlocks != null) {
				JSONArray subBlockArray = new JSONArray();
				for (int subdivisionCount = 0; subdivisionCount < subBlocks
						.size(); subdivisionCount++) {
					JSONObject subBlockObject = new JSONObject();
					String subdivision = (String) subBlocks
							.get(subdivisionCount);
					subBlockObject.put("subblock", subdivision);
					System.out.println("Subblock:" + subdivision);
					subBlockArray.add(subBlockObject);
				}
				if (subBlockArray != null && subBlockArray.size() > 0) {
					contentsJSON.put("subblocklist", subBlockArray);
				}
			}

		}
		userGroupJSON.put("usergroup", contentsJSON);
		jsonString = userGroupJSON.toString();
		System.out.println("USERGROUPJSON STRING:" + jsonString);
		return jsonString;
	}

	public static Collection<UserFlatMapping> getUserFlatMappingsFromUserGroupsJSON(
			String userGroupsJSON, UserFlatMapping currentUser,
			Apartment apartment) {
		System.out.println("Getting user groups xml:" + userGroupsJSON);
		Collection<UserFlatMapping> consolidatedList = new ArrayList<UserFlatMapping>();
		if (userGroupsJSON != null) {
			List<String> userIdList = new ArrayList<String>();
			List<String> groupIdList = new ArrayList<String>();
			List<String> roleIdList = new ArrayList<String>();
			List<String> blockList = new ArrayList<String>();
			List<String> subBlockList = new ArrayList<String>();

			Hashtable<String, Boolean> flags = new Hashtable<String, Boolean>();
			getuserGroupJSONValues(userGroupsJSON, userIdList, groupIdList,
					roleIdList, blockList, subBlockList, flags);
			boolean allUsers = flags.get("ALLUSERS");
			// boolean excludeMe = false;

			boolean filter = flags.get("FILTER");
			String filterPartQry = null;
			if (allUsers) {
				return UserGroupManager.getEnabledUsers(apartment);
			}
			if (filter == true) {
				filterPartQry = " and ";
			} else {
				filterPartQry = " or ";
			}
			String finalSelectionQry = "";
			String userSelectionQry = "";
			String roleSelectionQry = "";
			String divisionQry = "";
			String subDivQry = "";
			String orQry = " or ";
			String usrMemQry = "";
			String rolQry = "";
			String divQry = "";
			for (Iterator<String> iterSubDiv = subBlockList.iterator(); iterSubDiv
					.hasNext();) {
				String subBlock = iterSubDiv.next();
				if (!Utils.isNullOrEmpty(subBlock)) {
					subDivQry += "flat.subdivision like '" + subBlock + "'"
							+ orQry;
				}
			}
			if (subDivQry.endsWith(orQry) == true) {
				subDivQry = subDivQry.substring(0, subDivQry.length() - 4);
			}
			for (Iterator<String> iter = groupIdList.iterator(); iter.hasNext();) {
				System.out.println("iterating groups part xml");
				String id = iter.next();
				Group group = UserGroupManager.getGroupById(Integer
						.parseInt(id));
				if (group != null) {
					System.out.println("Getting group id:"
							+ group.getGroupName() + "," + group.getId());
					// newly added for user created groups starts on 15-03-2014
					String groupsXMLContents = group.getXmlGroupsContents();
					System.out.println("Getting groupsXMLcontents:"
							+ groupsXMLContents);
					if (groupsXMLContents != null
							&& groupsXMLContents.isEmpty() == false) {
						List<String> groupUserIdList = new ArrayList<String>();
						List<String> groupRoleIdList = new ArrayList<String>();
						List<String> groupBlockList = new ArrayList<String>();
						DatabaseXmlHelper.getUserCreatedGroupList(
								groupsXMLContents, groupUserIdList,
								groupRoleIdList, groupBlockList);
						// groups user starts
						for (Iterator<String> iterUser = groupUserIdList
								.iterator(); iterUser.hasNext();) {
							String usrId = iterUser.next();
							UserFlatMapping mapping = UserGroupManager
									.getUserFlatMappingForId(Integer
											.parseInt(usrId));
							if (mapping != null) {
								/*
								 * if (excludeMe && (mapping.getId() ==
								 * currentUser .getId())) continue;
								 */
								if (!mapping.isMappingOrUserDisabled())
									usrMemQry += " userflatmapping.id="
											+ mapping.getId() + orQry;
							}
						}
						// groups user ends
						// groups role starts
						for (Iterator<String> iterRole = groupRoleIdList
								.iterator(); iterRole.hasNext();) {
							String roleId = iterRole.next();
							RoleDefinition role = DBOperations
									.getRoleForId(Integer.parseInt(roleId));
							if (role != null) {
								rolQry += " roledefinition.id=" + role.getId()
										+ orQry;
							}
						}
						// groups role ends
						// groups block starts
						for (Iterator<String> iterBlock = groupBlockList
								.iterator(); iterBlock.hasNext();) {
							String blockId = iterBlock.next();
							if (!Utils.isNullOrEmpty(blockId)) {
								divQry += " flat.block_streetdetails like '"
										+ blockId + "'";
								if (subDivQry != null
										&& subDivQry.equalsIgnoreCase("") == false) {
									divQry += " and ( " + subDivQry + " )";
								}
								divQry += orQry;
							}
						}
						// groups block ends
					}
					// newly added for user created groups ends on 15-03-2014
				}
			}
			for (Iterator<String> iter = userIdList.iterator(); iter.hasNext();) {
				String id = iter.next();
				UserFlatMapping mapping = UserGroupManager
						.getUserFlatMappingForId(Integer.parseInt(id));
				if (mapping != null) {
					/*
					 * if (excludeMe && (mapping.getId() ==
					 * currentUser.getId())) continue;
					 */
					if (!mapping.isMappingOrUserDisabled())
						usrMemQry += " userflatmapping.id=" + mapping.getId()
								+ orQry;
				}
			}
			if (usrMemQry != null && usrMemQry.equalsIgnoreCase("") == false) {
				if (usrMemQry.endsWith(orQry) == true) {
					usrMemQry = usrMemQry.substring(0, usrMemQry.length() - 4);
					usrMemQry = "(" + usrMemQry + ")";
				}
			}
			// System.out.println("Befor role list Role QRY:" + rolQry);

			for (Iterator<String> iter = roleIdList.iterator(); iter.hasNext();) {
				String id = iter.next();
				RoleDefinition role = DBOperations.getRoleForId(Integer
						.parseInt(id));
				if (role != null) {
					rolQry += " roledefinition.id=" + role.getId() + orQry;
				}
			}
			// System.out.println("after role list Role QRY:" + rolQry);
			if (rolQry != null) {
				if (rolQry.endsWith(orQry) == true) {
					rolQry = rolQry.substring(0, rolQry.length() - 4);
					rolQry = " ( " + rolQry + " ) " + filterPartQry;
				}
			}
			// System.out.println("final role list Role QRY:" + rolQry);
			for (Iterator<String> iter = blockList.iterator(); iter.hasNext();) {
				String block = iter.next();
				if (!Utils.isNullOrEmpty(block)) {
					divQry += " flat.block_streetdetails like '" + block + "'";
					if (subDivQry != null
							&& subDivQry.equalsIgnoreCase("") == false) {
						divQry += " and ( " + subDivQry + " )";
					}
					divQry += orQry;
				}
			}
			if (divQry != null) {
				if (divQry.endsWith(orQry) == true) {
					divQry = divQry.substring(0, divQry.length() - 4);
					divQry = " ( " + divQry + " ) ";
				}
			}
			userSelectionQry = usrMemQry;
			roleSelectionQry = rolQry;
			divisionQry = divQry;

			System.out.println("UserSelectionQRY:" + userSelectionQry);
			System.out.println("RoleSelectionQry:" + roleSelectionQry);
			System.out.println("DivisionSelectionQry:" + divisionQry);
			String mainFinalQry = "";
			if ((userSelectionQry != null && userSelectionQry
					.equalsIgnoreCase("") == false)
					&& roleSelectionQry != null
					&& divisionQry != null) {
				mainFinalQry = usrMemQry + " or " + roleSelectionQry
						+ divisionQry;
				System.out.println("1.Main final QRy:" + mainFinalQry);
			}
			if (userSelectionQry != null
					&& roleSelectionQry.equalsIgnoreCase("") == true
					&& divisionQry.equalsIgnoreCase("") == true) {
				mainFinalQry = usrMemQry + roleSelectionQry + divisionQry;
				System.out.println("2.Main final QRy:" + mainFinalQry);
			}
			if ((userSelectionQry == null || userSelectionQry
					.equalsIgnoreCase("") == true)
					&& roleSelectionQry != null
					&& divisionQry != null) {
				mainFinalQry = usrMemQry + roleSelectionQry + divisionQry;
				System.out.println("3.Main final QRy:" + mainFinalQry);
			}
			String lastPartQry = "";
			if (mainFinalQry == null
					|| mainFinalQry.equalsIgnoreCase("") == true) {
				lastPartQry = "";
			} else {
				lastPartQry = " and (" + mainFinalQry + " )";
			}

			finalSelectionQry = "flat.apartmentId=" + apartment.getId()
					+ lastPartQry;
			System.out.println("FinalSelectionQry:" + finalSelectionQry);
			consolidatedList = DBOperations
					.getSelectedUsersList(finalSelectionQry);
			System.out.println("Consolidated list size:"
					+ consolidatedList.size());

		}
		return consolidatedList;
	}

	public static void getuserGroupJSONValues(String selectionJSONString,
			List<String> userIdList, List<String> groupIdList,
			List<String> roleIdList, List<String> blockList,
			List<String> subBlockList, Hashtable<String, Boolean> flags) {
		if (selectionJSONString == null) {
			// System.out.println("None of the parameters should be empty");
			return;
		}
		System.out.println("Selection JSON11:" + selectionJSONString);
		JSONObject selectionJSON = JSONObject.fromObject(selectionJSONString);
		System.out.println("Selection JSON Final:" + selectionJSON.toString(3));
		String allUsers = selectionJSON.getJSONObject("usergroup").getString(
				"allusers");
		String filter = selectionJSON.getJSONObject("usergroup").getString(
				"filter");
		boolean allUsersFlag = false;
		boolean filterFlag = false;
		if (filter.equalsIgnoreCase("true") == true) {
			filterFlag = true;
		} else {
			filterFlag = false;
		}
		System.out.println("========All users Starts=========");
		System.out.println("All users=" + allUsers);
		System.out.println("========All users ends=========");
		if (allUsers.equalsIgnoreCase("true") == true) {
			System.out.println("ALL USERS IS TRUE");
			allUsersFlag = true;
		} else {
			System.out.println("ALL USERS IS FALSE");
			allUsersFlag = false;
			// for users
			if (selectionJSON.getJSONObject("usergroup").containsKey("users") == true) {
				String user = null;
				JSONArray usersList = null;
				Object userObject = selectionJSON.getJSONObject("usergroup")
						.get("users");
				System.out.println("========Roles Starts=========");
				System.out.println("Role OBJ:" + userObject);
				if (userObject instanceof JSONArray) {
					usersList = selectionJSON.getJSONObject("usergroup")
							.getJSONArray("users");
					System.out.println("Role JSON Aray:"
							+ usersList.toString(3));
					for (int i = 0; i < usersList.size(); i++) {
						System.out.println("Users:"
								+ usersList.getJSONObject(i)
										.getString("userid"));
						/*
						 * String roleQry = "RoleName = '" +
						 * rolesList.getString(i) + "' and SocietyId=" +
						 * apt.getId(); RoleDefinition multipleRoles =
						 * (RoleDefinition) DBOperations
						 * .getSingleDatabaseObject(RoleDefinition.class,
						 * roleQry); if (multipleRoles != null) {
						 * selectedRoles.add(Integer.toString(multipleRoles
						 * .getId())); }
						 */
						userIdList.add(usersList.getJSONObject(i).getString(
								"userid"));
					}
				} else {
					System.out.println("user  JSON:" + userObject);
					user = selectionJSON.getJSONObject("usergroup")
							.getJSONObject("users").getString("userid");
					userIdList.add(user);
				}
				System.out.println("=======users Ends==========");
			}

			// for roles
			if (selectionJSON.getJSONObject("usergroup").containsKey("roles") == true) {
				String role = null;
				JSONArray rolesList = null;
				Object roleObject = selectionJSON.getJSONObject("usergroup")
						.get("roles");
				System.out.println("========Roles Starts=========");
				System.out.println("Role OBJ:" + roleObject);
				if (roleObject instanceof JSONArray) {
					rolesList = selectionJSON.getJSONObject("usergroup")
							.getJSONArray("roles");
					System.out.println("Role JSON Aray:"
							+ rolesList.toString(3));
					for (int i = 0; i < rolesList.size(); i++) {
						System.out.println("Roles:"
								+ rolesList.getJSONObject(i).getString("role"));
						roleIdList.add(rolesList.getJSONObject(i).getString(
								"role"));
					}
				} else {
					System.out.println("Role  JSON:" + roleObject);
					role = selectionJSON.getJSONObject("usergroup")
							.getJSONObject("roles").getString("role");
					roleIdList.add(role);
				}
				System.out.println("=======Roles Ends==========");
			}
			// for divisions
			if (selectionJSON.getJSONObject("usergroup").containsKey(
					"divisions") == true) {
				String division = null;
				Object divisionObject = selectionJSON
						.getJSONObject("usergroup").get("divisions");
				JSONArray divisionsList = null;
				System.out.println("======Divisions starts==========");
				if (divisionObject instanceof JSONArray) {
					divisionsList = selectionJSON.getJSONObject("usergroup")
							.getJSONArray("divisions");
					System.out.println("Division array json:"
							+ divisionsList.toString(3));
					for (int i = 0; i < divisionsList.size(); i++) {
						String div = divisionsList.getJSONObject(i).getString(
								"division");
						blockList.add(div);
					}
				} else {
					division = selectionJSON.getJSONObject("usergroup")
							.getJSONObject("divisions").getString("division");
					System.out.println("Division Obj json:" + divisionObject);
					blockList.add(division);
				}
				System.out.println("=======Divisions ends==========");
			}
			// for groups
			if (selectionJSON.getJSONObject("usergroup").containsKey("groups") == true) {
				String group = null;
				Object groupsObject = selectionJSON.getJSONObject("usergroup")
						.get("groups");
				JSONArray groupsList = null;
				System.out.println("======Groups starts===========");
				if (groupsObject instanceof JSONArray) {
					groupsList = selectionJSON.getJSONObject("usergroup")
							.getJSONArray("groups");
					System.out.println("Groups json array:"
							+ groupsList.toString(3));
					System.out.println("Groups JSON:" + groupsList.toString(3));
					for (int i = 0; i < groupsList.size(); i++) {
						groupIdList.add(groupsList.getJSONObject(i).getString(
								"group"));
					}

				} else {
					System.out.println("Group json obj:" + groupsObject);
					group = selectionJSON.getJSONObject("usergroup")
							.getJSONObject("groups").getString("group");
					groupIdList.add(group);
				}
				System.out.println("=======Groups ends==========");
			}
			// for subdivisions
			if (selectionJSON.getJSONObject("usergroup").containsKey(
					"subdivisions") == true) {
				String subDivision = null;
				Object subDivisionObject = selectionJSON.getJSONObject(
						"usergroup").get("subdivisions");
				JSONArray subDivisionsList = null;
				System.out.println("======SubDivisions starts==========");
				if (subDivisionObject instanceof JSONArray) {
					subDivisionsList = selectionJSON.getJSONObject("usergroup")
							.getJSONArray("subdivisions");
					System.out.println("SubDivision array json:"
							+ subDivisionsList.toString(3));
					for (int i = 0; i < subDivisionsList.size(); i++) {
						String subDiv = subDivisionsList.getJSONObject(i)
								.getString("subdivision");
						subBlockList.add(subDiv);
					}
				} else {
					subDivision = selectionJSON.getJSONObject("usergroup")
							.getJSONObject("subdivisions")
							.getString("subdivision");
					System.out.println("SubDivision Obj json:"
							+ subDivisionObject);
					subBlockList.add(subDivision);
				}
				System.out.println("=======SubDivisions ends==========");
			}
		}
		flags.put("ALLUSERS", allUsersFlag);
		// flags.put("EXCLUDEME", false);
		flags.put("FILTER", filterFlag);
	}

	// process error and sucess json response
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
