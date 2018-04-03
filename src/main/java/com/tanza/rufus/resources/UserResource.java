package com.tanza.rufus.resources;

import com.tanza.rufus.auth.AuthUtils;
import com.tanza.rufus.auth.BasicAuthenticator;
import com.tanza.rufus.auth.TokenGenerator;
import com.tanza.rufus.core.NewUser;
import com.tanza.rufus.core.User;
import com.tanza.rufus.core.UserUtils;
import com.tanza.rufus.db.ArticleDao;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.feed.FeedConstants;
import com.tanza.rufus.jobs.ConsistencyCheckerUsers;

import io.dropwizard.auth.Auth;
import org.apache.commons.collections.CollectionUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/user")
public class UserResource {

	private final BasicAuthenticator authenticator;
	private final TokenGenerator tokenGenerator;
	private final UserDao userDao;
	private final UserDao userDaoNewDb;
	private final ArticleDao articleDao;

	public UserResource(
			BasicAuthenticator authenticator,
			TokenGenerator tokenGenerator,
			UserDao userDao,
			UserDao userDaoNewDb,
			ArticleDao articleDao
			) {
		this.authenticator = authenticator;
		this.tokenGenerator = tokenGenerator;
		this.userDao = userDao;
		this.userDaoNewDb=userDaoNewDb;
		this.articleDao = articleDao;
	}

	@Path("/login")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@POST
	public Response login(@FormDataParam("email") String email, @FormDataParam("password") String password) {
		if (!UserUtils.valid(email, password)) {
			return ResourceUtils.badRequest("Email or password empty or invalid.");
		}
		Optional<User> optional = authenticator.authenticate(email, password);
		if (optional.isPresent()) {
			//generate and return jwt token to client
			return Response.ok(tokenGenerator.generateToken(email)).build();
		} else {
			return ResourceUtils.badRequest("Wrong email or password.");
		}
	}

	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@POST
	public Response newUser(NewUser newUser) {
		String email = newUser.getEmail();
		String pw = newUser.getPassword();

		if (!UserUtils.valid(email, pw)) {
			return ResourceUtils.badRequest("username /email empty or invalid");
		} else if (userDao.findByEmail(email) != null) {
			return ResourceUtils.badRequest("A user with that email address already exists");
		}

		userDao.addUser(new User(email, AuthUtils.hashPassword(pw)));
		User user = userDao.findByEmail(email);

		//Shadow writing new user to new data storage
		userDaoNewDb.addUser(new User(email, AuthUtils.hashPassword(pw)));
		//Instantly checking if databases consistent
		ConsistencyCheckerUsers.consistencyCheckerShadowWrites(user,userDao, userDaoNewDb);

		List<String> starterFeeds = newUser.getStarterFeeds();
		long userId = user.getId();
		if (CollectionUtils.isNotEmpty(starterFeeds) && UserUtils.validStarterFeeds(starterFeeds)) {
			for (String feed : starterFeeds) {
				articleDao.addFrontpageSource(
						userId,
						FeedConstants.STARTER_FEEDS.get(feed)
						);
			}
		}

		return Response.ok(tokenGenerator.generateToken(email)).build();
	}

	@Path("/deleteUser")
	@DELETE
	public Response deleteUser(@Auth User user) {
		userDao.deleteUser(user.getEmail());
		return Response.ok().build();
	}
}