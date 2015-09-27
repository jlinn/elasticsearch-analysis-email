package org.elasticsearch.index.analysis.email;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
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


    private List<AnalyzeResponse.AnalyzeToken> assertTokensContain(String email, String analyzer, String... expected) {
        List<AnalyzeResponse.AnalyzeToken> tokens = analyzeEmail(email, analyzer);
        for (String e : expected) {
            assertThat(tokens, hasItem(Matchers.<AnalyzeResponse.AnalyzeToken>hasProperty("term", equalTo(e))));
        }
        return tokens;
    }
}
