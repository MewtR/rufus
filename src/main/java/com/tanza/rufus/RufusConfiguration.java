package com.tanza.rufus;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.cache.CacheBuilderSpec;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class RufusConfiguration extends Configuration {

    @Valid
    @NotNull
    @JsonProperty("database1")
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty("database2")
    private DataSourceFactory dataSourceFactory1 = new DataSourceFactory();

    @JsonProperty
    private CacheBuilderSpec authenticationCachePolicy;

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public DataSourceFactory getDataSourceFactory1() {
        return dataSourceFactory1;
    }

    public CacheBuilderSpec getAuthenticationCachePolicy() {
        return authenticationCachePolicy;
    }
    
}
