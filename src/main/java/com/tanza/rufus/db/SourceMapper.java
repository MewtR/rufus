package com.tanza.rufus.db;

import com.tanza.rufus.api.Source;

import org.apache.commons.lang3.StringUtils;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jtanza.
 */
public class SourceMapper implements ResultSetMapper<Source> {
    @Override
    public Source map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
        String resultSource = resultSet.getString("source");
        if (StringUtils.isEmpty(resultSource)) {
            return null;
        }

        Source source;
        try {
            source = new Source(new URL(resultSource));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        Array tags = resultSet.getArray("tags");
        List<String> asList = null;
        if (tags != null) {
            asList = Arrays.asList((String[]) tags.getArray());
        }

        source.setTags(asList);
        source.setFrontpage(resultSet.getBoolean("frontpage"));
        return source;
    }

}
