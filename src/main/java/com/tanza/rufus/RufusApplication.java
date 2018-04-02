package com.tanza.rufus;

import com.tanza.rufus.auth.BasicAuthenticator;
import com.tanza.rufus.auth.JwtAuthenticator;
import com.tanza.rufus.auth.TokenGenerator;
import com.tanza.rufus.core.User;
import com.tanza.rufus.db.ArticleDao;
import com.tanza.rufus.db.UserDao;
import com.tanza.rufus.feed.FeedParser;
import com.tanza.rufus.feed.FeedProcessorImpl;
import com.tanza.rufus.feed.FeedUtils;
import com.tanza.rufus.jobs.UserForklift;
import com.tanza.rufus.jobs.ConsistencyCheckerUsers;
import com.tanza.rufus.resources.ArticleResource;
import com.tanza.rufus.resources.UserResource;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

import com.github.toastshaman.dropwizard.auth.jwt.CachingJwtAuthenticator;
import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;

import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.util.List;

import org.skife.jdbi.v2.DBI;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class RufusApplication extends Application<RufusConfiguration> {
    private static final byte[] VERIFICATION_KEY = FeedUtils.getVerificationKey();
    private static final String DB_SOURCE = "h2";
    private static final String DB_SOURCE2 = "hsqldb";
    private static final String BEARER = "bearer";
    private static final String REALM = "realm";
    private static final String ROOT_PATH = "/api/*";

    public static void main(String[] args) throws Exception {
        new RufusApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<RufusConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/app", "/", "index.html"));
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new MigrationsBundle<RufusConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(RufusConfiguration conf) {
                return conf.getDataSourceFactory1();
            }

            @Override
            public String name() {
                return "db1";
        }
        });

        bootstrap.addBundle(new MigrationsBundle<RufusConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(RufusConfiguration conf) {
                return conf.getDataSourceFactory2();
            }

            @Override
            public String name() {
                return "db2";
        }
        });
    }

    @Override
    public void run(RufusConfiguration conf, Environment env) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(env, conf.getDataSourceFactory1(), DB_SOURCE);
        final DBI jdbi2 = factory.build(env, conf.getDataSourceFactory2(), DB_SOURCE2);

        final UserDao userDao = jdbi.onDemand(UserDao.class);
        final ArticleDao articleDao = jdbi.onDemand(ArticleDao.class);

        final FeedProcessorImpl processor = FeedProcessorImpl.newInstance(articleDao);
        final FeedParser parser = new FeedParser(articleDao, processor);

        final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setAllowedClockSkewInSeconds(30)
            .setRequireExpirationTime()
            .setRequireSubject()
            .setVerificationKey(new HmacKey(VERIFICATION_KEY))
            .setRelaxVerificationKeyValidation()
            .build();
        final CachingJwtAuthenticator<User> cachingJwtAuthenticator = new CachingJwtAuthenticator<>(
            env.metrics(),
            new JwtAuthenticator(userDao),
            conf.getAuthenticationCachePolicy()
        );

        env.jersey().register(new ArticleResource(userDao, articleDao, processor, parser));
        env.jersey().register(
            new UserResource(
                new BasicAuthenticator(userDao),
                new TokenGenerator(VERIFICATION_KEY),
                userDao,
                articleDao
            )
        );

        //route source
        env.jersey().setUrlPattern(ROOT_PATH);

        env.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
        env.jersey().register(new AuthDynamicFeature(
            new JwtAuthFilter.Builder<User>()
                .setJwtConsumer(jwtConsumer)
                .setRealm(REALM)
                .setPrefix(BEARER)
                .setAuthenticator(cachingJwtAuthenticator)
                .buildAuthFilter()
        ));
        this.runUserForklift(jdbi, jdbi2);
        this.runUserConsistencyChecker(jdbi, jdbi2);
    }

    public void runUserForklift(DBI h2jdbi, DBI hsqldbjdbi) throws Exception {
    
        try{
    Scheduler sched = StdSchedulerFactory.getDefaultScheduler();

    sched.start();

    JobDetail userForklift = newJob(UserForklift.class).withIdentity("userForklift", "group1").build();

    SimpleTrigger userFTrigger = newTrigger().withIdentity("userFTrigger", "group1").startNow()
        .withSchedule(simpleSchedule().withIntervalInSeconds(60).repeatForever()).build();

    
    userForklift.getJobDataMap().put(UserForklift.H2DB, h2jdbi);
    userForklift.getJobDataMap().put(UserForklift.HSQLDB, hsqldbjdbi);

    sched.scheduleJob(userForklift, userFTrigger);
    Thread.yield();


        }catch (SchedulerException se) {
              se.printStackTrace();
          }

    }

    public void runUserConsistencyChecker(DBI h2jdbi, DBI hsqldbjdbi) throws Exception {
    
        try{
    Scheduler sched = StdSchedulerFactory.getDefaultScheduler();

    sched.start();

    JobDetail ccUsers = newJob(ConsistencyCheckerUsers.class).withIdentity("ccUsers", "group2").build();

    SimpleTrigger ccUsersTrigger = newTrigger().withIdentity("ccUsersTrigger", "group2").startNow()
        .withSchedule(simpleSchedule().withIntervalInSeconds(10).repeatForever()).build();

    
    ccUsers.getJobDataMap().put(ConsistencyCheckerUsers.H2DB, h2jdbi);
    ccUsers.getJobDataMap().put(ConsistencyCheckerUsers.HSQLDB, hsqldbjdbi);

    sched.scheduleJob(ccUsers, ccUsersTrigger);
    Thread.yield();


        }catch (SchedulerException se) {
              se.printStackTrace();
          }

    }


}
