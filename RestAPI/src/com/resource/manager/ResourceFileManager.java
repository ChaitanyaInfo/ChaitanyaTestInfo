package com.resource.manager;

import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.NoticeSettings;
import com.apartment.database.tables.Person;
import com.apartment.modules.noticemanagement.NoticeManagementManager;

public class ResourceFileManager {
	
	public boolean checkValidGroupzCode(String code) {
		boolean status = false;
		if (code != null && code.isEmpty() == false && code.length() > 0) {
			Apartment apt = DBOperations.getApartmentByCode(code);
			if (apt != null) {
			//	System.out.println("code valid");
				status = true;
			} else {
				//System.out.println("Code invalid");
				status = false;
			}		
		} else {
			//System.out.println("Code empty");
			status = false;
		}
		return status;
	}
	
	public byte[] getGroupzLogo(String code){
		byte[] image = null;
		if(code!=null && code.isEmpty()==false && code.length()>0){
			Apartment apt  = DBOperations.getApartmentByCode(code);
			if(apt!=null){
				NoticeSettings noticeSettings = NoticeManagementManager
						.getApartmetNoticeSettings(apt.getId());
				if (noticeSettings != null) {
					image = noticeSettings.getApartmentLogo();
				}
				/*if(apt.getApartmentImage()!=null){
			image = apt.getApartmentImage();
				}*/
			}
		}
		return image;
	}
	
	public boolean checkValidProfile(int id) {
		boolean status = false;
		if (id>0) {
			Person person = DBOperations.getPersonById(id);
			if (person != null) {
			//	System.out.println("code valid");
				status = true;
			} else {
				//System.out.println("Code invalid");
				status = false;
			}		
		} else {
			//System.out.println("Code empty");
			status = false;
		}
		return status;
	}
	

	public byte[] getProfilePhoto(int id){
		byte[] image = null;
		if (id>0) {
			Person person = DBOperations.getPersonById(id);
			if (person != null) {
				if(person.getPhoto()!=null){
			image = person.getPhoto();
				}
			}
		}
		return image;
	}

}
