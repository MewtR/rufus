package com.tanza.rufus.db;

import java.util.ArrayList;
import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.skife.jdbi.v2.DBI;

import com.tanza.rufus.core.User;

public class ConsistencyCheckerUsers {
	public static final String H2DB = "h2db";
	public static final String HSQLDB = "hsqldb";

	private List<User> usersNewDb=null;

	public ConsistencyCheckerUsers() {

	}

	public int execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Consistency check start");
		JobDataMap data = context.getJobDetail().getJobDataMap();

		int inconsistencies=0;

		DBI h2jdbi = (DBI) data.get(H2DB);
		DBI hsqldbjdbi = (DBI) data.get(HSQLDB);

		UserDao userDao = h2jdbi.open(UserDao.class);

		List<User> usersOldDb = userDao.getAll();

		if(this.usersNewDb==null)
		{
			UserDao userDao2 = hsqldbjdbi.open(UserDao.class);
			this.usersNewDb = userDao2.getAll();
			userDao2.close();
		}

		if (!usersOldDb.isEmpty()){
			for (User user : usersOldDb){
				if(!this.usersNewDb.contains(user))
					inconsistencies++;
			}
			userDao.close();
		}
		if(inconsistencies!=0){
			this.usersNewDb = new ArrayList<User>(usersOldDb);
		}
		
		return inconsistencies;

	}
}

