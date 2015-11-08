package org.elasticsearch.index.analysis;


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
        throw new IllegalArgumentException("Unrecognized email part: " + part);
    }
}
