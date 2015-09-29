package org.elasticsearch.index.analysis.email;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;

/**
 * Joe Linn
 * 9/26/2015
 */
public class EmailTokenizerIntegrationTest extends EmailAnalysisTestCase {
    @Test
    public void testAnalyze() {
        assertTokensContain("foo+bar@email.com", "email_domain", "email.com", "com");

        assertTokensContain("foo+bar@email.com", "email_localpart", "foo", "foo+bar");

        assertTokensContain("foo+bar@email.com", "email_all", "foo", "foo+bar", "foo@email.com", "foo+bar@email.com", "email.com", "com");

        assertTokensContain("foo+bar@email@com", "email_all", "foo+bar@email@com");
    }


    @Test
    public void testSearch() {
        client().prepareIndex(INDEX, "test", "1").setSource("email", "foo+bar-baz@email.com").get();
        client().prepareIndex(INDEX, "test", "2").setSource("email", "foo+barbaz@email.net").get();
        client().prepareIndex(INDEX, "test", "3").setSource("email", "foo+bar_baz@a.email.net").get();

        refresh();

        SearchHits hits = client().prepareSearch(INDEX).setQuery(QueryBuilders.queryStringQuery("email.domain:email.*")).get().getHits();
        assertThat(hits.getHits().length, equalTo(3));

        hits = client().prepareSearch(INDEX).setQuery(QueryBuilders.queryStringQuery("email.domain:*.email.*")).get().getHits();
        assertThat(hits.getHits().length, equalTo(1));

        hits = client().prepareSearch(INDEX).setQuery(QueryBuilders.termQuery("email.localpart", "baz")).get().getHits();
        assertThat(hits.getHits().length, equalTo(2));
    }


    private List<AnalyzeResponse.AnalyzeToken> assertTokensContain(String email, String analyzer, String... expected) {
        List<AnalyzeResponse.AnalyzeToken> tokens = analyzeEmail(email, analyzer);
        for (String e : expected) {
            assertThat(tokens, hasItem(Matchers.<AnalyzeResponse.AnalyzeToken>hasProperty("term", equalTo(e))));
        }
        return tokens;
    }
}
