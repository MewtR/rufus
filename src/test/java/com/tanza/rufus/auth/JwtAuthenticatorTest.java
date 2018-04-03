package com.tanza.rufus.auth;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;

import io.dropwizard.auth.AuthenticationException;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.JwtContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.util.Optional;

import static org.junit.Assert.*;

public class JwtAuthenticatorTest {

    private JwtAuthenticator jwtAuthenticator;

    @Before
    public void init() throws MalformedURLException {
        UserDao userDao = Mockito.mock(UserDao.class);
        UserDao userDao2 = Mockito.mock(UserDao.class);
        Mockito.when(userDao.findByEmail(ArgumentMatchers.anyString())).thenReturn(new User());
        jwtAuthenticator = new JwtAuthenticator(userDao, userDao2);
    }

    @Test
    public void testAuthenticateHappyPath() throws AuthenticationException, MalformedClaimException {


        JwtContext jwtContext = Mockito.mock(JwtContext.class);
        //Mock dependencies of the TokenGenerator.isExpired() method
        JwtClaims jwtClaims = Mockito.mock(JwtClaims.class);
        NumericDate numericDate = Mockito.mock(NumericDate.class);
        
		Mockito.when(jwtClaims.getExpirationTime()).thenReturn(numericDate);
		Mockito.when(jwtClaims.getSubject()).thenReturn("user@email.com");
		Mockito.when(numericDate.isBefore(NumericDate.now())).thenReturn(false);
		Mockito.doReturn(jwtClaims).when(jwtContext).getJwtClaims();

        Optional<User> u = jwtAuthenticator.authenticate2(jwtContext);
        
        Mockito.verify(jwtClaims).getExpirationTime();
        Mockito.verify(jwtContext, Mockito.times(2)).getJwtClaims();
        //Mockito.verify(numericDate).isBefore(NumericDate.now());
        Mockito.verify(jwtClaims).getSubject();
        
        assertTrue(u.isPresent());
    }

}
