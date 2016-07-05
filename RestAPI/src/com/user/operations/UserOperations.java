package com.user.operations;

import java.util.List;

import org.hibernate.SQLQuery;

import com.apartment.database.DatabaseSession;
import com.apartment.database.DatabaseSessionFactory;
import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Group;
import com.apartment.database.tables.UserFlatMapping;

public class UserOperations {
	private static DatabaseSessionFactory sessionFactory = DatabaseSessionFactory
			.getInstance();

	public static List<UserFlatMapping> getMembersListForGroupz(String query) {
		List<UserFlatMapping> membersList = null;
		DatabaseSession session = sessionFactory.newDatabaseSession();
		try {
			SQLQuery results = constructMembersQuery(query);
			System.out.println("Final SQL Qry:" + results.toString());
			membersList = (List<UserFlatMapping>) results.addEntity("userflatmapping",
					UserFlatMapping.class).list();
			System.out.println("Final Groupz List:" + membersList.size());
			session.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return membersList;
	}

	private static SQLQuery constructMembersQuery(String query) {
		DatabaseSession session = sessionFactory.newDatabaseSession();
		String finalQry = "select distinct userflatmapping.* from userflatmapping,flat,person" +
				" where userflatmapping.flatid=flat.id and flat.registeredpersonid=person.id and userflatmapping.enabled=true and "
				+ query + " order by userflatmapping.id";
		SQLQuery sqlQuery = session.createSQLQuery(finalQry);
		return sqlQuery;
	}
	
	public static List<UserFlatMapping> checkMobileNumberValidForGroupz(String query) {
		List<UserFlatMapping> membersList = null;
		DatabaseSession session = sessionFactory.newDatabaseSession();
		try {
			SQLQuery results = constructMobileNumberValidForGroupzQuery(query);
			System.out.println("Final SQL Qry:" + results.toString());
			membersList = (List<UserFlatMapping>) results.addEntity("userflatmapping",
					UserFlatMapping.class).list();
			System.out.println("Final Groupz List:" + membersList.size());
			session.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return membersList;
	}

	private static SQLQuery constructMobileNumberValidForGroupzQuery(String query) {
		DatabaseSession session = sessionFactory.newDatabaseSession();
		String finalQry = "select distinct userflatmapping.* from userflatmapping,flat,person,roledefinition" +
				" where userflatmapping.flatid=flat.id and flat.registeredpersonid=person.id and userflatmapping.enabled=true and " +
				"userflatmapping.roleid=roledefinition.id and "
				+ query + " order by userflatmapping.id";
		SQLQuery sqlQuery = session.createSQLQuery(finalQry);
		return sqlQuery;
	}
	
	// getting role group list	
	public static List<Group> getIVRGroupsList(String query) {		
		List<Group> roleGroupsList = (List<Group>) DBOperations.getDatabaseObjects(Group.class,query);
		return roleGroupsList;
	}
	
}
