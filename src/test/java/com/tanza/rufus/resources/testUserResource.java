package com.tanza.rufus.resources;

import static org.junit.Assert.*;

import org.junit.Test;

import static org.mockito.Mockito.*;

import javax.ws.rs.core.Response;

import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.db.ArticleDao;
import com.tanza.rufus.auth.TokenGenerator;
import com.tanza.rufus.auth.BasicAuthenticator;
import com.tanza.rufus.core.User;

public class testUserResource {

	@Test
	public void testDeleteUser() {
	    //create the mock BasicAuthenticator
		BasicAuthenticator mockBA = mock(BasicAuthenticator.class);
		
		//create the mock TokenGenerator
		TokenGenerator mockToken = mock(TokenGenerator.class);
		
		//create the mock Database interface
		UserDao mockUserDao = mock(UserDao.class);
		
		//create the mock articleDao
		ArticleDao mockArticle = mock(ArticleDao.class);
		
		//create the mock user
		User user = mock(User.class);
		
		when(user.getEmail()).thenReturn("testing@gmail.com");
		
		//call the class under test
		UserResource userRes = new UserResource(mockBA,mockToken,mockUserDao, mockUserDao,mockArticle);
		
		userRes.deleteUser(user);
		
		verify(mockUserDao).deleteUser("testing@gmail.com");
		
		assertEquals(Response.ok().build().toString(),userRes.deleteUser(user).toString());
			
	}

}
