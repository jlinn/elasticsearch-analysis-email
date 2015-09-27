package org.elasticsearch.index.analysis;

import org.elasticsearch.index.analysis.email.EmailTokenizer;

/**
 * Joe Linn
 * 9/26/2015
 */
public class EmailTokenAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {
    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer(EmailTokenizer.NAME, EmailTokenizerFactory.class);
    }
}
