package com.user.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Announcement;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Builder;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.modules.usermanagement.UserGroupManager;
import com.apartment.util.Utils;
import com.user.operations.RetrievalDataOperations;
import com.user.utils.IPAddressCheck;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

public class RetrievalDataManager {

	private List<String> ipAddressList;
	private RestUtils restUtils = new RestUtils();

	public String getRetrievalDataResponse(String retrievalDataRequest,
			HttpServletRequest req) {
		String retrievalDataResponse = "";
		String serviceType = "";
		String functionType = "";
		String groupzCode = "";
		String ipAddress = "";
		List<String> ipAddressList;
		IPAddressCheck ipCheck = new IPAddressCheck();

		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject json = new JSONObject();
		try {
			json = (JSONObject) xmlSerializer.read(retrievalDataRequest);
			serviceType = json.getJSONObject("request")
					.getString("servicetype");
			functionType = json.getJSONObject("request").getString(
					"functiontype");
			groupzCode = json.getJSONObject("request").getString("groupzcode");
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				retrievalDataResponse = processError(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return retrievalDataResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				retrievalDataResponse = processError(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return retrievalDataResponse;
			}
			if (restUtils.isEmpty(groupzCode) == false) {
				retrievalDataResponse = processError(
						PropertiesUtil
								.getProperty("invalidgroupzcodeempty_code"),
						PropertiesUtil
								.getProperty("invalidgroupzcodeempty_message"));
				return retrievalDataResponse;
			}
			ipAddress = req.getRemoteAddr();
			System.out.println("Ipaddress:" + ipAddress);
			ipAddressList = new ArrayList<String>();
			Apartment apt = null;
			if (groupzCode != null && groupzCode.trim().isEmpty() == false
					&& groupzCode.equalsIgnoreCase("") == false) {
				apt = DBOperations.getApartmentByCode(groupzCode);
			}
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
							&& functionType.equalsIgnoreCase(PropertiesUtil
									.getProperty("retrievallistforivrrecord")) == true) {
						retrievalDataResponse = getRecordedAnnouncements(
								serviceType, functionType, groupzCode, json);
						return retrievalDataResponse;
					} else {
						retrievalDataResponse = processError(
								PropertiesUtil
										.getProperty("invalidfunctionType_code"),
								PropertiesUtil
										.getProperty("invalidfunctionType_message"));
						return retrievalDataResponse;
					}
				} else {
					retrievalDataResponse = processError(
							PropertiesUtil.getProperty("accessdenied_code"),
							PropertiesUtil.getProperty("accessdenied_message"));
					return retrievalDataResponse;
				}
			} else {
				retrievalDataResponse = processError(
						PropertiesUtil.getProperty("invalidgroupzcode_code"),
						PropertiesUtil.getProperty("invalidgroupzcode_message"));
				return retrievalDataResponse;
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			retrievalDataResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return retrievalDataResponse;
	}

	// getting recorded announcements
	public String getRecordedAnnouncements(String serviceType,
			String functionType, String groupzCode, JSONObject json) {
		String recordedAnnouncementsResponse = "";
		String mobileNumber = "";
		String countryCode = "";
		String memberCode = "";
		String memberId = "";
		String ipAddress ="";
		StringBuffer recordedAnnouncementsXMLBuffer = new StringBuffer();
		try {
			countryCode = json.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			mobileNumber = json.getJSONObject("request")
					.getJSONObject("mobile").getString("mobilenumber");
			memberCode = json.getJSONObject("request").getString("membercode");
			memberId = json.getJSONObject("request").getString("memberid");
			if (restUtils.isEmpty(memberCode) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("emptymemberCode_code"),
						PropertiesUtil.getProperty("emptymemberCode_message"));
				return recordedAnnouncementsResponse;
			}
			if (restUtils.isEmpty(memberId) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("emptymemberId_code"),
						PropertiesUtil.getProperty("emptymemberId_message"));
				return recordedAnnouncementsResponse;
			}
			if (restUtils.isEmpty(countryCode) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return recordedAnnouncementsResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return recordedAnnouncementsResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return recordedAnnouncementsResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				recordedAnnouncementsResponse = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return recordedAnnouncementsResponse;
			}
			
			//cheking ip address tag present or not			
				ipAddress = json.getJSONObject("request").getString("ipaddress");
				if (restUtils.isEmpty(ipAddress) == false) {
					recordedAnnouncementsResponse = processError(
							PropertiesUtil.getProperty("ipaddress_empty_code"),
							PropertiesUtil.getProperty("ipaddress_empty_message"));
					return recordedAnnouncementsResponse;
				}
				System.out.println("IP address in retrieval:"+ipAddress);
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			Apartment apt = DBOperations.getApartmentByCode(groupzCode);
			if (apt != null) {
				System.out.println("MemberId:" + memberId);
				UserFlatMapping member = DBOperations.getUfm(Integer
						.parseInt(memberId));
				if (member != null) {
					System.out.println("GpzCode:"
							+ member.getFlat().getApartment().getSocietyCode()
							+ "--GPZCODE:" + groupzCode);
					System.out.println("Doorno:" + member.getFlat().getDoorNo()
							+ "--Membercode:" + memberCode);
					System.out.println("MOBILE:"
							+ member.getFlat().getRegisteredPerson()
									.getMobile() + "--COMPMOB:"
							+ completeMobile);
					if ((member.getFlat().getApartment().getSocietyCode()
							.equalsIgnoreCase(groupzCode) == false)
							|| (member.getFlat().getRegisteredPerson()
									.getMobile()
									.equalsIgnoreCase(completeMobile) == false)
							|| (member.getFlat().getDoorNo()
									.equalsIgnoreCase(memberCode) == false)) {
						recordedAnnouncementsResponse = processError(
								PropertiesUtil
										.getProperty("invalidmemberId_code"),
								PropertiesUtil
										.getProperty("invalidmemberId_message"));
						return recordedAnnouncementsResponse;
					}

				} else {
					System.out.println("Member id not matched");
					recordedAnnouncementsResponse = processError(
							PropertiesUtil.getProperty("invalidmemberId_code"),
							PropertiesUtil
									.getProperty("invalidmemberId_message"));
					return recordedAnnouncementsResponse;
				}
				// data part starts
				String paginationQuery = "";
				String expirationQry = "";
				if (json.getJSONObject("request").containsKey("data") == true) {
					String fromPt = "";
					String endPt = "";
					String expiredFlag = "";
					JSONObject dataJSON = json.getJSONObject("request").optJSONObject("data");
					if(dataJSON==null){
						System.out.println("Data json in string");
					}else{
						System.out.println("Data json is object");					
					if (json.getJSONObject("request").getJSONObject("data")
							.containsKey("from") == true) {
						fromPt = json.getJSONObject("request")
								.getJSONObject("data").getString("from");
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.containsKey("to") == true) {
						endPt = json.getJSONObject("request")
								.getJSONObject("data").getString("to");
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.containsKey("expired") == true) {
						expiredFlag = json.getJSONObject("request")
								.getJSONObject("data").getString("expired");
					}
					if ((restUtils.isEmpty(fromPt) == true)
							&& (restUtils.isEmpty(endPt) == true)) {
						paginationQuery = " LIMIT " + endPt + " OFFSET "
								+ fromPt;
					}
					if (restUtils.isEmpty(expiredFlag) == true) {
						if (expiredFlag.equalsIgnoreCase("false") == true) {
							expirationQry = " and anmntB.EndDate>='"
									+ Utils.mySqlFormatterDate(new Date())
									+ " 23:59:59 '";
						} else {
							expirationQry = "";
						}
					} else {
						expirationQry = " and anmntB.EndDate>='"
								+ Utils.mySqlFormatterDate(new Date())
								+ " 23:59:59 '";
					}
				}
				}
				StringBuffer dataXML = new StringBuffer();
				/*
				 * String storageURL = "";  Builder builder = (Builder) DBOperations
				 * .getSingleDatabaseObject(Builder.class, "id=" +
				 * member.getFlat().getApartment().getBuilder() .getId()); if
				 * (builder != null) { if (builder.getStorageURL() != null &&
				 * builder.getStorageURL().isEmpty() == false &&
				 * builder.getStorageURL().equalsIgnoreCase("") == false) {
				 * storageURL = builder.getStorageURL(); } else { storageURL =
				 * PropertiesUtil .getProperty("IVR_STORAGE_URL"); } } else {
				 * storageURL = PropertiesUtil.getProperty("IVR_STORAGE_URL"); }
				 * System.out.println("Storage URL:" + storageURL);
				 */
				dataXML.append("<data>");
				List<Integer> recordedAnnouncements = RetrievalDataOperations
						.getRecordedMessages(apt,
								UserGroupManager.constructUserXmlQuery(member),
								paginationQuery, expirationQry);
				if (recordedAnnouncements != null
						&& recordedAnnouncements.isEmpty() == false) {
					dataXML.append("<announcementlist>");

					for (Integer rootId : recordedAnnouncements) {
						dataXML.append("<announcement>");
						Announcement an = (Announcement) DBOperations
								.getSingleDatabaseObject(Announcement.class,
										"id=" + rootId);
						/*
						 * dataXML.append("<titleId>");
						 * dataXML.append(an.getTitle()+","+an.getId());
						 * dataXML.append("</titleId>");
						 */
						dataXML.append("<urllist>");
						String link = an.getLink();
						JSONObject mediaLink = JSONObject.fromObject(link);
						// System.out.println("Media Link:"+mediaLink.toString(3));
						// for english
						if (mediaLink.getJSONObject("language").containsKey(
								"english") == true) {							
							String englishId = mediaLink.getJSONObject(
									"language").getString("english");
							if (englishId.contains("id=")) {							
								englishId = englishId.substring(3);								
							}
							String encodedEnglishId = formEncodedRecordedId(ipAddress, englishId);							
							dataXML.append("<english>" + encodedEnglishId
									+ "</english>");
						}
						// for tamil
						if (mediaLink.getJSONObject("language").containsKey(
								"tamil") == true) {							
							String tamilId = mediaLink
									.getJSONObject("language").getString(
											"tamil");
							if (tamilId.contains("id=")) {								
								tamilId = tamilId.substring(3);								
							}
							String encodedTamilId = formEncodedRecordedId(ipAddress, tamilId);							
							dataXML.append("<tamil>" + encodedTamilId + "</tamil>");
						}
						// for kannada
						if (mediaLink.getJSONObject("language").containsKey(
								"kannada") == true) {							
							String kannadaId = mediaLink.getJSONObject(
									"language").getString("kannada");
							if (kannadaId.contains("id=")) {								
								kannadaId = kannadaId.substring(3);								
							}
							String encodedKannadalId = formEncodedRecordedId(ipAddress, kannadaId);						
							dataXML.append("<kannada>" + encodedKannadalId
									+ "</kannada>");
						}
						// for malayalam
						if (mediaLink.getJSONObject("language").containsKey(
								"malayalam") == true) {							
							String malayalamId = mediaLink.getJSONObject(
									"language").getString("malayalam");
							if (malayalamId.contains("id=")) {							
								malayalamId = malayalamId.substring(3);								
							}
							String encodedMalayalamlId = formEncodedRecordedId(ipAddress, malayalamId);							
							dataXML.append("<malayalam>" + encodedMalayalamlId
									+ "</malayalam>");
						}
						// for telugu
						if (mediaLink.getJSONObject("language").containsKey(
								"telugu") == true) {							
							String teluguId = mediaLink.getJSONObject(
									"language").getString("telugu");
							if (teluguId.contains("id=")) {								
								teluguId = teluguId.substring(3);								
							}
							String encodedTeluguId = formEncodedRecordedId(ipAddress, teluguId);							
							dataXML.append("<telugu>" + encodedTeluguId + "</telugu>");
						}
						// for hindi
						if (mediaLink.getJSONObject("language").containsKey(
								"hindi") == true) {
							String hindiId = mediaLink
									.getJSONObject("language").getString(
											"hindi");
							if (hindiId.contains("id=")) {								
								hindiId = hindiId.substring(3);								
							}
							String encodedHindiId = formEncodedRecordedId(ipAddress, hindiId);							
							dataXML.append("<hindi>" + encodedHindiId + "</hindi>");
						}
						dataXML.append("</urllist>");
						dataXML.append("</announcement>");
					}
					dataXML.append("</announcementlist>");
				}
				dataXML.append("</data>");
				recordedAnnouncementsXMLBuffer.append("<xml>");
				recordedAnnouncementsXMLBuffer.append("<response>");
				recordedAnnouncementsXMLBuffer.append("<servicetype>");
				recordedAnnouncementsXMLBuffer.append(serviceType);
				recordedAnnouncementsXMLBuffer.append("</servicetype>");
				recordedAnnouncementsXMLBuffer.append("<functiontype>");
				recordedAnnouncementsXMLBuffer.append(functionType);
				recordedAnnouncementsXMLBuffer.append("</functiontype>");
				recordedAnnouncementsXMLBuffer.append("<statuscode>");
				recordedAnnouncementsXMLBuffer.append(PropertiesUtil
						.getProperty("statuscodesuccessvalue"));
				recordedAnnouncementsXMLBuffer.append("</statuscode>");
				recordedAnnouncementsXMLBuffer.append("<statusmessage>");
				recordedAnnouncementsXMLBuffer.append(PropertiesUtil
						.getProperty("statusmessagesuccessvalue"));
				recordedAnnouncementsXMLBuffer.append("</statusmessage>");
				recordedAnnouncementsXMLBuffer.append(dataXML);
				recordedAnnouncementsXMLBuffer.append("</response>");
				recordedAnnouncementsXMLBuffer.append("</xml>");
				recordedAnnouncementsResponse = recordedAnnouncementsXMLBuffer
						.toString();
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			recordedAnnouncementsResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return recordedAnnouncementsResponse;
	}

	// Error response
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
	
	// forming encoded record id
	public String formEncodedRecordedId(String ipAddress,String recordedId){		
		String recordedMessageId = recordedId;
		return recordedMessageId;
	}

}
