package com.tanza.rufus.feed;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jtanza
 */
public class FeedUtilsTest {

    @Test
    public void testHtml() {
        String someHtmlArticleDescription = "<h1> Today in the news </h1> <p> something or another </p>";
        Assert.assertFalse(FeedUtils.clean(someHtmlArticleDescription).contains("<"));
    }

    @Test
    public void testHtml2() {
        String DifferentCase1 = "<h1>  </h1> <br> </br>";
        Assert.assertFalse(FeedUtils.clean(DifferentCase1).contains("<"));
    }



}
