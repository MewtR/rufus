package com.tanza.rufus.jobs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.skife.jdbi.v2.DBI;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;

import static org.quartz.SimpleScheduleBuilder.*;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

public class ConsistencyCheckerUsers implements org.quartz.Job {
	public  static final String H2DB = "h2db";
	public  static final String HSQLDB = "hsqldb";
	private static int checkerCalls=0;
	private static int unsuccessfulChecks=0;

	public ConsistencyCheckerUsers() {

	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println("Consistency check for rufususer start");
		JobDataMap data = context.getJobDetail().getJobDataMap();
		int inconsistencies=0;
		DBI h2jdbi = (DBI) data.get(H2DB);
		DBI hsqldbjdbi = (DBI) data.get(HSQLDB);

		UserDao userDao = h2jdbi.open(UserDao.class);
		List<User> usersOldDb = userDao.getAll();
		List<String> usersOldDbHash = new ArrayList();

		UserDao userDao2 = hsqldbjdbi.open(UserDao.class);
		List<User> usersNewDb= userDao2.getAll();

		MessageDigest md=null;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(User user : usersOldDb){
			String fullInfo=user.getId()+user.getEmail()+user.getName();
			md.update(fullInfo.getBytes());
			byte[] digest = md.digest();
			String hashedInfo = DatatypeConverter.printHexBinary(digest).toUpperCase();
			usersOldDbHash.add(hashedInfo);
		}

		if (!usersNewDb.isEmpty()){
			for (User user : usersNewDb){
				//If the new database contains a record
				//not found in the old database (inconsistency) delete it from the old database then run forklift
				if(!usersOldDb.contains(user))
					userDao2.deleteUser(user.getEmail());
			}
			//refresh list
			usersNewDb = userDao2.getAll();
		}

		//Forklift
		if (!usersOldDb.isEmpty()){
			for (User user : usersOldDb){
				if(!usersNewDb.contains(user))
					userDao2.insertUser(user);
			}
		}

		userDao.close();
		userDao2.close();
	}

	public static int consistencyCheckerShadowWrites(User user,UserDao userDao, UserDao userDaoNewDb){
		System.out.println("Consistency Shadow Write check start");
		checkerCalls++;
		int inconsistencies=0;

		List<User> usersOldDb = userDao.getAll();
		List<User> usersNewDb = userDaoNewDb.getAll();

		if(!usersNewDb.contains(user)||!usersOldDb.contains(user))
			inconsistencies++;

		if(inconsistencies!=0)
			unsuccessfulChecks++;
		return inconsistencies;
	}
	public static boolean consistencyCheckerShadowReads(User oldDb, User newDb){
		checkerCalls++;
		boolean consistent;
		if(oldDb.equals(newDb)){
			consistent=true;
		}
		else{
			consistent=false;
		}

		if(!consistent)
			unsuccessfulChecks++;

		return consistent;
	}

	public static double getPassingRate(){
		double passingRate;
		
		if(checkerCalls==0){
			passingRate=0;
		}
		else{
			passingRate= (checkerCalls-unsuccessfulChecks)/checkerCalls;
		}
		
		return passingRate;
	}

}

