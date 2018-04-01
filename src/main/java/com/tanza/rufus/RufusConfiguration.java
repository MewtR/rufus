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
    private DataSourceFactory dataSourceFactory1 = new DataSourceFactory();

    @Valid
    @NotNull
    @JsonProperty("database2")
    private DataSourceFactory dataSourceFactory2 = new DataSourceFactory();

    @JsonProperty
    private CacheBuilderSpec authenticationCachePolicy;

    public DataSourceFactory getDataSourceFactory1() {
        return dataSourceFactory1;
    }

    public DataSourceFactory getDataSourceFactory2() {
        return dataSourceFactory2;
    }

    public CacheBuilderSpec getAuthenticationCachePolicy() {
        return authenticationCachePolicy;
    }
    
}
