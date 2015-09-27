package org.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.index.analysis.EmailTokenAnalysisBinderProcessor;
import org.elasticsearch.plugins.AbstractPlugin;

/**
 * Joe Linn
 * 9/26/2015
 */
public class AnalysisEmailPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "analysis-email";
    }

    @Override
    public String description() {
        return "Email address tokenizer.";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new EmailTokenAnalysisBinderProcessor());
    }
}
