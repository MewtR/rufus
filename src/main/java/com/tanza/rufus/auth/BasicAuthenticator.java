package com.tanza.rufus.auth;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.jobs.ConsistencyCheckerUsers;
import com.tanza.rufus.resources.UserResource;

import java.util.Optional;


/**
 * Provides basic authentication against
 * requests to {@link UserResource#login(String, String)}.
 *
 * Created by jtanza.
 */
public class BasicAuthenticator {
	private final UserDao userDao;
	private final UserDao userDaoNewDb;
	private boolean switched=false;

	public BasicAuthenticator(UserDao userDao, UserDao userDaoNewDb) {
		this.userDao = userDao;
		this.userDaoNewDb=userDaoNewDb;
	}

	public Optional<User> authenticate(String email, String password) {
		User u = userDao.findByEmail(email);
		User u2 = userDaoNewDb.findByEmail(email);
		boolean consistency=ConsistencyCheckerUsers.consistencyCheckerShadowReads(u,u2);
		if(!consistency)
			System.out.println("Not consistent shadow read!!");

		//Checking if reached threshold to switch to new DB
		if(switched){
			return u2 != null && AuthUtils.isPassword(password, u2.getPassword()) ? Optional.of(u2) : Optional.empty();
		}
		else if(ConsistencyCheckerUsers.getPassingRate()>0.99){
			switched=true;
			return u2 != null && AuthUtils.isPassword(password, u2.getPassword()) ? Optional.of(u2) : Optional.empty();
		}
		else{
			return u != null && AuthUtils.isPassword(password, u.getPassword()) ? Optional.of(u) : Optional.empty();
		}

	}
}
