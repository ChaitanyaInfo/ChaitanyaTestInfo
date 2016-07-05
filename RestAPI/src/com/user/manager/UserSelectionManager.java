package com.user.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import javax.management.relation.Role;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Group;
import com.apartment.database.tables.RoleDefinition;
import com.apartment.database.tables.SocietyProfile;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.ui.jsf.beans.UserGroupSelectionBean;
import com.apartment.util.DatabaseXmlHelper;
import com.apartment.util.Utils;
import com.user.operations.GroupzOperations;
import com.user.operations.UserOperations;
import com.user.utils.IPAddressCheck;
import com.user.utils.PropertiesUtil;
import com.user.utils.RestUtils;

public class UserSelectionManager {

	private IPAddressCheck ipCheck = new IPAddressCheck();
	private List<String> ipAddressList;
	private RestUtils restUtils = new RestUtils();
	private String userSelectionResponse = "";
	private String serviceType = "";
	private String functionType = "";
	private String groupzCode = "";
	private String groupzId = "";
	private String ipAddress = "";

	public String getSelectionList(String userSelectionRequest,
			HttpServletRequest request) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject json = new JSONObject();
		try {
			json = (JSONObject) xmlSerializer.read(userSelectionRequest);
			serviceType = json.getJSONObject("request")
					.getString("servicetype");
			functionType = json.getJSONObject("request").getString(
					"functiontype");
			if (json.getJSONObject("request").containsKey("groupzcode") == true) {
				groupzCode = json.getJSONObject("request").getString(
						"groupzcode");
			}

			// newly added for groupz code list
			if (json.getJSONObject("request").containsKey("groupzlist") == true) {
				// JSONArray groupzArray =
				// json.getJSONObject("request").getJSONArray("groupzlist");
				// for(int i=0;i<groupzArray.size();i++){
				groupzCode = (String) json.getJSONObject("request")
						.getJSONObject("groupzlist").get("groupzcode");
				System.out.println("Groupz code:" + groupzCode);
				// }
			}
			if (json.getJSONObject("request").containsKey("groupzid") == true) {
				groupzId = json.getJSONObject("request").getString("groupzid");
			}
			if (restUtils.isEmpty(serviceType) == false
					|| serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == false) {
				userSelectionResponse = processError(
						PropertiesUtil.getProperty("invalidserviceType_code"),
						PropertiesUtil
								.getProperty("invalidserviceType_message"));
				return userSelectionResponse;
			}
			if (restUtils.isEmpty(functionType) == false) {
				userSelectionResponse = processError(
						PropertiesUtil.getProperty("invalidfunctionType_code"),
						PropertiesUtil
								.getProperty("invalidfunctionType_message"));
				return userSelectionResponse;
			}
			if (json.getJSONObject("request").containsKey("groupzcode") == true) {
				if (restUtils.isEmpty(groupzCode) == false) {
					userSelectionResponse = processError(
							PropertiesUtil
									.getProperty("invalidgroupzcodeempty_code"),
							PropertiesUtil
									.getProperty("invalidgroupzcodeempty_message"));
					return userSelectionResponse;
				}
			}
			if (json.getJSONObject("request").containsKey("groupzid") == true) {
				if (restUtils.isEmpty(groupzId) == false) {
					userSelectionResponse = processError(
							PropertiesUtil
									.getProperty("invalidgroupzidempty_code"),
							PropertiesUtil
									.getProperty("invalidgroupzideempty_message"));
					return userSelectionResponse;
				}
			}
			// ipaddress validation
			ipAddress = request.getRemoteAddr();
			System.out.println("Ipaddress:" + ipAddress);
			ipAddressList = new ArrayList<String>();
			Apartment apt = null;
			if (groupzCode != null && groupzCode.trim().isEmpty() == false
					&& groupzCode.equalsIgnoreCase("") == false) {
				apt = DBOperations.getApartmentByCode(groupzCode);
			}
			if (groupzId != null && groupzId.trim().isEmpty() == false
					&& groupzId.equalsIgnoreCase("") == false) {
				apt = DBOperations.getApartmentById(groupzId);
			}
			if (apt != null) {
				System.out.println("Inside APt:" + apt.getName());
				String stList = (String) DBOperations.getIpAddresses(
						apt.getId()).get(0);
				if (stList != null) {
					StringTokenizer st = new StringTokenizer(stList, "\n");
					while (st.hasMoreTokens()) {
						ipAddressList.add(st.nextToken());
					}
				}
				if (ipCheck.checkIPAddressInList(ipAddress, ipAddressList) == true) {
					// members list for groupzid and mobilenumber
					if (serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == true
							&& functionType
									.equalsIgnoreCase(PropertiesUtil
											.getProperty("listofmembersforivrenquirymobileandgroupzid")) == true) {
						userSelectionResponse = getMembersListForGroupzandMobile(request,
								serviceType, functionType, groupzId, json);
						return userSelectionResponse;
					}
					// memebers list for groupzid and landline number
					else if (serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == true
							&& functionType
									.equalsIgnoreCase(PropertiesUtil
											.getProperty("listofmembersforivrenquirylandlineandgroupzid")) == true) {
						userSelectionResponse = getMembersListForGroupzandLandline(request,
								serviceType, functionType, groupzId, json);
						return userSelectionResponse;
					}
					// list of selection
					else if (serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == true
							&& functionType
									.equalsIgnoreCase(PropertiesUtil
											.getProperty("listofselectionforivrrecord")) == true) {
						userSelectionResponse = getSelectionList(serviceType,
								functionType, groupzCode, json);
						return userSelectionResponse;
					}
					// publishing XML for IVR record
					else if (serviceType.equalsIgnoreCase(PropertiesUtil
							.getProperty("ivrselection")) == true
							&& functionType
									.equalsIgnoreCase(PropertiesUtil
											.getProperty("submitselectionforivrrecord")) == true) {
						userSelectionResponse = publishXMLForIVRRecord(
								serviceType, functionType, groupzCode, json);
						return userSelectionResponse;
					} else {
						userSelectionResponse = processError(
								PropertiesUtil
										.getProperty("invalidfunctionType_code"),
								PropertiesUtil
										.getProperty("invalidfunctionType_message"));
						return userSelectionResponse;
					}
				} else {
					userSelectionResponse = processError(
							PropertiesUtil.getProperty("accessdenied_code"),
							PropertiesUtil.getProperty("accessdenied_message"));
					return userSelectionResponse;
				}
			} else {
				if (json.getJSONObject("request").containsKey("groupzid") == true) {
					userSelectionResponse = processError(
							PropertiesUtil.getProperty("invalidgroupzid_code"),
							PropertiesUtil
									.getProperty("invalidgroupzid_message"));
					return userSelectionResponse;
				}
				if (json.getJSONObject("request").containsKey("groupzlist") == true) {
					userSelectionResponse = processError(
							PropertiesUtil
									.getProperty("invalidgroupzcode_code"),
							PropertiesUtil
									.getProperty("invalidgroupzcode_message"));
					return userSelectionResponse;
				}

				if (json.getJSONObject("request").containsKey("groupzcode") == true) {
					userSelectionResponse = processError(
							PropertiesUtil
									.getProperty("invalidgroupzcode_code"),
							PropertiesUtil
									.getProperty("invalidgroupzcode_message"));
					return userSelectionResponse;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			userSelectionResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return userSelectionResponse;
	}

	// getting members list for groupzid and mobile number
	public String getMembersListForGroupzandMobile(HttpServletRequest request,String serviceType,
			String functionType, String gpzId, JSONObject jsonObj) {
		String membersListResponseForGroupzandMobile = "";
		String countryCode = "";
		String mobileNumber = "";
		String contactFlag = "";
		String contactQry = "";
		try {
			countryCode = jsonObj.getJSONObject("request")
					.getJSONObject("mobile").getString("countrycode");
			mobileNumber = jsonObj.getJSONObject("request")
					.getJSONObject("mobile").getString("mobilenumber");
			contactFlag = jsonObj.getJSONObject("request").getString(
					"includecontacts");
			if (restUtils.isEmpty(countryCode) == false) {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return membersListResponseForGroupzandMobile;
			}
			if (Utils.isNumber(countryCode) == false) {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return membersListResponseForGroupzandMobile;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return membersListResponseForGroupzandMobile;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return membersListResponseForGroupzandMobile;
			}
			if (restUtils.isEmpty(contactFlag) == false) {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("contactflagempty_code"),
						PropertiesUtil.getProperty("contactflagempty_message"));
				return membersListResponseForGroupzandMobile;
			}
			if (contactFlag.equalsIgnoreCase("true") == true) {
				contactQry = "";
			} else if (contactFlag.equalsIgnoreCase("false") == true) {
				contactQry = "flat.contact=false and";
			} else {
				membersListResponseForGroupzandMobile = processError(
						PropertiesUtil.getProperty("invalidcontactflag_code"),
						PropertiesUtil
								.getProperty("invalidcontactflag_message"));
				return membersListResponseForGroupzandMobile;
			}
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			String query = contactQry + "  flat.apartmentid=" + gpzId
					+ " and person.mobile='" + completeMobile + "'";
			List<UserFlatMapping> membersList = UserOperations
					.getMembersListForGroupz(query);
			StringBuffer membersBuffer = new StringBuffer();
			if (membersList != null) {
				System.out.println("Members list:" + membersList.size());
				for (UserFlatMapping ufm : membersList) {
					String st = "";
					String memberCodeTag = "";
					String profilePhotoURL="";
					profilePhotoURL = restUtils.createProfilePhotoLogo(request, ufm.getFlat().getRegisteredPerson().getId());
					if (ufm.getFlat().getDoorNo() != null
							&& ufm.getFlat().getDoorNo().trim().isEmpty() == false
							&& ufm.getFlat().getDoorNo().equalsIgnoreCase("") == false) {
						memberCodeTag = "<membercode>"
								+ ufm.getFlat().getDoorNo() + "</membercode>";
					} else {
						memberCodeTag = "<membercode></membercode>";
					}
					st = "<element><memberid>" + ufm.getId() + "</memberid>" +
							"<membername>"+ufm.getFlat().getRegisteredPerson().getName()+"</membername>" +
									"<profileurl>"+Utils.encode(profilePhotoURL)+"</profileurl>"
							+ memberCodeTag + "<division>"
							+ ufm.getFlat().getBlockStreetDetails()
							+ "</division><personid>"+ufm.getFlat().getRegisteredPerson().getId()+"</personid></element>";
					membersBuffer.append(st);

				}
			}
			String finalResp = "<memberslist>" + membersBuffer.toString()
					+ "</memberslist>";
			membersListResponseForGroupzandMobile = processSucess(serviceType,
					functionType, "", finalResp.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			membersListResponseForGroupzandMobile = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return membersListResponseForGroupzandMobile;
	}

	// getting members list for groupzid and landline number
	public String getMembersListForGroupzandLandline(HttpServletRequest request,String serviceType,
			String functionType, String gpzId, JSONObject jsonObj) {
		String membersListResponseForGroupzandLandline = "";
		String countryCode = "";
		String stateCode = "";
		String landlineNumber = "";
		String contactFlag = "";
		String contactQry = "";
		try {
			countryCode = jsonObj.getJSONObject("request")
					.getJSONObject("landline").getString("countrycode");
			stateCode = jsonObj.getJSONObject("request")
					.getJSONObject("landline").getString("statecode");
			landlineNumber = jsonObj.getJSONObject("request")
					.getJSONObject("landline").getString("landlinenumber");
			contactFlag = jsonObj.getJSONObject("request").getString(
					"includecontacts");
			if (restUtils.isEmpty(countryCode) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (Utils.isNumber(countryCode) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (restUtils.isEmpty(stateCode) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("stateempty_code"),
						PropertiesUtil.getProperty("stateempty_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (Utils.isNumber(stateCode) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("invalidstate_code"),
						PropertiesUtil.getProperty("invalidstate_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (restUtils.isEmpty(landlineNumber) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("landlineempty_code"),
						PropertiesUtil.getProperty("landlineempty_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (Utils.isNumber(landlineNumber) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("invalidlandline_code"),
						PropertiesUtil.getProperty("invalidlandline_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (restUtils.isEmpty(contactFlag) == false) {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("contactflagempty_code"),
						PropertiesUtil.getProperty("contactflagempty_message"));
				return membersListResponseForGroupzandLandline;
			}
			if (contactFlag.equalsIgnoreCase("true") == true) {
				contactQry = "";
			} else if (contactFlag.equalsIgnoreCase("false") == true) {
				contactQry = "flat.contact=false and";
			} else {
				membersListResponseForGroupzandLandline = processError(
						PropertiesUtil.getProperty("invalidcontactflag_code"),
						PropertiesUtil
								.getProperty("invalidcontactflag_message"));
				return membersListResponseForGroupzandLandline;
			}
			String completeLandline = "+" + countryCode + "." + stateCode + "."
					+ landlineNumber;
			String query = contactQry + " flat.apartmentid=" + gpzId
					+ " and person.phone='" + completeLandline + "'";
			List<UserFlatMapping> membersList = UserOperations
					.getMembersListForGroupz(query);
			StringBuffer membersBuffer = new StringBuffer();
			if (membersList != null) {
				System.out.println("Members list:" + membersList.size());
				for (UserFlatMapping ufm : membersList) {
					String st = "";
					String memberCodeTag = "";
					String profilePhotoURL = "";										
					profilePhotoURL = restUtils.createProfilePhotoLogo(request, ufm.getFlat().getRegisteredPerson().getId());
					if (ufm.getFlat().getDoorNo() != null
							&& ufm.getFlat().getDoorNo().trim().isEmpty() == false
							&& ufm.getFlat().getDoorNo().equalsIgnoreCase("") == false) {
						memberCodeTag = "<membercode>"
								+ ufm.getFlat().getDoorNo() + "</membercode>";
					} else {
						memberCodeTag = "<membercode></membercode>";
					}
					st = "<element><memberid>" + ufm.getId() + "</memberid>" +
							"<membername>"+ufm.getFlat().getRegisteredPerson().getName()+"</membername>" +
									"<profileurl>"+Utils.encode(profilePhotoURL)+"</profileurl>"
							+ memberCodeTag + "<division>"
							+ ufm.getFlat().getBlockStreetDetails()
							+ "</division><personid>"+ufm.getFlat().getRegisteredPerson().getId()+"</personid></element>";
					membersBuffer.append(st);

				}
			}
			String finalResp = "<memberslist>" + membersBuffer.toString()
					+ "</memberslist>";
			membersListResponseForGroupzandLandline = processSucess(
					serviceType, functionType, "", finalResp.toString());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			membersListResponseForGroupzandLandline = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return membersListResponseForGroupzandLandline;
	}

	// getting selection list
	public String getSelectionList(String serviceType, String functionType,
			String gpzCode, JSONObject json) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		String mobileNumber = "";
		String countryCode = "";
		String selectionListResponse = "";
		StringBuffer selectionListBuffer = new StringBuffer();
		try {
			countryCode = json.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			mobileNumber = json.getJSONObject("request")
					.getJSONObject("mobile").getString("mobilenumber");
			if (restUtils.isEmpty(countryCode) == false) {
				selectionListResponse = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return selectionListResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				selectionListResponse = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return selectionListResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				selectionListResponse = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return selectionListResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				selectionListResponse = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return selectionListResponse;
			}
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			Apartment apt = DBOperations.getApartmentByCode(gpzCode);

			if (apt != null) {
				String query = " flat.apartmentid=" + apt.getId()
						+ " and person.mobile='" + completeMobile
						+ "' and roledefinition.rolevalue&"
						+ RoleDefinition.CAN_RECORD_MESSAGES;
				List<UserFlatMapping> membersList = UserOperations
						.checkMobileNumberValidForGroupz(query);
				if (membersList == null || membersList.isEmpty() == true
						|| membersList.size() == 0) {
					selectionListResponse = processError(
							PropertiesUtil.getProperty("invalidmobile_code"),
							PropertiesUtil.getProperty("invalidmobile_message"));
					return selectionListResponse;
				}
				selectionListBuffer.append("<mobile>");
				selectionListBuffer.append("<countrycode>");
				selectionListBuffer.append(countryCode);
				selectionListBuffer.append("</countrycode>");
				selectionListBuffer.append("<mobilenumber>");
				selectionListBuffer.append(mobileNumber);
				selectionListBuffer.append("</mobilenumber>");
				selectionListBuffer.append("</mobile>");
				selectionListBuffer.append("<selectionlist>");
				selectionListBuffer.append("<selection>");
				selectionListBuffer.append("ALL");
				selectionListBuffer.append("</selection>");
				selectionListBuffer.append("<selection>");
				selectionListBuffer.append("roles");
				selectionListBuffer.append("</selection>");
				selectionListBuffer.append("<selection>");
				selectionListBuffer.append("divisions");
				selectionListBuffer.append("</selection>");
				selectionListBuffer.append("<selection>");
				selectionListBuffer.append("groups");
				selectionListBuffer.append("</selection>");

				// roles part started
				String roleGroupquery = " apartmentid=" + apt.getId()
						+ " and groupvalue=" + Group.ROLE_EXISTS
						+ " and VoiceMailGroup=true";
				List<Group> roleGroupsList = UserOperations
						.getIVRGroupsList(roleGroupquery);
				StringBuffer roleBuffer = new StringBuffer();
				roleBuffer.append("<roles>");
				roleBuffer.append("<role>ALL</role>");
				List<String> roleIdListinGroups = new ArrayList<String>();
				List<String> availableRoleIdsList = new ArrayList<String>();
				JSONArray rolGrpArray = new JSONArray();
				StringBuffer gropRoleBuffer = new StringBuffer();
				if (roleGroupsList != null && roleGroupsList.isEmpty() == false) {
					for (Group roleGrp : roleGroupsList) {
						JSONObject roleGroupObj = (JSONObject) xmlSerializer
								.read(roleGrp.getXmlGroupsContents());
						roleIdListinGroups.addAll(roleGroupObj
								.getJSONArray("rolesList"));
						gropRoleBuffer.append("<" + roleGrp.getGroupName()
								+ "><role>ALL</role>");
						rolGrpArray = roleGroupObj.getJSONArray("rolesList");
						for (int i = 0; i < rolGrpArray.size(); i++) {
							RoleDefinition role = (RoleDefinition) DBOperations
									.getSingleDatabaseObject(
											RoleDefinition.class, "id="
													+ rolGrpArray.getInt(i));
							gropRoleBuffer.append("<role>" + role.getRoleName()
									+ "</role>");
						}
						roleBuffer.append("<role>" + roleGrp.getGroupName()
								+ "</role>");
						gropRoleBuffer.append("</" + roleGrp.getGroupName()
								+ ">");
					}
				}
				List<RoleDefinition> avaliableRoles = DBOperations
						.getRolesForSelectedUserSociety(apt);
				List<RoleDefinition> rolesNotAvailableinGroups = new ArrayList<RoleDefinition>();
				if (avaliableRoles != null && avaliableRoles.isEmpty() == false) {
					for (RoleDefinition allRoles : avaliableRoles) {
						availableRoleIdsList.add(String.valueOf(allRoles
								.getId()));
					}
				}
				List<String> remaingRoleList = new ArrayList<String>();
				remaingRoleList.addAll(availableRoleIdsList);
				remaingRoleList.removeAll(roleIdListinGroups);
				for (String roleString : remaingRoleList) {
					RoleDefinition role = (RoleDefinition) DBOperations
							.getSingleDatabaseObject(RoleDefinition.class,
									"id=" + roleString);
					rolesNotAvailableinGroups.add(role);
				}
				if (rolesNotAvailableinGroups != null
						&& rolesNotAvailableinGroups.isEmpty() == false) {
					for (RoleDefinition finalRoles : rolesNotAvailableinGroups) {
						roleBuffer.append("<role>" + finalRoles.getRoleName()
								+ "</role>");
					}
				}
				if (gropRoleBuffer != null) {
					roleBuffer.append(gropRoleBuffer);
				}
				roleBuffer.append("</roles>");
				selectionListBuffer.append(roleBuffer);
				// role part ends

				// division part started
				StringBuffer divsionBuffer = new StringBuffer();
				List<String> mainDivisionsList = new ArrayList<String>();
				String divisionGroupquery = " apartmentid=" + apt.getId()
						+ " and groupvalue=" + Group.DIVISION_EXISTS
						+ " and VoiceMailGroup=true";
				List<Group> divsionGroupsList = UserOperations
						.getIVRGroupsList(divisionGroupquery);
				mainDivisionsList = (List<String>) apt.getDistinctBlocks();
				divsionBuffer.append("<divisions>");
				divsionBuffer.append("<division>ALL</division>");
				List<String> divsListinGroups = new ArrayList<String>();
				JSONArray divGrpArray = new JSONArray();
				StringBuffer groupDivisionBuffer = new StringBuffer();
				if (divsionGroupsList != null
						&& divsionGroupsList.isEmpty() == false) {
					for (Group divisGrp : divsionGroupsList) {
						JSONObject divisionGroupObj = (JSONObject) xmlSerializer
								.read(divisGrp.getXmlGroupsContents());
						divsListinGroups.addAll(divisionGroupObj
								.getJSONArray("blocksList"));
						groupDivisionBuffer.append("<"
								+ divisGrp.getGroupName()
								+ "><division>ALL</division>");
						divGrpArray = divisionGroupObj
								.getJSONArray("blocksList");
						for (int i = 0; i < divGrpArray.size(); i++) {
							groupDivisionBuffer.append("<division>"
									+ divGrpArray.getString(i) + "</division>");
						}
						divsionBuffer.append("<division>"
								+ divisGrp.getGroupName() + "</division>");
						groupDivisionBuffer.append("</"
								+ divisGrp.getGroupName() + ">");
					}
				}
				List<String> remainingDivisionList = new ArrayList<String>();
				remainingDivisionList.addAll(mainDivisionsList);
				remainingDivisionList.removeAll(divsListinGroups);
				for (String block : remainingDivisionList) {
					divsionBuffer.append("<division>" + block + "</division>");
				}
				if (groupDivisionBuffer != null) {
					divsionBuffer.append(groupDivisionBuffer);
				}
				divsionBuffer.append("</divisions>");
				selectionListBuffer.append(divsionBuffer);
				// division part ends

				// groups part started
				List<Group> groupsList = DBOperations.getGroups(apt);
				StringBuffer groupBuffer = new StringBuffer();
				groupBuffer.append("<groups>");
				if (groupsList != null && groupsList.isEmpty() == false) {
					groupBuffer.append("<group>ALL</group>");
					for (Group grp : groupsList) {
						groupBuffer.append("<group>" + grp.getGroupName()
								+ "</group>");
					}
				}
				groupBuffer.append("</groups>");
				selectionListBuffer.append(groupBuffer);
				// groups part ends
				selectionListBuffer.append("</selectionlist>");
				selectionListResponse = processSucess(serviceType,
						functionType, gpzCode, selectionListBuffer.toString());
			} else {
				selectionListResponse = processError(
						PropertiesUtil.getProperty("invalidgroupzid_code"),
						PropertiesUtil.getProperty("invalidgroupzid_message"));
				return selectionListResponse;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			selectionListResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}
		return selectionListResponse;
	}

	// publishing xml
	public String publishXMLForIVRRecord(String serviceType,
			String functionType, String gpzCode, JSONObject json) {
		String mobileNumber = "";
		String countryCode = "";
		String publishXMLResponse = "";
		StringBuffer publishXMLBuffer = new StringBuffer();
		System.out.println("JSON STRING:"+json.toString(3));
		try {
			countryCode = json.getJSONObject("request").getJSONObject("mobile")
					.getString("countrycode");
			mobileNumber = json.getJSONObject("request")
					.getJSONObject("mobile").getString("mobilenumber");
			if (restUtils.isEmpty(countryCode) == false) {
				publishXMLResponse = processError(
						PropertiesUtil.getProperty("countryempty_code"),
						PropertiesUtil.getProperty("countryempty_message"));
				return publishXMLResponse;
			}
			if (Utils.isNumber(countryCode) == false) {
				publishXMLResponse = processError(
						PropertiesUtil.getProperty("invalidcountry_code"),
						PropertiesUtil.getProperty("invalidcountry_message"));
				return publishXMLResponse;
			}
			if (restUtils.isEmpty(mobileNumber) == false) {
				publishXMLResponse = processError(
						PropertiesUtil.getProperty("Mobileempty_code"),
						PropertiesUtil.getProperty("Mobileempty_message"));
				return publishXMLResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				publishXMLResponse = processError(
						PropertiesUtil.getProperty("invalidmobile_code"),
						PropertiesUtil.getProperty("invalidmobile_message"));
				return publishXMLResponse;
			}
			String completeMobile = "+" + countryCode + "." + mobileNumber;
			Apartment apt = DBOperations.getApartmentByCode(gpzCode);

			if (apt != null) {
				String query = " flat.apartmentid=" + apt.getId()
						+ " and person.mobile='" + completeMobile
						+ "' and roledefinition.rolevalue&"
						+ RoleDefinition.CAN_RECORD_MESSAGES;
				List<UserFlatMapping> membersList = UserOperations
						.checkMobileNumberValidForGroupz(query);
				if (membersList == null || membersList.isEmpty() == true
						|| membersList.size() == 0) {
					publishXMLResponse = processError(
							PropertiesUtil.getProperty("invalidmobile_code"),
							PropertiesUtil.getProperty("invalidmobile_message"));
					return publishXMLResponse;
				}
				if (json.getJSONObject("request").getString("data") == null
						|| json.getJSONObject("request").getString("data")
								.isEmpty() == true
						|| json.getJSONObject("request").getString("data")
								.equalsIgnoreCase("[]") == true) {
					System.out.println("Data part IS EMPTY");
					publishXMLResponse = processError(
							PropertiesUtil.getProperty("urlList_tag_code"),
							PropertiesUtil.getProperty("urlList_tag_message"));
					return publishXMLResponse;
				}
				if (json.getJSONObject("request").getJSONObject("data")
						.containsKey("urlsList") == true) {
					System.out.println("URL LIST Tag present");
					String kanndaURL = null;
					String tamilURL = null;
					String malayalamURL = null;
					String englishURL = null;
					String hindiURL = null;
					String teluguURL = null;
					System.out.println("URL TAG:"
							+ json.getJSONObject("request")
									.getJSONObject("data")
									.getString("urlsList"));
					if (json.getJSONObject("request").getJSONObject("data")
							.getString("urlsList") == null
							|| json.getJSONObject("request")
									.getJSONObject("data")
									.getString("urlsList").isEmpty() == true
							|| json.getJSONObject("request")
									.getJSONObject("data")
									.getString("urlsList")
									.equalsIgnoreCase("[]") == true) {
						System.out.println("URL TAG LIST IS EMPTY");
						publishXMLResponse = processError(
								PropertiesUtil
										.getProperty("urlList_empty_code"),
								PropertiesUtil
										.getProperty("urlList_empty_message"));
						return publishXMLResponse;
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("english") == true) {
						englishURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList").getString("english");
						System.out.println("English URL:" + englishURL);
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("malayalam") == true) {
						malayalamURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList")
								.getString("malayalam");
						System.out.println("Malayalam URL:" + malayalamURL);
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("kannada") == true) {
						kanndaURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList").getString("kannada");
						System.out.println("Kannada URL:" + kanndaURL);
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("tamil") == true) {
						tamilURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList").getString("tamil");
						System.out.println("Tamil URL:" + tamilURL);
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("hindi") == true) {
						hindiURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList").getString("hindi");
						System.out.println("Hindi URL:" + hindiURL);
					}
					if (json.getJSONObject("request").getJSONObject("data")
							.getJSONObject("urlsList").containsKey("telugu") == true) {
						teluguURL = json.getJSONObject("request")
								.getJSONObject("data")
								.getJSONObject("urlsList").getString("telugu");
						System.out.println("Telugu URL:" + teluguURL);
					}
					/*
					 * else{ publishXMLResponse = processError(
					 * PropertiesUtil.getProperty("urlList_invalid_code"),
					 * PropertiesUtil.getProperty("urlList_invalid_message"));
					 * return publishXMLResponse; }
					 */
				} else {
					publishXMLResponse = processError(
							PropertiesUtil.getProperty("urlList_tag_code"),
							PropertiesUtil.getProperty("urlList_tag_message"));
					return publishXMLResponse;
				}
				String allUsers = json.getJSONObject("request").getString(
						"allusers");
				List<Object> selectedGroups = new ArrayList<Object>();
				List<Object> selectedRoles = new ArrayList<Object>();
				List<Object> selectedDivisions = new ArrayList<Object>();
				boolean allUsersFlag = false;
				System.out.println("========All users Starts=========");
				System.out.println("All users=" + allUsers);
				System.out.println("========All users ends=========");
				if (allUsers.equalsIgnoreCase("true") == true) {
					System.out.println("ALL USERS IS TRUE");
					allUsersFlag = true;
				} else {
					System.out.println("ALL USERS IS FALSE");
					allUsersFlag = false;
					// for roles
					if (json.getJSONObject("request").containsKey("roles") == true) {
						String role = null;
						JSONObject rolesObj = json.getJSONObject("request")
								.optJSONObject("roles");
						JSONArray rolesList = null;
						System.out.println("========Roles Starts=========");
						if (rolesObj == null) {
							rolesList = json.getJSONObject("request")
									.optJSONArray("roles");
							for (int i = 0; i < rolesList.size(); i++) {
								System.out.println("Roles:"
										+ rolesList.getJSONObject(i).getString("role"));
								String roleQry = "RoleName = '"
										+ rolesList.getJSONObject(i).getString("role")
										+ "' and SocietyId=" + apt.getId();
								RoleDefinition multipleRoles = (RoleDefinition) DBOperations
										.getSingleDatabaseObject(
												RoleDefinition.class, roleQry);
								if (multipleRoles != null) {
									selectedRoles.add(Integer
											.toString(multipleRoles.getId()));
								}
							}
						} else {
							role = json.getJSONObject("request")
									.getJSONObject("roles").getJSONObject("element").getString("role");
							if (role.equalsIgnoreCase("all") == true) {
								System.out.println("All roles selected");
								List<RoleDefinition> allRolesList = DBOperations
										.getRolesForSelectedUserSociety(apt);
								if (allRolesList != null) {
									for (RoleDefinition allRoles : allRolesList) {
										selectedRoles.add(Integer
												.toString(allRoles.getId()));
									}
								}
							} else {
								System.out.println("Only one role selected");
								String roleQry = "RoleName = '" + role
										+ "' and SocietyId=" + apt.getId();
								RoleDefinition singeRole = (RoleDefinition) DBOperations
										.getSingleDatabaseObject(
												RoleDefinition.class, roleQry);
								if (singeRole != null) {
									selectedRoles.add(Integer
											.toString(singeRole.getId()));
								}
							}
						}
						System.out.println("=======Roles Ends==========");
					}
					// for divisions
					if (json.getJSONObject("request").containsKey("divisions") == true) {
						String division = null;
						JSONObject divisionsObj = json.getJSONObject("request")
								.optJSONObject("divisions");
						JSONArray divisionsList = null;
						System.out.println("======Divisions starts==========");
						if (divisionsObj == null) {
							divisionsList = json.getJSONObject("request")
									.optJSONArray("divisions");
							for (int i = 0; i < divisionsList.size(); i++) {
								String div = divisionsList.getJSONObject(i).getString("division");
								System.out.println("Divisions:" + div);
								selectedDivisions.add(div);
							}
						} else {
							division = json.getJSONObject("request")
									.getJSONObject("divisions").getJSONObject("element")
									.getString("division");
							if (division.equalsIgnoreCase("all") == true) {
								System.out.println("All divisions selected");
								Collection<String> allDivisionsList = apt
										.getDistinctBlocks();
								if (allDivisionsList != null) {
									selectedDivisions.addAll(allDivisionsList);
								}
							} else {
								System.out
										.println("Only one division selected");
								selectedDivisions.add(division);
							}
						}
						System.out.println("=======Divisions ends==========");
					}
					// for groups
					if (json.getJSONObject("request").containsKey("groups") == true) {
						String group = null;
						JSONObject groupsObj = json.getJSONObject("request")
								.optJSONObject("groups");
						JSONArray groupsList = null;
						System.out.println("======Groups starts===========");
						if (groupsObj == null) {
							groupsList = json.getJSONObject("request")
									.optJSONArray("groups");
							for (int i = 0; i < groupsList.size(); i++) {
								System.out.println("Groups:"
										+ groupsList.getJSONObject(i).getString("group"));
								String grpQry = "GroupName ='"
										+ groupsList.getJSONObject(i).getString("group")
										+ "' and ApartmentId=" + apt.getId();
								Group multipleGroups = (Group) DBOperations
										.getSingleDatabaseObject(Group.class,
												grpQry);
								if (multipleGroups != null) {
									selectedGroups.add(Integer
											.toString(multipleGroups.getId()));
								}
							}

						} else {
							group = json.getJSONObject("request")
									.getJSONObject("groups").getJSONObject("element").getString("group");
							if (group.equalsIgnoreCase("all") == true) {
								System.out.println("All groups selected");
								List<Group> allGroupsList = DBOperations
										.getGroups(apt);
								if (allGroupsList != null) {
									for (Group allGroups : allGroupsList) {
										selectedGroups.add(Integer
												.toString(allGroups.getId()));
									}
								}
							} else {
								String grpQry = "GroupName ='" + group
										+ "' and ApartmentId=" + apt.getId();
								Group singleGroup = (Group) DBOperations
										.getSingleDatabaseObject(Group.class,
												grpQry);
								if (singleGroup != null) {
									selectedGroups.add(Integer
											.toString(singleGroup.getId()));
								}
								System.out.println("Only one group selected");
							}
						}
						System.out.println("=======Groups ends==========");
					}
				}
				String userXMLGroups = DatabaseXmlHelper
						.createXMLUserGroupString(selectedGroups, null,
								selectedRoles, selectedDivisions, allUsersFlag,
								false);
				System.out.println("User XML groups:" + userXMLGroups);
				publishXMLBuffer.append("<xml>");
				publishXMLBuffer.append("<response>");
				publishXMLBuffer.append("<servicetype>");
				publishXMLBuffer.append(serviceType);
				publishXMLBuffer.append("</servicetype>");
				publishXMLBuffer.append("<functiontype>");
				publishXMLBuffer.append(functionType);
				publishXMLBuffer.append("</functiontype>");
				publishXMLBuffer.append("<statuscode>");
				publishXMLBuffer.append(PropertiesUtil
						.getProperty("statuscodesuccessvalue"));
				publishXMLBuffer.append("</statuscode>");
				publishXMLBuffer.append("<statusmessage>");
				publishXMLBuffer.append(PropertiesUtil
						.getProperty("statusmessagesuccessvalue"));
				publishXMLBuffer.append("</statusmessage>");
				publishXMLBuffer.append("</response>");
				publishXMLBuffer.append("</xml>");
				publishXMLResponse = publishXMLBuffer.toString();
			} else {
				publishXMLResponse = processError(
						PropertiesUtil.getProperty("invalidgroupzid_code"),
						PropertiesUtil.getProperty("invalidgroupzid_message"));
				return publishXMLResponse;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			publishXMLResponse = processError(
					PropertiesUtil.getProperty("XMLRequest_code"),
					PropertiesUtil.getProperty("XMLRequest_message"));
		}

		return publishXMLResponse;
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

	// success Response
	public String processSucess(String sType, String fType, String gpzCode,
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
		if (gpzCode != null && gpzCode.trim().isEmpty() == false
				&& gpzCode.equalsIgnoreCase("") == false) {
			sucessXMLString.append("<groupzcode>");
			sucessXMLString.append(gpzCode);
			sucessXMLString.append("</groupzcode>");
		}
		sucessXMLString.append(message);
		sucessXMLString.append("</response>");
		sucessXMLString.append("</xml>");
		return sucessXMLString.toString();

	}

}
