package com.tanza.rufus.jobs;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.skife.jdbi.v2.DBI;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.quartz.SimpleScheduleBuilder.*;


public class UserForklift implements org.quartz.Job {

	public static final String H2DB = "h2db";
	public static final String HSQLDB = "hsqldb";

      public UserForklift() {
      }

      public void execute(JobExecutionContext context) throws JobExecutionException {

          System.out.println("User forklift started");
    	JobDataMap data = context.getJobDetail().getJobDataMap();
        DBI h2jdbi = (DBI) data.get(H2DB);
        DBI hsqldbjdbi = (DBI) data.get(HSQLDB);

        UserDao userDao = h2jdbi.open(UserDao.class);
        List<User> usersOldDb = userDao.getAll();
        userDao.close();

        if (!usersOldDb.isEmpty()){
        userDao = hsqldbjdbi.open(UserDao.class);
        List<User> usersNewDb = userDao.getAll();
        List<Long> usersNewDbIds = userDao.getAllIds();
        for (User user : usersOldDb){
            if((!usersNewDb.contains(user)) && (!usersNewDbIds.contains(user.getId())))
            userDao.insertUser(user);
        }
        userDao.close();
        }

      }
  }
      
