package com.tanza.rufus.resources;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.tanza.rufus.core.User;

public class testArticleResource {
	
	private ArticleResource articleResource;
	private User user;
	
	@Before
	public void init() {
		articleResource = Mockito.mock(ArticleResource.class);
		user = new User("guy@mail.com", "password");
	}

	@Test
	//Test the return response of an action: User Adding a Feed
	public void addFeedTest() {
		ArrayList<String> feeds = new ArrayList<String>();
		Mockito.when(articleResource.addFeed(user, feeds)).thenReturn(Response.ok("Successfully added : \n").build());
		assertEquals(Response.ok("Successfully added : \n").build().toString(), articleResource.addFeed(user,feeds).toString());
	}

}
