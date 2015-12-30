package org.elasticsearch.index.analysis.email;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.plugin.analysis.AnalysisEmailPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.StreamsUtils;
import org.junit.Before;

import java.util.Collection;
import java.util.List;

/**
 * Joe Linn
 * 9/26/2015
 */
public abstract class EmailAnalysisTestCase extends ESIntegTestCase {
    protected static final String INDEX = "email_token_filter";


    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return pluginList(AnalysisEmailPlugin.class);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        String settings = StreamsUtils.copyToStringFromClasspath("/test-settings.json");
        String mapping = StreamsUtils.copyToStringFromClasspath("/test-mapping.json");
        client().admin().indices().prepareCreate(INDEX).setSettings(settings).addMapping("test", mapping).get();
        refresh();
        Thread.sleep(75);   // Ensure that the shard is available before we start making analyze requests.
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
