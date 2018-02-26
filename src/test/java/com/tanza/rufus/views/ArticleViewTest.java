package com.tanza.rufus.views;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import com.tanza.rufus.api.Article;

import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;

public class ArticleViewTest {

	@Test
	public void ArticleView() {
		//Breaking dependency between ArticleView and Article
		
		//First create an Article mock
		Article mockArticle=mock(Article.class);
		
		//Instantiate a List of articles
		List<Article> collectionArticles= new ArrayList<Article>();
		
		//Add our mock article to our list
		collectionArticles.add(mockArticle);
		
		//Instantiate articleView with a collection of mock articles
		ArticleView articleView=ArticleView.ofArticles(collectionArticles);
		
		//mockArticle returns this string when printed out
		when(mockArticle.toString()).thenReturn("This is a mock article");
		
		//get collection of articles from articleView
		Collection<Article> retrievedArticles = articleView.getArticles();
		
		//Test that the articleView now uses the collection of mock articles
		assertTrue(retrievedArticles.contains(mockArticle));
		assertTrue(retrievedArticles.toString().equals("[This is a mock article]"));
	
	}

}
