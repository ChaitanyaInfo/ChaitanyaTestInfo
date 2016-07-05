package com.user.operations;

import java.util.List;

import org.hibernate.SQLQuery;

import com.apartment.database.DatabaseSession;
import com.apartment.database.DatabaseSessionFactory;
import com.apartment.database.tables.Apartment;
import com.apartment.database.tables.Group;

public class GroupzOperations {
	private static DatabaseSessionFactory sessionFactory = DatabaseSessionFactory
			.getInstance();

	public static List<Apartment> getGroupzListForGroupz(String query) {
		List<Apartment> groupzList = null;
		DatabaseSession session = sessionFactory.newDatabaseSession();
		try {			
			/*String finalQry = "select apartment.* from apartment,userflatmapping,flat,roledefinition,person where"
					+ " userflatmapping.flatid=flat.id and flat.registeredpersonid=person.id and apartment.id=flat.apartmentid "
					+ "and flat.apartmentid=roledefinition.societyid and "
					+ query;
			System.out.println("Final SQL Qry:" + finalQry);
			groupzList = session.createSQLQuery(finalQry).list();
			System.out.println("Final Group List:" + groupzList.size());
			session.close();*/
			SQLQuery results =constructGroupzQuery(query);
			System.out.println("Final SQL Qry:"+results.toString());
			groupzList = (List<Apartment>) results.addEntity("apartment",Apartment.class).list();
			System.out.println("Final Groupz List:"+groupzList.size());
			session.close();		
		} catch (Exception e) {
			// TODO: handle exception
		}
		return groupzList;
	}
	
	private static SQLQuery constructGroupzQuery(String query){		
		DatabaseSession session = sessionFactory.newDatabaseSession();	
		String finalQry = "select distinct apartment.* from apartment,userflatmapping,flat,roledefinition,person where"
		+ " userflatmapping.flatid=flat.id and flat.registeredpersonid=person.id and apartment.id=flat.apartmentid "
		+ "and flat.apartmentid=roledefinition.societyid and apartment.enabled=true and "
		+ query+ " order by apartment.id";	
		SQLQuery sqlQuery = session.createSQLQuery(finalQry);			
		return sqlQuery;
	}
}
