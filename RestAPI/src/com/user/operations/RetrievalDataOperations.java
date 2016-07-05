package com.user.operations;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.SQLQuery;

import com.apartment.database.DatabaseSession;
import com.apartment.database.DatabaseSessionFactory;
import com.apartment.database.operations.DBOperations;
import com.apartment.database.tables.Announcement;
import com.apartment.database.tables.Apartment;
import java.util.Date;

public class RetrievalDataOperations {
	private static DatabaseSessionFactory sessionFactory = DatabaseSessionFactory
			.getInstance();
	
	public static List<Integer> getRecordedMessages(Apartment apartment,List<String> userXmlQueries,String paginationQry,String expirationQry) { 
		StringBuffer queryBuffer = new StringBuffer();
		Date d1 = new Date();
		System.out.println("Start Time : " + d1);
		queryBuffer.append("anmntA.ApartmentId=" + apartment.getId());

		if (userXmlQueries.size() > 0) {
			queryBuffer.append(" and (");
			for (int i = 0; i < userXmlQueries.size(); i++) {
				String query = userXmlQueries.get(i);
				if (i != 0)
					queryBuffer.append(" or ");
				queryBuffer.append("(anmntA.usergroups LIKE '%" + query + "%')");
			}
			queryBuffer.append(")");
		}
		

		DatabaseSession session = sessionFactory.newDatabaseSession();
		String annSQL = "select anmntA.rootannouncementid from announcement anmntA , announcement anmntB where anmntA.rootannouncementid = anmntB.id and anmntB.promotion = false and anmntB.media=true and "
						+ queryBuffer.toString()
						+ " and anmntB.approval=true and anmntB.approvaldate is not null and anmntB.disable=false "+ expirationQry +" order by anmntB.PostedDate DESC" + paginationQry;

		/*SQLQuery sqlQuery = session
				.createSQLQuery("select {anmntA.rootannouncementid} from announcement anmntA , announcement anmntB where anmntA.rootannouncementid = anmntB.id and anmntB.promotion = false and anmntB.media=true and "
						+ queryBuffer.toString()
						+ " and anmntB.approval=true and anmntB.approvaldate is not null and anmntB.disable=false "+ expirationQry +" order by anmntB.PostedDate DESC" 
						+  paginationQry);*/
		/*SQLQuery sqlQuery = session
				.createSQLQuery("select {anmntA.RootAnnouncementId} from announcement anmntA , announcement anmntB where anmntA.id = anmntB.rootannouncementid and "
						+ queryBuffer.toString()						
						 );*/
		/*SQLQuery sqlQuery = session.createSQLQuery(annSQL);
		List<Announcement> list=null;
		if (sqlQuery != null) {
			 list=  (List<Announcement>) sqlQuery.list();
			// System.out.println("Returning  follow count list Size : " +
			// list.size());
			session.close();
		}*/
		System.out
				.println("Announcement query for user:" + annSQL);
		List<Integer> list=null;
		try{
			SQLQuery results = session.createSQLQuery(annSQL);
			 list = (List<Integer>) results.list();
			session.close();
		///sqlQuery.addEntity("anmntA", Announcement.class);		
		//list = (List<Integer>) sqlQuery.list();
		//session.close();
		System.out.println("Announcement list before iterating:"+list.size());
		}catch(Exception e){
			e.printStackTrace();
		}
		/*List<Announcement> finallist = new ArrayList<Announcement>();
		/*if(list!=null && list.isEmpty()==false){
			System.out.println("Announcement list after iterating:"+list.size());
		for(Announcement a:list){
			String qry= expirationQry + " order by enddate DESC ";
			System.out.println("Qry:"+qry);
		Announcement ann = (Announcement) DBOperations.getSingleDatabaseObject(Announcement.class, "rootannouncementid= "+a.getRootAnnouncement().getId());		
		if(ann.isPromotion()==false && ann.isMedia()==true && ann.isDisable()==false && ann.isApproval()==true && ann.getApprovalDate()!=null){		
			Announcement finalAnn = (Announcement) DBOperations.getSingleDatabaseObject(Announcement.class, "rootannouncementid="+ann.getRootAnnouncement().getId()+qry);
			if(finalAnn!=null){
			finallist.add(finalAnn);
			System.out.println("Value id:"+finalAnn.getId()+"Adding size:"+finallist.size());
			}
		}
		}	
		}		
		System.out.println("Announcement list final:"+finallist.size());
		Date d2 = new Date();
		System.out.println("End Time : " + d2);
		System.out.println("Processing Time : " + (d2.getTime() - d1.getTime())
				/ 1000 + " Secs");
		return finallist;	*/	
		return list;
	}
}
