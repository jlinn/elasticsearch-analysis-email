package org.elasticsearch.index.analysis.email;

import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.elasticsearch.index.analysis.EmailPart;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.elasticsearch.index.analysis.email.IsTokenizerWithTokenAndPosition.hasTokenAtOffset;

/**
 * Joe Linn
 * 9/26/2015
 */
public class EmailTokenizerTest extends BaseTokenStreamTestCase {
    @Test
    public void testTokenizeDomain() throws IOException {
        EmailTokenizer tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.DOMAIN);
        assertTokenStreamContents(tokenizer, stringArray("gmail.com", "com"));

        tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.DOMAIN);
        assertThat(tokenizer, hasTokenAtOffset("com", 14, 17));
        tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.DOMAIN);
        assertThat(tokenizer, hasTokenAtOffset("gmail.com", 8, 17));
    }


    @Test(expected = IOException.class)
    public void testMalformed() throws IOException {
        EmailTokenizer tokenizer = createTokenizer("foo@bar@com", null);
        assertTokenStreamContents(tokenizer, stringArray("gmail.com", "com"));
    }


    @Test
    public void testTokenizeLocalpart() throws IOException {
        EmailTokenizer tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.LOCALPART);
        assertTokenStreamContents(tokenizer, stringArray("foo+bar", "foo"));

        tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.LOCALPART);
        assertThat(tokenizer, hasTokenAtOffset("foo+bar", 0, 7));
        tokenizer = createTokenizer("foo+bar@gmail.com", EmailPart.LOCALPART);
        assertThat(tokenizer, hasTokenAtOffset("foo", 0, 3));
    }


    @Test
    public void testSplitLocalpart() throws IOException {
        EmailTokenizer tokenizer = createTokenizer("foo.bar_baz@gmail.com", EmailPart.LOCALPART);
        tokenizer.setSplitLocalpart(new String[]{".", "_"});
        assertTokenStreamContents(tokenizer, stringArray("foo.bar_baz", "foo", "bar_baz", "foo.bar", "baz"));

        tokenizer = createTokenizer("foo.bar_baz@gmail.com", EmailPart.LOCALPART);
        tokenizer.setSplitLocalpart(new String[]{".", "_"});
        assertThat(tokenizer, hasTokenAtOffset("bar_baz", 4, 11));

        tokenizer = createTokenizer("foo.bar_baz@gmail.com", EmailPart.LOCALPART);
        tokenizer.setSplitLocalpart(new String[]{".", "_"});
        assertThat(tokenizer, hasTokenAtOffset("baz", 8, 11));
    }


    private EmailTokenizer createTokenizer(String input, EmailPart part) {
        return new EmailTokenizer(new StringReader(input), part);
    }

    private String[] stringArray(String... strings) {
        return strings;
    }
}