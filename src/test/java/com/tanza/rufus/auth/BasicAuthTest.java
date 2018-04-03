package com.tanza.rufus.auth;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;

public class BasicAuthTest {

	
	@Test
	public void testAuthenticate() {
		
		//create mock db interface
		UserDao MockInterface = mock(UserDao.class);
		
		//create mock user
		User Gio = mock(User.class);
		
		//when we pass this email return user "gio"
		when(MockInterface.findByEmail("gio@gmail.com")).thenReturn(Gio);
		
		//when AuthUtils calls getPassword, return the hashed password for "giveUsA"
		when(Gio.getPassword()).thenReturn(BCrypt.hashpw("giveUsA", BCrypt.gensalt()));
		
		//create a an authenticator
		BasicAuthenticator authenticator = new BasicAuthenticator(MockInterface, MockInterface);
		
		//check to see if user Gio is initialized
		Optional<User> u = authenticator.authenticate("gio@gmail.com", "giveUsA");
		
		//test if Gio was authenticated
		assertTrue(u.isPresent());
		
	}

}
