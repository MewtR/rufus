package com.tanza.rufus.jobs;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.skife.jdbi.v2.DBI;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.db.ArticleDao;
import com.tanza.rufus.api.Source;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;

import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.quartz.SimpleScheduleBuilder.*;


public class SourceForklift implements org.quartz.Job {

	public static final String H2DB = "h2db";
	public static final String HSQLDB = "hsqldb";

      public SourceForklift() {
      }

      public void execute(JobExecutionContext context) throws JobExecutionException {

          System.out.println("Source forklift started");
    	JobDataMap data = context.getJobDetail().getJobDataMap();
        DBI h2jdbi = (DBI) data.get(H2DB);
        DBI hsqldbjdbi = (DBI) data.get(HSQLDB);

        UserDao userDao = h2jdbi.open(UserDao.class);
        List<User> usersOldDb = userDao.getAll();
        userDao.close();

        userDao = hsqldbjdbi.open(UserDao.class);
        List<User> usersNewDb = userDao.getAll();
        //Check there were users in the old database and
        //the same set of users are already in the new database
        if (!usersOldDb.isEmpty() && (usersOldDb.equals(usersNewDb))){
        ArticleDao sourcesDao = h2jdbi.open(ArticleDao.class);
        ArticleDao sourcesDao2 = hsqldbjdbi.open(ArticleDao.class);
        for (User u : usersOldDb){
            //Get the source of each user in old db
        List<Source> uSourcesOldDb = sourcesDao.getSources(u.getId());
        List<Source> uSourcesNewDb = sourcesDao2.getSources(u.getId());
            if ((!uSourcesOldDb.isEmpty()) && (uSourcesOldDb.get(0) != null)){
              for (Source s : uSourcesOldDb){
                  //Check that new db does not contain same source
                  //for the same user
                  if (!uSourcesNewDb.contains(s)){
                      //insert
                      if (s.isFrontpage()){
                      sourcesDao2.addFrontpageSource(u.getId(), s.getUrl().toString());
                      }else{
                      sourcesDao2.addSource(u.getId(), s.getUrl().toString());
                      }
                      //Deal with frontpage inconsistency if source is already in new db
                  }else {
                      if (s.isFrontpage())
                          sourcesDao2.setFront(u.getId(), s.getUrl().toString());
                      else
                          sourcesDao2.removeFront(u.getId(), s.getUrl().toString());
                  }

              }

            }

        }
        sourcesDao.close();
        sourcesDao2.close();
        }
        userDao.close();

      }
  }
