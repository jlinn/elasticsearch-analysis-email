package org.elasticsearch.index.analysis;

import com.google.common.base.Strings;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.email.EmailTokenizer;
import org.elasticsearch.index.settings.IndexSettings;

/**
 * Joe Linn
 * 9/26/2015
 */
public class EmailTokenizerFactory extends AbstractTokenizerFactory {
    private EmailPart part;
    private boolean tokenizeDomain;
    private boolean splitOnPlus;
    private boolean allowMalformed;
    private String[] splitLocalpart;

    @Inject
    public EmailTokenizerFactory(Index index, @IndexSettings Settings indexSettings, @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettings, name, settings);

        String partString = settings.get("part");
        if (!Strings.isNullOrEmpty(partString)) {
            this.part = EmailPart.fromString(partString);
        }
        this.tokenizeDomain = settings.getAsBoolean("tokenize_domain", true);
        this.splitOnPlus = settings.getAsBoolean("split_on_plus", true);
        this.allowMalformed = settings.getAsBoolean("allow_malformed", false);
        this.splitLocalpart = settings.getAsArray("split_localpart", null);
    }

    @Override
    public Tokenizer create() {
        EmailTokenizer tokenizer = new EmailTokenizer();
        tokenizer.setPart(part);
        tokenizer.setTokenizeDomian(tokenizeDomain);
        tokenizer.setSplitOnPlus(splitOnPlus);
        tokenizer.setAllowMalformed(allowMalformed);
        tokenizer.setSplitLocalpart(splitLocalpart);
        return tokenizer;
    }
}
