package com.ServiceRequest.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.operations.IssueDBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Issue;
import com.apartment.database.tables.IssueAssignment;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.defs.IssueSettings;
import com.apartment.logging.Logger;
import com.apartment.modules.apartmentmanagement.ApartmentManager;
import com.apartment.modules.issuemanagement.IssueManagementManager;
import com.apartment.modules.usermanagement.UserGroupManager;
import com.apartment.util.Utils;
import com.apartment.webservices.server.GroupzException;
import com.apartment.webservices.server.SocietyHelper;

import com.user.operations.SROperations;
import com.user.utils.IPAddressCheck;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

public class ServiceRequestManager {

	private String srResponseList = "";
	private String serviceType = "";
	private String functionType = "";
	private RestUtils restUtils = new RestUtils();
	//private String groupzId = "";
	private String ipAddress = "";
	private IPAddressCheck ipCheck = new IPAddressCheck();
	private List<String> ipAddressList;

	public String processSR(String srRequestList, HttpServletRequest request) {

		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject json = new JSONObject();

		try {
			json = (JSONObject) xmlSerializer.read(srRequestList);

			serviceType = json.getJSONObject("request")
					.getString("servicetype");
			functionType = json.getJSONObject("request").getString(
					"functiontype");
			System.out.println("\n TESTING ");
			
			System.out.println("\n ServiceType :-> " + serviceType+ " FunctionType:-> " + functionType);

			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				srResponseList = processError(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return srResponseList;
			}
			if (restUtils.isEmpty(functionType) == false) {
				srResponseList = processError(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return srResponseList;
			}

			// For groupzSR-Categories
			if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("listofgroupzSRCategories")))) {
				System.out.println(" Inside GroupzSR Categories ");
				srResponseList = getGroupzSRCategories(json, request);
			}

			// For listofGroupZByTechnicianMobile
			else if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("listofGroupZByTechnicianMobile")))) {
				System.out.println(" Inside listofGroupZByTechnicianMobile  ");
				srResponseList = getlistofGroupZByTechnicianMobile(json,
						request);
			}

			// For placegroupzIssuewithsourcetype
			else if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("placegroupzIssuewithsourcetype")))) {
				System.out.println(" Inside placegroupzIssuewithsourcetype  ");
				srResponseList = placegroupzIssuewithsourcetype(json, request);
			}
			
			
			// For completeGroupSR
			else if ((serviceType.equalsIgnoreCase(PropertiesUtil
					.getProperty("ivrselection")))
					&& (functionType.equalsIgnoreCase(PropertiesUtil
							.getProperty("completeGroupSR")))) {
				System.out.println(" Inside completeGroupSR  ");
				srResponseList = completeGroupSR(json, request);
			}

			else {
				srResponseList = processError(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return srResponseList;
			}

		} catch (Exception e) {
			e.printStackTrace();
			srResponseList = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return srResponseList;
	}

	
	// Get Groupz SR Categories
	private String getGroupzSRCategories(JSONObject json,
			HttpServletRequest request) throws GroupzException {
		String groupzSRCategories = "";
		System.out.println("Inside GroupzSR Categories function ");

		String accountId = "";
		
		String moduleId = "";
		
		try {

			ipAddress = request.getRemoteAddr();
			System.out.println("Ipaddress  for GroupzSR Categories :"
					+ ipAddress);
			ipAddressList = new ArrayList<String>();
			Apartment apt = null;
			moduleId = json.getJSONObject("request").getString("moduleid");
			accountId = json.getJSONObject("request").getString("groupzid");

			
			if (restUtils.isEmpty(moduleId) == false) {
				groupzSRCategories = processError(
						PropertiesUtil.getProperty("moduleempty_code"),
						PropertiesUtil.getProperty("moduleempty_message"));
				return groupzSRCategories;
			}
			
			
			if (restUtils.isEmpty(accountId) == false) {
				groupzSRCategories = processError(PropertiesUtil
						.getProperty("invalidgroupzidempty_code"),
						PropertiesUtil
								.getProperty("invalidgroupzideempty_message"));
				return groupzSRCategories;
			}
			
			
			
			if (accountId != null && accountId.trim().isEmpty() == false
					&& accountId.equalsIgnoreCase("") == false) {
				apt = DBOperations.getApartmentById(accountId);

			}

			// Check valid moduleID
						if (moduleId.equalsIgnoreCase(PropertiesUtil
								.getProperty("issues_enabled"))) {
			if (apt != null) {
				String stList = (String) DBOperations.getIpAddresses(
						apt.getId()).get(0);
				if (stList != null) {
					StringTokenizer st = new StringTokenizer(stList, "\n");
					while (st.hasMoreTokens()) {
						ipAddressList.add(st.nextToken());
					}
				}
				int apartmentId = SocietyHelper.asGroupzIdInt(accountId);

				IssueSettings issueSettings = ApartmentManager
						.getApartmentSettings(apartmentId).getIssueSettings();
				StringBuffer categoryBuffer = new StringBuffer();

				if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
					if (issueSettings != null) {
						List<String> catList = issueSettings.getCategories();
						for (String srcat : catList) {
							String srlocal = "";
							srlocal = "<element><category>" + srcat
									+ "</category></element>";
							categoryBuffer.append(srlocal);
						}

					} else {
						System.out.println("\n Issue settings is NULL ");
					}
				} else {
					groupzSRCategories = processError(
							PropertiesUtil.getProperty("accessdenied_code"),
							PropertiesUtil.getProperty("accessdenied_message"));
					return groupzSRCategories;
					}

					String finalResp = "<servicerequestcategories>"
							+ categoryBuffer.toString()
							+ "</servicerequestcategories>";

					groupzSRCategories = processSucess(serviceType,
							functionType, accountId, finalResp.toString());
				} else {

					groupzSRCategories = processError(
							PropertiesUtil.getProperty("invalidgroupzid_code"),
							PropertiesUtil
									.getProperty("invalidgroupzid_message"));
					return groupzSRCategories;
				}
			} else {
				groupzSRCategories = processError(
						PropertiesUtil.getProperty("invalidmodule_code"),
						PropertiesUtil.getProperty("invalidmodule_message"));
				return groupzSRCategories;
			}
		

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("\n Error in groupzCategories :-->> "
					+ e.getMessage());
			groupzSRCategories = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}

		return groupzSRCategories;
	}

	// Get listofGroupZByTechnicianMobile
	private String getlistofGroupZByTechnicianMobile(JSONObject json,
			HttpServletRequest request) {
		String listofGroupZByTechnicianMobile = "";
		String technicianMobileNo = "";
		String countryCode = "";
		String moduleId = "";
		try {
			moduleId = json.getJSONObject("request").getString("moduleid");
			countryCode = json.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			technicianMobileNo = json.getJSONObject("request")
					.getJSONObject("mobile").getString("mobilenumber");

			if (restUtils.isEmpty(moduleId) == false) {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("moduleempty_code"),
						PropertiesUtil.getProperty("moduleempty_message"));
				return listofGroupZByTechnicianMobile;
			}
			
			if (Utils.isNumber(countryCode) == false) {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return listofGroupZByTechnicianMobile;
			}
			
			if (Utils.isNumber(technicianMobileNo) == false) {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return listofGroupZByTechnicianMobile;
			}

			if (restUtils.isEmpty(technicianMobileNo) == false) {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return listofGroupZByTechnicianMobile;
			}

			String completeMobileNo = "+" + countryCode + "."
					+ technicianMobileNo;
			System.out.println(" Final Mobile number to pass :-->> "
					+ completeMobileNo);

			List<Integer> groupzList = UserGroupManager
					.findPeopleByTechnician(completeMobileNo);

			ipAddress = request.getRemoteAddr();
			System.out.println(" Ipaddress in Technician proces :->> "
					+ ipAddress);
			ipAddressList = new ArrayList<String>();

			StringBuffer grpzList = new StringBuffer();
			int countIPAddress = 0;
			
			// Check valid moduleID
			if (moduleId.equalsIgnoreCase(PropertiesUtil
					.getProperty("issues_enabled"))) {

			if (groupzList != null && groupzList.isEmpty() == false) {

				System.out.println("\n grpzList   :-->> " + groupzList);

				for (Integer apt : groupzList) {
					System.out.println("\n Check ip address for groupz :--> "
							+ apt);

					String stList = (String) DBOperations.getIpAddresses(apt)
							.get(0);
					if (stList != null) {
						StringTokenizer st = new StringTokenizer(stList, "\n");
						while (st.hasMoreTokens()) {
							ipAddressList.add(st.nextToken());
						}
					}
					if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
						String st = "";
						st = "<element><groupzid>" + apt
								+ "</groupzid></element>";
						System.out.println(" \n Appending value :-->> " + st);
						grpzList.append(st);

					} else {
						countIPAddress++;
						if (countIPAddress == groupzList.size()) {
							listofGroupZByTechnicianMobile = processError(
									PropertiesUtil
											.getProperty("accessdenied_code"),
									PropertiesUtil
											.getProperty("accessdenied_message"));
							return listofGroupZByTechnicianMobile;
						}

					}
					stList = "";
					ipAddressList = new ArrayList<String>();
				}
			} else {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("technicianempty_code"),
						PropertiesUtil.getProperty("technicianempty_message"));
				System.out
						.println(" error for technician mobile not presnt :- "
								+ listofGroupZByTechnicianMobile);

				return listofGroupZByTechnicianMobile;
			}
			}else {
				listofGroupZByTechnicianMobile = processError(
						PropertiesUtil.getProperty("invalidmodule_code"),
						PropertiesUtil.getProperty("invalidmodule_message"));
				return listofGroupZByTechnicianMobile;
			}

			String finalResp = "<groupzlist>" + grpzList.toString()
					+ "</groupzlist>";

			listofGroupZByTechnicianMobile = processSucess(finalResp.toString());

		} catch (Exception e) {
			e.printStackTrace();
			System.out
					.println("\n Error in listofGroupZByTechnicianMobile :-->> "
							+ e.getMessage());
			listofGroupZByTechnicianMobile = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return listofGroupZByTechnicianMobile;
	}

	// placegroupzIssuewithsourcetype
	private String placegroupzIssuewithsourcetype(JSONObject json,
			HttpServletRequest request) throws GroupzException {

		String placegroupzIssuewithsourcetype = "";
		System.out.println("Inside placegroupzIssuewithsourcetype function ");

		String moduleId = "";
		String accountId = "";
		String memberId = "";
		String category = "";
		String title = "";
		String description = "";
		String preferredTime = "";
		String srSourceType = "";
		String srType = "";
		String srScopeType = "";

		Issue sr = new Issue();

		moduleId = json.getJSONObject("request").getString("moduleid");
		accountId = json.getJSONObject("request").getString("groupzid");
		memberId = json.getJSONObject("request").getString("memberid");
		category = json.getJSONObject("request").getString("category");
		title = json.getJSONObject("request").getString("title");
		description = json.getJSONObject("request").getString("description");
		preferredTime = json.getJSONObject("request")
				.getString("preferredTime");
		srSourceType = json.getJSONObject("request").getString("sourceType");
		srType = json.getJSONObject("request").getString("issuetype");
		srScopeType = json.getJSONObject("request").getString("scopetype");

		System.out.println(" \n  moduleid :- " + moduleId + "  Accountid :- "
				+ accountId + " MemberId :- " + memberId + " Category :- "
				+ category + " Title :- " + title + " Description :- "
				+ description + " PreferredTime :- " + preferredTime
				+ " Sourcetype :- " + srSourceType + " SRtype :- " + srType
				+ " Scopetype :- " + srScopeType);

		// For module-id
		if (restUtils.isEmpty(moduleId) == false) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("moduleempty_code"),
					PropertiesUtil.getProperty("moduleempty_message"));
			return placegroupzIssuewithsourcetype;
		}

		// Groupz null check
		if (restUtils.isEmpty(accountId) == false) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("invalidgroupzidempty_code"),
					PropertiesUtil.getProperty("invalidgroupzideempty_message"));
			return placegroupzIssuewithsourcetype;
		}

		// member null check
		if (restUtils.isEmpty(memberId) == false) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("emptymemberId_code"),
					PropertiesUtil.getProperty("emptymemberId_message"));
			return placegroupzIssuewithsourcetype;
		}

		// For titile
		if (restUtils.isEmpty(title) == false) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("title_code"),
					PropertiesUtil.getProperty("title_message"));
			return placegroupzIssuewithsourcetype;
		}

		// For srType
		if (restUtils.isEmpty(srType) == false) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("sr_type_emptycode"),
					PropertiesUtil.getProperty("sr_type__message"));
			return placegroupzIssuewithsourcetype;
		}

		//For Valid-Issuetype
		if ((srType.equalsIgnoreCase(Issue.ISSUE_TYPE_USERSPECIFIC_STR)
				|| srType.equalsIgnoreCase(Issue.ISSUE_TYPE_COMMON_STR)
				|| srType.equalsIgnoreCase(Issue.ISSUE_TYPE_MANAGEMENT_STR))==false) {

			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("sr_type_validcode"),
					PropertiesUtil.getProperty("sr_type_validmessage"));
			return placegroupzIssuewithsourcetype;
		}
		
		
		List<UserFlatMapping> ufmList = new ArrayList<UserFlatMapping>() ;
		
		ufmList = SROperations.getValiduserForGroupz(accountId,memberId) ;
		
		try{
			
		
		if(ufmList!=null && ufmList.isEmpty()==false && ufmList.size()!=0){
			int grpzId = Integer.parseInt(accountId) ;
			int membrId = Integer.parseInt(memberId) ;

			for (UserFlatMapping UFM : ufmList) {

				if (UFM.getFlat().getApartment().getId() == grpzId
						&& UFM.getId() == membrId) {

					Apartment apartment = SocietyHelper.asApartment(accountId);

					UserFlatMapping user = SocietyHelper
							.asUserFlatMapping(memberId);

					// Check valid moduleID
					if (moduleId.equalsIgnoreCase(PropertiesUtil
				.getProperty("issues_enabled"))) {

			if (apartment.getProfile().isIssuesEnabled() == true) {
				if (user == null) {
					throw new GroupzException(
							PropertiesUtil
									.getProperty("invalidmemberId_message"));
				}
				UserFlatMapping forUser = user;

				sr.setFiledByUser(user);
				sr.setPriority(Issue.PRIORITY_MEDIUM);
				sr.setState(Issue.STATE_NEW);
				sr.setUser(forUser.getUser());
				sr.setUserFlatMappingId(forUser);
				sr.setFlat(forUser.getFlat());
				sr.setApartment(apartment);

				sr.setSourceType(srSourceType);
				
				// Personal
				if (srType.equalsIgnoreCase(Issue.ISSUE_TYPE_USERSPECIFIC_STR)) {
					sr.setIssueType(Issue.ISSUE_TYPE_USERSPECIFIC);
				}
				// Common
				if (srType.equalsIgnoreCase(Issue.ISSUE_TYPE_COMMON_STR)) {
					sr.setIssueType(Issue.ISSUE_TYPE_COMMON);
				}
				// Management
				if (srType.equalsIgnoreCase(Issue.ISSUE_TYPE_MANAGEMENT_STR)) {
					sr.setIssueType(Issue.ISSUE_TYPE_MANAGEMENT);
				}
			
				sr.setServiceRequestscope(Issue.ISSUE_SCOPE_COMPLAINT);
				
				if (restUtils.isEmpty(category) == false) {
					sr.setCategory("Others");
				} else {
					sr.setCategory(category);
				}

				sr.setPreferredTime(preferredTime);
				sr.setDescription(description);
				sr.setTitle(title);
				IssueManagementManager.addIssue(sr, user);
				
				String finalResp = "<srgroupzno>" + sr.getIssueId()
						+ "</srgroupzno>";
				
				placegroupzIssuewithsourcetype = processSucessSR(finalResp.toString());
			} else {
				placegroupzIssuewithsourcetype = processError(
						PropertiesUtil.getProperty("sr_code"),
						PropertiesUtil.getProperty("sr_message"));
				return placegroupzIssuewithsourcetype;
			}

						// String finalResp = "<srgroupzno>" + sr.getIssueId() +
						// "</srgroupzno>";

						// placegroupzIssuewithsourcetype =
						// processSucess(finalResp.toString());

					} else {
						placegroupzIssuewithsourcetype = processError(
								PropertiesUtil
										.getProperty("invalidmodule_code"),
								PropertiesUtil
										.getProperty("invalidmodule_message"));
						return placegroupzIssuewithsourcetype;
					}
				} else {
					placegroupzIssuewithsourcetype = processError(
							PropertiesUtil
									.getProperty("sr_groupz_userinvalid_code"),
							PropertiesUtil
									.getProperty("sr_groupz_userinvalid_message"));
					return placegroupzIssuewithsourcetype;
				}

			}
		} else {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("sr_groupz_userinvalid_code"),
					PropertiesUtil.getProperty("sr_groupz_userinvalid_message"));
			return placegroupzIssuewithsourcetype;
		}
		}catch (Exception e) {
			placegroupzIssuewithsourcetype = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}

		return placegroupzIssuewithsourcetype;
	}
	
	// for completeGroupSR
	private String completeGroupSR(JSONObject json, HttpServletRequest request) throws GroupzException {
		String completeGroupzSR = "";

		String moduleId = "";
		String accountId = "";
		//String accountCode = "";
		String countryCode = "";
		String mobile = "";
		String srNo = "";
		String comment = "";

		moduleId = json.getJSONObject("request").getString("moduleid");
		accountId = json.getJSONObject("request").getString("groupzid");
	//	accountCode = json.getJSONObject("request").getString("groupzcode");
		countryCode = json.getJSONObject("request").getJSONObject("mobile")
				.getString("countrycode");
		mobile = json.getJSONObject("request").getJSONObject("mobile").getString("mobilenumber");
		srNo = json.getJSONObject("request").getString("serviceRequestNumber");
		comment = json.getJSONObject("request").getString("comment");

		System.out.println(" \n  moduleid :- " + moduleId + "  Accountid :- "+accountId
				+ " mobile :- "	+ mobile + " countryCode :- " + countryCode
				+ " srNo :- " + srNo 
				+ " comment :- " + comment);
		
		
		// For module-id
		if (restUtils.isEmpty(moduleId) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("moduleempty_code"),
					PropertiesUtil.getProperty("moduleempty_message"));
			return completeGroupzSR;
		}

		// Groupz null check
		if (restUtils.isEmpty(accountId) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("invalidgroupzidempty_code"),
					PropertiesUtil.getProperty("invalidgroupzideempty_message"));
			return completeGroupzSR;
		}

		// Country code
		if (Utils.isNumber(countryCode) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("invalidcountry_code"),
					PropertiesUtil.getProperty("invalidcountry_message"));
			return completeGroupzSR;
		}

		// Country code
				if (Utils.isNumber(mobile) == false) {
					completeGroupzSR = processError(
							PropertiesUtil.getProperty("invalidmobile_code"),
							PropertiesUtil.getProperty("invalidmobile_message"));
					return completeGroupzSR;
				}

		// Mobile number
		if (restUtils.isEmpty(mobile) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("Mobileempty_code"),
					PropertiesUtil.getProperty("Mobileempty_message"));
			return completeGroupzSR;
		}

		// For SR-No
		if (restUtils.isEmpty(srNo) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("srnumberempty_code"),
					PropertiesUtil.getProperty("srnumberempty_message"));
			return completeGroupzSR;
		}

		// Country code
		if (Utils.isNumber(srNo) == false) {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("srnumberinvalid_code"),
					PropertiesUtil.getProperty("srnumberinvalid_message"));
			return completeGroupzSR;
		}
		
		// Check valid moduleID
				if (moduleId.equalsIgnoreCase(PropertiesUtil
						.getProperty("issues_enabled"))) {
		Apartment groupz = SocietyHelper.asApartment(accountId);

		System.out.println("\n Groupz id --- " + groupz.getId());

		Issue targetIssue = (Issue) DBOperations.getSingleDatabaseObject(
				Issue.class, " apartmentid = " + groupz.getId()
						+ " and apartmentissueid = " + srNo);
		String completeMobileNo = "+" + countryCode + "."+ mobile;
		System.out.println(" Final Mobile number to pass complete SR :-->> "	+ completeMobileNo);
		
		if(targetIssue!=null){
			
			String stateString = targetIssue.getStateString();
			if (stateString == null) {
				//return "State String is empty";
				completeGroupzSR = processError(
						PropertiesUtil.getProperty("srstate_invalid_code"),
						PropertiesUtil.getProperty("srstate_invalid_message"));
				return completeGroupzSR;

			}
			Logger.getLogger().log( " Webservices GroupzId  " + groupz.getId() +" SR Number "+ srNo +" SRState String -- "+ stateString ) ;
	    	   
				if (stateString
						.equalsIgnoreCase(Issue.ISSUE_STATE_ASSIGN_CONTRACTOR_STR)) {

					List<IssueAssignment> assignmentList = targetIssue
							.getAssignments();
					if (assignmentList == null) {

						completeGroupzSR = processError(
								PropertiesUtil
										.getProperty("sr_assignmentlistempty_code"),
								PropertiesUtil
										.getProperty("sr_assignmentlistempty_message"));
						return completeGroupzSR;

					} else {
						try {
	    					int count = 0 ;
	    					System.out.println("\n AssignmentListSize  ---- " + assignmentList.size());
	        				
	        					System.out.println("\n 1. Mobile number for Technician from URL - " + completeMobileNo.trim() );
	        				
	        					for( IssueAssignment asngmt : assignmentList) {
	        						System.out.println("\n  Inside Loop condition - " + asngmt.getContractor().getMobileNumber() + " from url " + completeMobileNo.trim() );
	            					
	        						 if( asngmt.getContractor().getMobileNumber().equalsIgnoreCase(completeMobileNo) ) {
	        							 count ++ ;
	        							 System.out.println("\n Success condition  Inside Loop condition - " + asngmt.getContractor().getMobileNumber() + " from url " + completeMobileNo.trim() );
									IssueManagementManager.completeByTechnician(
												targetIssue,comment, completeMobileNo);
								}
						}
							if (count == 0) {
								completeGroupzSR = processError(
										PropertiesUtil
												.getProperty("invalid_technician_code"),
										PropertiesUtil
												.getProperty("invalid_technician_message"));
								return completeGroupzSR;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				} else {

				try {

						System.out.println("\n Mobile number from url ["+ completeMobileNo.trim() + "]");

						if (targetIssue.isIssueCanBeCompleted() == true) {
							List<String> userMobileNolist = new ArrayList<String>();

							userMobileNolist = IssueDBOperations
							.getSRUserMobileNumber(groupz.getId(),
								targetIssue, srNo);
							
							if (userMobileNolist != null) {
							         								
								
								System.out.println("\n UserMobile numberList From DBoperation --- "	+ userMobileNolist.size());
								
									System.out.println("\n 1. Rolename--- " + targetIssue.getUserFlatMapping().getRole().getRoleName() + " rolevalue -- "+targetIssue.getUserFlatMapping().getRole().getRoleValue());

									for (String mobileNumber : userMobileNolist) {
										
										System.out.println("\n Mobile number from url ["+ completeMobileNo.trim() + "] ********** Mobilenumber from db --["+ mobileNumber + "]");
										
										if (completeMobileNo!=null && mobileNumber.equalsIgnoreCase(completeMobileNo)) {
										
											System.out.println("\n Mobile number from url ["+ completeMobileNo.trim() + "] Mobilenumber from db --["+ mobileNumber + "]");
											IssueManagementManager.completeSR(
													targetIssue, comment, targetIssue.getUserFlatMapping(),
													completeMobileNo);
											break;
										}
									}
									
								  }else{
									  completeGroupzSR = processError(
												PropertiesUtil.getProperty("srno_mobileno_code"),
												PropertiesUtil.getProperty("srno_mobileno_message"));
										return completeGroupzSR;  
								  }
																
						} else {
							
							completeGroupzSR = processError(
									PropertiesUtil.getProperty("srstate_invalid_code"),
									PropertiesUtil.getProperty("srstate_invalid_message"));
							return completeGroupzSR;
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} else {

				completeGroupzSR = processError(
						PropertiesUtil.getProperty("sr_empty_code"),
						PropertiesUtil.getProperty("sr_empty_message"));
				return completeGroupzSR;

			}

			String finalResp = "<srgroupzid>" + srNo + "</srgroupzid>";

			completeGroupzSR = processSucess(finalResp.toString());
		} else {
			completeGroupzSR = processError(
					PropertiesUtil.getProperty("invalidmodule_code"),
					PropertiesUtil.getProperty("invalidmodule_message"));
			return completeGroupzSR;
		}

		return completeGroupzSR;
	}
	
	// success Response
	public String processSucess(String sType, String fType, String gpzId,
			String message) {
		StringBuffer sucessXMLString = new StringBuffer();
		sucessXMLString.append("<xml>");
		sucessXMLString.append("<response>");
		sucessXMLString.append("<servicetype>");
		sucessXMLString.append(sType);
		sucessXMLString.append("</servicetype>");
		sucessXMLString.append("<functiontype>");
		sucessXMLString.append(fType);
		sucessXMLString.append("</functiontype>");
		sucessXMLString.append("<statuscode>");
		sucessXMLString.append(PropertiesUtil
				.getProperty("statuscodesuccessvalue"));
		sucessXMLString.append("</statuscode>");
		sucessXMLString.append("<statusmessage>");
		sucessXMLString.append(PropertiesUtil
				.getProperty("statusmessagesuccessvalue"));
		sucessXMLString.append("</statusmessage>");
		if (gpzId != null && gpzId.trim().isEmpty() == false
				&& gpzId.equalsIgnoreCase("") == false) {
			sucessXMLString.append("<groupzid>");
			sucessXMLString.append(gpzId);
			sucessXMLString.append("</groupzid>");
		}
		sucessXMLString.append(message);
		sucessXMLString.append("</response>");
		sucessXMLString.append("</xml>");
		return sucessXMLString.toString();

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

	public String processSucessSR(String message) {
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
				.getProperty("sr_sucess_message"));
		sucessXMLString.append("</statusmessage>");
		sucessXMLString.append(message);
		sucessXMLString.append("</response>");
		sucessXMLString.append("</xml>");
		return sucessXMLString.toString();

	}

	
	
}
