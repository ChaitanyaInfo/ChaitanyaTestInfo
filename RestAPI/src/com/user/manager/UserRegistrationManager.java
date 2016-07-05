package com.user.manager;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.sf.json.xml.XMLSerializer;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.ApartmentSettings;
import com.apartment.database.tables.Flat;
import com.apartment.database.tables.Person;
import com.apartment.database.tables.RegistrationInvitation;
import com.apartment.database.tables.TemporaryDBObject;
import com.apartment.database.tables.User;
import com.apartment.database.tables.UserFlatMapping;
import com.apartment.defs.UserRegistrationRequest;
import com.apartment.events.NotificationEvent;
import com.apartment.events.NotificationManager;
import com.apartment.modules.apartmentmanagement.ApartmentCounterManager;
import com.apartment.modules.usermanagement.UserPropertyChangeListener;
import com.apartment.util.GroupzTriepon;
import com.apartment.util.Utils;
import com.user.utils.IPAddressCheck;
import com.user.utils.RestUtils;

public class UserRegistrationManager {
	private IPAddressCheck ipCheck = new IPAddressCheck();
	private RestUtils restUtils = new RestUtils();
	private List<String> ipAddressList;

	private String groupzsyncResponse = "";
	private String name = "";
	private String userName = "";
	private String preferredEmail = "";
	private String command = "";
	private String key = "";
	private String flatNo = "";
	private String block = "";
	private String type = "";
	private String countryCode = "";
	private String mobileNumber = "";
	private String gender = "";
	private String proprietaryData = "";
	private String autoApprove = "";
	private String ipAddress = "";
	
	//newly field added sitesize
	private String siteSize="";

	// newly added for landline
	private String landlineCountryCode = "";
	private String landlineStateCode = "";
	private String landlineNumber = "";

	private String contactComments = "";

	private EmailAndSmsManager emsManger = new EmailAndSmsManager();

	public String addNewUser(String groupzsyncRequest, HttpServletRequest req) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		JSONObject json = new JSONObject();
		try {
			json = (JSONObject) xmlSerializer.read(groupzsyncRequest);
			System.out.println("XML to JSON " + json.toString(2));
			command = json.getString("command");
			key = json.getString("key");
			name = json.getJSONObject("data").getString("name");
			userName = json.getJSONObject("data").getString("username");
			preferredEmail = json.getJSONObject("data").getString(
					"preferredemail");
			flatNo = json.getJSONObject("data").getString("flatno");
			block = json.getJSONObject("data").getString("block");
			type = json.getJSONObject("data").getString("type");
			countryCode = json.getJSONObject("data").getJSONObject("mobile")
					.getString("countrycode");
			mobileNumber = json.getJSONObject("data").getJSONObject("mobile")
					.getString("mobilenumber");
			gender = json.getJSONObject("data").getString("gender");
			proprietaryData = json.getJSONObject("data").getString(
					"proprietarydata");
			autoApprove = json.getJSONObject("data").getString("autoapprove");

			// newly added for lanline starts
			if (json.getJSONObject("data").containsKey("landline") == true) {
				landlineCountryCode = json.getJSONObject("data")
						.getJSONObject("landline")
						.getString("landlinecountrycode");
				landlineStateCode = json.getJSONObject("data")
						.getJSONObject("landline")
						.getString("landlinestatecode");
				landlineNumber = json.getJSONObject("data")
						.getJSONObject("landline").getString("landlinenumber");
			}
			if (json.getJSONObject("data").containsKey("contactcomments") == true) {
				contactComments = json.getJSONObject("data").getString(
						"contactcomments");
			} else {
				contactComments = "";
			}
			if (json.getJSONObject("data").containsKey("sitesize") == true) {
				siteSize = json.getJSONObject("data").getString(
						"sitesize");
			} else {
				siteSize = "0";
			}

			if (isEmpty(command) == false) {
				groupzsyncResponse = processError(41008,
						"Command cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(key) == false) {
				groupzsyncResponse = processError(41009, "Key cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(name) == false) {
				groupzsyncResponse = processError(41010, "Name cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(userName) == false) {
				groupzsyncResponse = processError(41011,
						"Username cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(preferredEmail) == false) {
				preferredEmail = userName;
			}
			if (isEmpty(block) == false) {
				groupzsyncResponse = processError(41013,
						"Block cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(type) == false) {
				groupzsyncResponse = processError(41014, "Type cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(countryCode) == false) {
				countryCode = "91";
			}
			if (isEmpty(mobileNumber) == false) {
				groupzsyncResponse = processError(41015,
						"Mobile Number cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(gender) == false) {
				groupzsyncResponse = processError(41016,
						"Gender cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(proprietaryData) == false) {
				groupzsyncResponse = processError(41017,
						"Proprietary Data cannot be empty");
				return groupzsyncResponse;
			}
			if (isEmpty(autoApprove) == false) {
				groupzsyncResponse = processError(41018,
						"Auto Approve cannot be empty");
				return groupzsyncResponse;
			}
			if (isValidEmail(userName) == false) {
				groupzsyncResponse = processError(41019, "Invalid Email id");
				return groupzsyncResponse;
			}
			if (isValidEmail(preferredEmail) == false) {
				groupzsyncResponse = processError(41019, "Invalid Email id");
				return groupzsyncResponse;
			}
			// newly added for mobile number validation starts
			if (Utils.isNumber(countryCode) == false) {
				groupzsyncResponse = processError(41020, "Invalid Country code");
				return groupzsyncResponse;
			}
			if (Utils.isNumber(mobileNumber) == false) {
				groupzsyncResponse = processError(41021,
						"Invalid Mobile Number");
				return groupzsyncResponse;
			}
			// newly added for mobile number validation ends
			if ((command.equals("ADD_NEW_USER"))
					|| (command.equals("ADD_NEW_CONTACT"))) {
				User usr = new User();
				RegistrationInvitation regInv = DBOperations
						.getRegistrationInvitation(key);
				if (regInv != null) {
					ApartmentSettings aptSettings = DBOperations
							.getApartmentSettings(regInv.getApartment().getId());
					if (regInv.getStartDate().before(new Date())) {
						if (regInv.getEndDate().after(new Date())) {
							if (regInv.isContact() == false) {
								if (isEmpty(flatNo) == false) {
									groupzsyncResponse = processError(41012,
											"Flat no cannot be empty");
									return groupzsyncResponse;
								}
							}
							ipAddress = req.getRemoteAddr();
							ipAddressList = new ArrayList<String>();
							String stList = (String) DBOperations
									.getIpAddresses(
											regInv.getApartment().getId()).get(
											0);
							if (stList != null) {
								StringTokenizer st = new StringTokenizer(
										stList, "\n");
								while (st.hasMoreTokens()) {
									ipAddressList.add(st.nextToken());
								}
							}
							if (ipCheck.checkIPAddressInList(ipAddress,
									ipAddressList) == true) {
								//newly added for landline starts
								String landline="";
								if((landlineCountryCode!=null && landlineCountryCode.isEmpty()==false && landlineCountryCode.equalsIgnoreCase("")==false)&&
										(landlineStateCode!=null && landlineStateCode.isEmpty()==false && landlineStateCode.equalsIgnoreCase("")==false)&&
										(landlineNumber!=null && landlineNumber.isEmpty()==false && landlineNumber.equalsIgnoreCase("")==false)){
								landline="+"+landlineCountryCode+"."+landlineStateCode+"."+landlineNumber;
								}
								String mobile = "+";
								mobile += countryCode + "." + mobileNumber;
								User user = DBOperations.getUser(userName);
								if (user != null) {
									if (command.equals("ADD_NEW_USER") == true) {
										if (autoApprove
												.equalsIgnoreCase("true") == true) {
											String encodedString = "";
											GroupzTriepon grt = new GroupzTriepon();
											if (json.getJSONObject("data")
													.containsKey(
															"usermappingkey") == true) {
												encodedString = json
														.getJSONObject("data")
														.getString(
																"usermappingkey");
												if (isEmpty(encodedString) == false) {
													groupzsyncResponse = processError(
															41027,
															"User already registered");
													return groupzsyncResponse;
												}
												try {
													boolean decodeId = grt
															.decode(encodedString);
													if (decodeId == false) {
														Calendar timeCal = Calendar
																.getInstance();
														timeCal = grt
																.getTimestamp();
														int usrId = grt
																.getMemberId();
														int aptId = grt
																.getGroupzId();
														if (usrId == user
																.getId()
																&& aptId == regInv
																		.getApartment()
																		.getId()) {
															Date currentDate = new Date();
															Date encodedDate = timeCal
																	.getTime();
															System.out
																	.println("IN AUTOAPPROVE=true --> Time in encoded string:"
																			+ timeCal
																					.getTime()
																			+ " current time:"
																			+ new Date());
															if (restUtils
																	.checktimeWithinHour(
																			encodedDate,
																			currentDate) == true) {
																System.out
																		.println("IN AUTOAPPROVE=true --> Register in table directly");
																saveDirectly(
																		name,
																		mobile,
																		gender,
																		preferredEmail,
																		mobileNumber,
																		userName,
																		block,
																		type,
																		flatNo,
																		proprietaryData,
																		contactComments,
																		regInv,
																		aptSettings,
																		true,
																		user,landline,siteSize);
															} else {
																System.out
																		.println("IN AUTOAPPROVE=true --> Invalid encoded string where time doesn't match");
																groupzsyncResponse = processError(
																		41026,
																		"Invalid User mapping key");
																return groupzsyncResponse;
															}
														} else {
															System.out
																	.println("IN AUTOAPPROVE=true --> Invalid encoded string where user id and apt id doesn't match");
															groupzsyncResponse = processError(
																	41026,
																	"Invalid User mapping key");
															return groupzsyncResponse;
														}
													} else {
														groupzsyncResponse = processError(
																41026,
																"Invalid User mapping key");
														return groupzsyncResponse;
													}
												} catch (Exception e) {
													groupzsyncResponse = processError(
															41026,
															"Invalid User mapping key");
													return groupzsyncResponse;
												}
											} else {
												int uid = user.getId();
												int aid = regInv.getApartment()
														.getId();
												int modid = 21;
												GroupzTriepon gr = new GroupzTriepon(
														uid, aid,
														(short) modid, 1);
												Calendar cal = Calendar
														.getInstance();
												Date d = new Date();
												cal.setTime(d);
												gr.setTimestamp(cal);
												String id = gr.encode();
												System.out
														.println("Auto approve==>true Id from groupzTriepon : "
																+ id);
												groupzsyncResponse = processEncodedKey(
														41028, id);
												return groupzsyncResponse;
											}
										} else {
											String encodedString = "";
											GroupzTriepon grt = new GroupzTriepon();
											if (json.getJSONObject("data")
													.containsKey(
															"usermappingkey") == true) {
												encodedString = json
														.getJSONObject("data")
														.getString(
																"usermappingkey");
												if (isEmpty(encodedString) == false) {
													groupzsyncResponse = processError(
															41027,
															"User already registered");
													return groupzsyncResponse;
												}
												try {
													boolean decodeId = grt
															.decode(encodedString);
													if (decodeId == false) {
														Calendar timeCal = Calendar
																.getInstance();
														timeCal = grt
																.getTimestamp();
														int usrId = grt
																.getMemberId();
														int aptId = grt
																.getGroupzId();
														if (usrId == user
																.getId()
																&& aptId == regInv
																		.getApartment()
																		.getId()) {
															System.out
																	.println("IN AUTOAPPROVE=false --> Time in encoded string:"
																			+ timeCal
																					.getTime()
																			+ " current time:"
																			+ new Date());
															Date currentDate = new Date();
															Date encodedDate = timeCal
																	.getTime();
															if (restUtils
																	.checktimeWithinHour(
																			encodedDate,
																			currentDate) == true) {
																System.out
																		.println("Register in queue");
																saveInQueue(
																		name,
																		mobile,
																		gender,
																		preferredEmail,
																		mobileNumber,
																		userName,
																		block,
																		type,
																		flatNo,
																		proprietaryData,
																		contactComments,
																		regInv,
																		aptSettings,
																		true,
																		user,landline,siteSize);
															} else {
																System.out
																		.println("IN AUTOAPPROVE=false --> Invalid encoded string where time doesn't match");
																groupzsyncResponse = processError(
																		41026,
																		"Invalid User mapping key");
																return groupzsyncResponse;
															}
														} else {
															System.out
																	.println("IN AUTOAPPROVE=false --> Invalid encoded string where user id and apt id doesn't match");
															groupzsyncResponse = processError(
																	41026,
																	"Invalid User mapping key");
															return groupzsyncResponse;
														}
													} else {
														groupzsyncResponse = processError(
																41026,
																"Invalid User mapping key");
														return groupzsyncResponse;
													}
												} catch (Exception e) {
													groupzsyncResponse = processError(
															41026,
															"Invalid User mapping key");
													return groupzsyncResponse;
												}
											} else {
												int uid = user.getId();
												int aid = regInv.getApartment()
														.getId();
												int modid = 21;
												GroupzTriepon gr = new GroupzTriepon(
														uid, aid,
														(short) modid, 1);
												Calendar cal = Calendar
														.getInstance();
												Date d = new Date();
												cal.setTime(d);
												gr.setTimestamp(cal);
												String id = gr.encode();
												System.out
														.println("In Auto approve==>false Id from groupzTriepon : "
																+ id);
												groupzsyncResponse = processEncodedKey(
														41028, id);
												return groupzsyncResponse;
											}

										}
									} else {
										List<UserFlatMapping> ufmList = DBOperations
												.getUserFlats(user);
										int count = 0;
										for (UserFlatMapping uf : ufmList) {
											if (uf.getFlat().getApartment()
													.getId() == regInv
													.getApartment().getId()) {
												count++;
											}
										}
										if (count == 0) {
											System.out
													.println("Inisde for contact loop");
											System.out
													.println("User name not exists for different groupz");
											if (autoApprove
													.equalsIgnoreCase("false")) {
												System.out
														.println("if auto approve equals false store it in temporary objects");
												saveInQueue(name, mobile,
														gender, preferredEmail,
														mobileNumber, userName,
														block, type, flatNo,
														proprietaryData,
														contactComments,
														regInv, aptSettings,
														true, user,landline,siteSize);
											} else {
												System.out
														.println("if auto approve equals true directly store it in respected tables");
												saveDirectly(name, mobile,
														gender, preferredEmail,
														mobileNumber, userName,
														block, type, flatNo,
														proprietaryData,
														contactComments,
														regInv, aptSettings,
														true, user,landline,siteSize);
											}
										} else {
											System.out
													.println("Already registered");
											groupzsyncResponse = processError(
													41005,
													"User name already registered");
											return groupzsyncResponse;
										}
									}

								} else {
									System.out
											.println("outside for loop--new user");
									if (autoApprove.equalsIgnoreCase("false")) {
										System.out
												.println("if auto approve equals false store it in temporary objects");
										saveInQueue(name, mobile, gender,
												preferredEmail, mobileNumber,
												userName, block, type, flatNo,
												proprietaryData,
												contactComments, regInv,
												aptSettings, false, usr,landline,siteSize);
									} else {
										System.out
												.println("if auto approve equals true directly store it in respected tables");
										saveDirectly(name, mobile, gender,
												preferredEmail, mobileNumber,
												userName, block, type, flatNo,
												proprietaryData,
												contactComments, regInv,
												aptSettings, false, usr,landline,siteSize);
									}
								}
							} else {
								groupzsyncResponse = processError(41006,
										"Access Denied");
							}
						} else {
							groupzsyncResponse = processError(41004,
									"Key expired");
						}
					} else {
						groupzsyncResponse = processError(41003,
								"Key not enabled");
					}
				} else {
					groupzsyncResponse = processError(41002, "Invalid key");
				}

			} else {
				groupzsyncResponse = processError(41007, "Invalid Command");
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();

			groupzsyncResponse = processError(41001, "Cannot parse XML request");
		}
		return groupzsyncResponse;

	}

	// framing encoded key with response
	public String processEncodedKey(int statusCode, String message) {
		StringBuffer errorXMLString = new StringBuffer();
		errorXMLString.append("<groupzsyncresponse>");
		errorXMLString.append("<statuscode>");
		errorXMLString.append(statusCode);
		errorXMLString.append("</statuscode>");
		errorXMLString.append("<statusmessage>");
		errorXMLString.append("User already exists");
		errorXMLString.append("</statusmessage>");
		errorXMLString.append("<usermappingkey>");
		if (message != null) {
			errorXMLString.append(message);
		}
		errorXMLString.append("</usermappingkey>");
		errorXMLString.append("</groupzsyncresponse>");
		return errorXMLString.toString();

	}

	// Invalid message response
	public String processError(int statusCode, String message) {
		StringBuffer errorXMLString = new StringBuffer();
		errorXMLString.append("<groupzsyncresponse>");
		errorXMLString.append("<statuscode>");
		errorXMLString.append(statusCode);
		errorXMLString.append("</statuscode>");
		errorXMLString.append("<statusmessage>");
		if (message != null) {
			errorXMLString.append(message);
		}
		errorXMLString.append("</statusmessage>");
		errorXMLString.append("</groupzsyncresponse>");
		return errorXMLString.toString();

	}

	// success response
	public String processSucess(boolean approve, String message) {
		StringBuffer sucessXMLString = new StringBuffer();
		sucessXMLString.append("<groupzsyncresponse>");
		sucessXMLString.append("<statuscode>");
		sucessXMLString.append("0");
		sucessXMLString.append("</statuscode>");
		sucessXMLString.append("<statusmessage>");
		sucessXMLString.append("sucess");
		sucessXMLString.append("</statusmessage>");
		if (approve == true) {
			sucessXMLString.append("<data>");
			sucessXMLString.append("<uniquieid>");
			sucessXMLString.append(message);
			sucessXMLString.append("</uniquieid>");
			sucessXMLString.append("</data>");

		}
		sucessXMLString.append("</groupzsyncresponse>");
		return sucessXMLString.toString();

	}

	public boolean isEmpty(String test) {
		if (test == null || test.trim().isEmpty() == true
				|| test.equalsIgnoreCase("[]") || test == "") {
			return false;
		}
		return true;
	}

	public boolean isValidEmail(String email) {
		String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

		if (email.matches(EMAIL_REGEX) == true) {
			return true;
		}

		return false;
	}

	// save function for storing details in respective tables
	public void saveDirectly(String name, String mobile, String gender,
			String prefEmail, String password, String usrName, String block,
			String type, String flatNo, String propData, String contComments,
			RegistrationInvitation regInv, ApartmentSettings aptSettings,
			boolean userPresent, User userExists,String landline,String siteSize) {
		Person person = new Person();
		User newUsr = new User();
		Flat flat = new Flat();
		UserFlatMapping ufm = new UserFlatMapping();
		person.setName(name);
		person.setMobile(mobile);
		person.setGender(gender);
		person.setRelationship("Myself");
		//newly added for landline
		if (landline != null && landline.isEmpty() == false
				&& landline.equalsIgnoreCase("") == false) {
			person.setPhone(landline);
		}
		person.save();
		if (userPresent == false) {
			newUsr.setAdmin(false);
			newUsr.setEmail(preferredEmail);
			newUsr.setEnabled(true);
			newUsr.setPassword(mobileNumber);
			newUsr.setUserName(userName);
		}
		flat.setBlockStreetDetails(block);
		flat.setSubDivision(type);
		flat.setSiteSize(Integer.parseInt(siteSize));
		if (regInv.isContact() == false) {
			flat.setDoorNo(flatNo);
		}
		if (isEmpty(contactComments) == true) {
			flat.setContactComments(contactComments);
		}
		flat.setApartment(regInv.getApartment());
		flat.setContact(regInv.isContact());
		flat.setStartDate(new Date());
		flat.save();
		person.setFlat(flat);
		person.save();
		flat.setRegisteredPerson(person);
		flat.setProprietaryData(proprietaryData);
		if (regInv.isContact() == false) {
			int uniqueIdCounter = ApartmentCounterManager
					.getNextUserCounter(regInv.getApartment());
			String groupzCode = regInv.getApartment().getSocietyCode();
			if (Utils.isNullOrEmpty(groupzCode)) {
				groupzCode = "Groupz";
			}
			String uniqueId = String.format("%s%s%06d", groupzCode, "-",
					uniqueIdCounter);
			flat.setUniqueId(uniqueId);
			flat.setUniqueIdCounter(uniqueIdCounter);
		}
		flat.save();
		ufm.setFlat(flat);
		if (userPresent == false) {
			System.out.println("new user save in ufm");
			ufm.setUser(newUsr);
		} else {
			System.out.println("user exists save in ufm");
			ufm.setUser(userExists);
		}
		ufm.setEnabled(true);
		if (regInv.isContact() == false) {
			if (aptSettings != null)
				ufm.setRole(aptSettings.getDefaultRole());
		}
		if (userPresent == false) {
			newUsr.save();
		}
		ufm.save();
		if (regInv.isContact() == false) {
			groupzsyncResponse = processSucess(true,
					Utils.encode(flat.getUniqueId()));
		} else {
			groupzsyncResponse = processSucess(false, "Contact added");
		}
		// message forwarding starts
		String contactApprovedMailTitle = regInv.isContact() ? aptSettings
				.getContactApprovedMailTitle() : aptSettings
				.getUserApprovedMailTitle();
		String contactApprovedMailText = regInv.isContact() ? aptSettings
				.getContactApprovedMailText() : aptSettings
				.getUserApprovedMailText();
		String approvedSMSText = null;
		if (regInv.isContact() == false) {
			approvedSMSText = "Your access to online "
					+ ufm.getFlat().getApartment().getName()
					+ " has been activated. User name is "
					+ ufm.getUser().getUserName() + ",password is "
					+ ufm.getUser().getPassword()
					+ ",please check email for details.";
		} else {
			approvedSMSText = aptSettings.getContactApprovedSMSText();
			if (com.apartment.util.Utils.isNullOrEmpty(approvedSMSText)) {
				approvedSMSText = "You have been added as a contact in "
						+ ufm.getFlat().getApartment().getName();
			} else {
				approvedSMSText = approvedSMSText.replaceAll("\\$Name", ufm
						.getFlat().getRegisteredPerson().getName());
				approvedSMSText = approvedSMSText.replaceAll("\\$Groupz", ufm
						.getFlat().getApartment().getName());
			}
		}
		if (com.apartment.util.Utils.isNullOrEmpty(contactApprovedMailTitle)) {
			contactApprovedMailTitle = "groupZ account activated";
		}
		if (!com.apartment.util.Utils.isNullOrEmpty(contactApprovedMailText)) {
			contactApprovedMailText = contactApprovedMailText.replaceAll(
					"\\$MemberName", ufm.getFlat().getRegisteredPerson()
							.getName());
			contactApprovedMailText = contactApprovedMailText.replaceAll(
					"\\$Name", ufm.getFlat().getRegisteredPerson().getName());
			contactApprovedMailText = contactApprovedMailText.replaceAll(
					"\\$Username", ufm.getUser().getUserName());
			contactApprovedMailText = contactApprovedMailText.replaceAll(
					"\\$Society", ufm.getFlat().getApartment().getName());
			contactApprovedMailText = contactApprovedMailText.replaceAll(
					"\\$Groupz", ufm.getFlat().getApartment().getName());

			if (ufm.getUser().getPassword() != null
					&& !ufm.getFlat().isContact()) {
				contactApprovedMailText = contactApprovedMailText.replaceAll(
						"\\$Password", ufm.getUser().getPassword());
			}
			User usrObj = DBOperations.getAdminUser();
			UserFlatMapping ufmObj = null;
			emsManger.sendEmailAndSms(contactApprovedMailTitle,
					contactApprovedMailText, approvedSMSText,
					usrObj.getUserName(), usrObj.getEmail(), "", ufmObj, ufm);
		}
	}

	// save it in queue
	public void saveInQueue(String name, String mobile, String gender,
			String prefEmail, String password, String usrName, String block,
			String type, String flatNo, String propData, String contComments,
			RegistrationInvitation regInv, ApartmentSettings aptSettings,
			boolean userPresent, User userExists,String landline,String siteSize) {
		Person person = new Person();
		User newUsr = new User();
		Flat flat = new Flat();
		person.setName(name);
		person.setMobile(mobile);
		person.setGender(gender);
		//newly added for landline
		if(landline!=null && landline.isEmpty()==false && landline.equalsIgnoreCase("")==false){
		person.setPhone(landline);
		}
		person.setRelationship("Myself");
		if (userPresent == false) {
			newUsr.setAdmin(false);
			newUsr.setEmail(preferredEmail);
			newUsr.setEnabled(true);
			newUsr.setPassword(mobileNumber);
			newUsr.setUserName(userName);
		}
		flat.setBlockStreetDetails(block);
		flat.setSubDivision(type);
		if (regInv.isContact() == false) {
			flat.setDoorNo(flatNo);
		}
		flat.setProprietaryData(proprietaryData);
		if (isEmpty(contactComments) == true) {
			flat.setContactComments(contactComments);
		}
		flat.setApartment(regInv.getApartment());
		flat.setContact(regInv.isContact());
		flat.setStartDate(new Date());
		flat.setSiteSize(Integer.parseInt(siteSize));
		TemporaryDBObject tempObject = new TemporaryDBObject();
		tempObject.setObjectType(TemporaryDBObject.USER_REGISTRATION);
		tempObject.setApartment(regInv.getApartment());
		tempObject.addObjectForPersistence(person);
		if (userPresent == false) {
			tempObject.addObjectForPersistence(newUsr);
		} else {
			tempObject.addObjectForPersistence(userExists);
		}
		tempObject.addObjectForPersistence(flat);
		if (aptSettings != null){
			if(aptSettings.getDefaultRole()!=null){
			tempObject.addObjectForPersistence(aptSettings.getDefaultRole());
			}
		}
		tempObject.save();
		person = new Person();
		flat = new Flat();
		newUsr = new User();
		groupzsyncResponse = processSucess(false, "pending approval");
	}
}
