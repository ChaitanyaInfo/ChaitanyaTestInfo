package com.user.operations;

import java.util.List;

import com.apartment.database.DatabaseSession;
import com.apartment.database.DatabaseSessionFactory;
import com.apartment.database.tables.UserFlatMapping;

public class SROperations {

	private static DatabaseSessionFactory sessionFactory = DatabaseSessionFactory
			.getInstance();

	@SuppressWarnings("unchecked")
	public static List<UserFlatMapping> getValiduserForGroupz(String accountId,
			String memberId) {
		DatabaseSession session = sessionFactory.newDatabaseSession();

		String qry = " select ufm.*  from userflatmapping ufm , flat flt , user us where ufm.flatid = flt.id and ufm.userid = us.id  and ufm.id = "
				+ memberId + " and flt.apartmentid = " + accountId;
		System.out.println(" Final Query to pass valid user :-> " + qry);

		List<UserFlatMapping> list = (List<UserFlatMapping>) session
				.createSQLQuery(qry).addEntity("ufm", UserFlatMapping.class)
				.list();
		
		System.out.println(" getValiduserForGroupz list size :->> " + list.size());
		session.close();
		
		return list;
	}

}
