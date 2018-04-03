package com.tanza.rufus.auth;

import com.tanza.rufus.core.User;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.jobs.ConsistencyCheckerUsers;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;

import java.util.Optional;

/**
 * @author jtanza
 */
public class JwtAuthenticator implements Authenticator<JwtContext, User> {

    private final UserDao userDao;
    private final UserDao userDaoNewDb;

    public JwtAuthenticator(UserDao userDao,UserDao userDaoNewDb) {
        this.userDao = userDao;
        this.userDaoNewDb=userDaoNewDb;
    }

    /**
     * Validates an existing {@link User} once their corresponding JWT's signature has been verified.
     * i.e. this method is called after signature verification.
     *
     * //TODO potentially blacklist tokens here until their expiry if a user has
     * //TODO logged out &or changed her password.
     *
     * @param jwtContext
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Optional<User> authenticate(JwtContext jwtContext) throws AuthenticationException {
        try {
            if (TokenGenerator.isExpired(jwtContext)) {
                return Optional.empty();
            }
            User u = userDao.findByEmail(jwtContext.getJwtClaims().getSubject());
            User u2 = userDaoNewDb.findByEmail(jwtContext.getJwtClaims().getSubject());
            boolean consistency=ConsistencyCheckerUsers.consistencyCheckerShadowReads(u,u2);
            if(!consistency)
            	System.out.println("Not consistent shadow read!!");
            
            return u != null ? Optional.of(u) : Optional.empty();
        } catch (MalformedClaimException e) {
            return Optional.empty();
        }
    }
}
