package org.elasticsearch.index.analysis;

import org.elasticsearch.ElasticsearchIllegalArgumentException;

/**
 * Joe Linn
 * 9/26/2015
 */
public enum EmailPart {
    DOMAIN,
    LOCALPART,
    WHOLE;

    public static EmailPart fromString(final String part) {
        for (EmailPart emailPart : EmailPart.values()) {
            if (emailPart.name().equalsIgnoreCase(part)) {
                return emailPart;
            }
        }
        throw new ElasticsearchIllegalArgumentException("Unrecognized email part: " + part);
    }
}
