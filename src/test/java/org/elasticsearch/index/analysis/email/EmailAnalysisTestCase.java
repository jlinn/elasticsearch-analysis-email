package org.elasticsearch.index.analysis.email;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.test.ElasticsearchSingleNodeTest;
import org.junit.Before;

import java.util.List;

/**
 * Joe Linn
 * 9/26/2015
 */
public abstract class EmailAnalysisTestCase extends ElasticsearchSingleNodeTest {
    protected static final String INDEX = "email_token_filter";

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String settings = Streams.copyToStringFromClasspath("/test-settings.json");
        String mapping = Streams.copyToStringFromClasspath("/test-mapping.json");
        client().admin().indices().prepareCreate(INDEX).setSettings(settings).addMapping("test", mapping).get();
        refresh();
        Thread.sleep(75);   // Ensure that the shard is available before we start making analyze requests.
    }

    protected void refresh() {
        client().admin().indices().prepareRefresh().get();
    }


    protected List<AnalyzeResponse.AnalyzeToken> analyzeEmail(String email, String analyzer) {
        return client()
                .admin()
                .indices()
                .prepareAnalyze(INDEX, email)
                .setAnalyzer(analyzer)
                .get()
                .getTokens();
    }
}
