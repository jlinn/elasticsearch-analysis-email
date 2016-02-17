package org.elasticsearch.index.analysis.email;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.path.ReversePathHierarchyTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.common.base.Splitter;
import org.elasticsearch.common.base.Strings;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.lang3.StringUtils;
import org.elasticsearch.index.analysis.EmailPart;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Joe Linn
 * 9/26/2015
 */
public final class EmailTokenizer extends Tokenizer {
    public static final String NAME = "email";
    private static final Pattern VALIDATOR = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?", Pattern.CASE_INSENSITIVE);

    /**
     * If set, only the given part of the email will be tokenized.
     */
    private EmailPart part;

    /**
     * If true, the email's domain will be tokenized using a {@link ReversePathHierarchyTokenizer}
     */
    private boolean tokenizeDomian = true;

    /**
     * If true, the localpart will be split on '+', and the part prior to '+' and the whole localpart will both become tokens.
     */
    private boolean splitOnPlus = true;

    /**
     * If true, malformed email addresses will not be rejected, and will be tokenized as a single token.
     */
    private boolean allowMalformed;

    /**
     * If set, the localpart will be split on each of the strings in this array, and the resulting parts will all become tokens.
     */
    private String[] splitLocalpart;

    private final CharTermAttribute termAttribute = addAttribute(CharTermAttribute.class);
    private final TypeAttribute typeAttribute = addAttribute(TypeAttribute.class);
    private final OffsetAttribute offsetAttribute = addAttribute(OffsetAttribute.class);

    private List<Token> tokens;
    private Iterator<Token> iterator;

    public EmailTokenizer(Reader input) {
        super(input);
    }

    public EmailTokenizer(Reader input, EmailPart part) {
        this(input);
        this.part = part;
    }

    public EmailTokenizer(AttributeFactory factory, Reader input) {
        super(factory, input);
    }


    public EmailTokenizer setPart(EmailPart part) {
        this.part = part;
        return this;
    }

    public EmailTokenizer setTokenizeDomian(boolean tokenizeDomian) {
        this.tokenizeDomian = tokenizeDomian;
        return this;
    }

    public EmailTokenizer setSplitOnPlus(boolean splitOnPlus) {
        this.splitOnPlus = splitOnPlus;
        return this;
    }

    public EmailTokenizer setAllowMalformed(boolean allowMalformed) {
        this.allowMalformed = allowMalformed;
        return this;
    }

    public EmailTokenizer setSplitLocalpart(String[] splitLocalpart) {
        this.splitLocalpart = splitLocalpart;
        return this;
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (iterator == null) {
            String emailString = readerToString(input);
            if (Strings.isNullOrEmpty(emailString)) {
                return false;
            }
            tokens = tokenize(emailString);
            iterator = tokens.iterator();
        }
        if (!iterator.hasNext()) {
            return false;
        }

        clearAttributes();
        Token token = iterator.next();
        termAttribute.append(token.getToken());
        typeAttribute.setType(token.getPart().name().toLowerCase());
        offsetAttribute.setOffset(token.getStart(), token.getEnd());
        return true;
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        tokens = null;
        iterator = null;
    }


    /**
     * Tokenize the given email address according to the options which have been set.
     * @param emailString the string to be tokenized
     * @return a list of {@link Token}s parsed from the string
     * @throws IOException
     */
    private List<Token> tokenize(String emailString) throws IOException {
        if (!VALIDATOR.matcher(emailString).matches()) {
            if (!allowMalformed) {
                throw new IOException("Malformed email address: " + emailString);
            } else {
                return ImmutableList.of(new Token(emailString, EmailPart.WHOLE, 0, emailString.length() - 1));
            }
        }
        if (part != null) {
            return tokenize(emailString, part);
        }
        List<Token> tokens = new ArrayList<>();
        for (EmailPart emailPart : EmailPart.values()) {
            tokens.addAll(tokenize(emailString, emailPart));
        }
        tokens.addAll(tokenizeSpecial(emailString));
        return tokens;
    }


    /**
     * Tokenize the given email address based on the desired {@link EmailPart} and currently set tokenizer options.
     * @param email the email address to be tokenized
     * @param part the desired part of the email address
     * @return a list of {@link Token}s parsed from the given address
     * @throws IOException
     */
    private List<Token> tokenize(final String email, final EmailPart part) throws IOException {
        String partString = getPart(email, part);
        if (Strings.isNullOrEmpty(partString)) {
            // desired part was not found
            return new ArrayList<>();
        }
        int start = 0;
        int end = 0;
        switch (part) {
            case LOCALPART:
                return tokenizeLocalPart(partString);
            case DOMAIN:
                start = getStartIndex(email, partString);
                if (!tokenizeDomian) {
                    end = getEndIndex(start, partString);
                    return ImmutableList.of(new Token(partString, part, start, end));
                }
                return tokenize(part, new ReversePathHierarchyTokenizer(new StringReader(partString), '.', '.'), start);
            case WHOLE:
                end = partString.length();
                break;
        }
        return ImmutableList.of(new Token(partString, part, start, end));
    }


    /**
     * Tokenize the given localpart of an email address
     * @param localPart the string to be tokenized
     * @return a list of tokens
     */
    private List<Token> tokenizeLocalPart(String localPart) {
        List<Token> tokens = new ArrayList<>(1);
        tokens.add(new Token(localPart, EmailPart.LOCALPART, 0, getEndIndex(0, localPart)));
        if (splitOnPlus && localPart.contains("+")) {
            String beforePlus = StringUtils.substringBefore(localPart, "+");
            tokens.add(new Token(beforePlus, EmailPart.LOCALPART, 0, getEndIndex(0, beforePlus)));
        }
        if (splitLocalpart != null) {
            int start;
            for (String delimiter : splitLocalpart) {
                if (!localPart.contains(delimiter)) {
                    continue;
                }
                start = 0;
                for (String part : Splitter.on(delimiter).splitToList(localPart)) {
                    tokens.add(new Token(part, EmailPart.LOCALPART, start, start + part.length()));
                    start += part.length() + delimiter.length();
                }
            }
        }
        return tokens;
    }


    /**
     * Get a list of {@link Token}s from the given {@link Tokenizer}
     *
     * @param part      the email part which should be used in {@link Token} creation
     * @param tokenizer the tokenizer from which tokens will be gleaned
     * @return a list of tokens
     * @throws IOException
     */
    List<Token> tokenize(EmailPart part, Tokenizer tokenizer, int start) throws IOException {
        tokenizer.reset();
        List<Token> tokens = new ArrayList<>();
        OffsetAttribute offset;
        String token;
        while (tokenizer.incrementToken()) {
            token = tokenizer.getAttribute(CharTermAttribute.class).toString();
            offset = tokenizer.getAttribute(OffsetAttribute.class);
            tokens.add(new Token(token, part, start + offset.startOffset(), start + offset.endOffset()));
        }
        return tokens;
    }


    /**
     * Perform non-standard tokenization.
     * @param email email address to be tokenized
     * @return a list of tokens
     */
    private List<Token> tokenizeSpecial(String email) {
        List<Token> tokens = new ArrayList<>();
        if (splitOnPlus && email.contains("+")) {
            final String withoutPlus = StringUtils.substringBefore(email, "+") + "@" + StringUtils.substringAfter(email, "@");
            tokens.add(new Token(withoutPlus, EmailPart.WHOLE, 0, email.length() - 1));
        }
        return tokens;
    }


    private int getStartIndex(String email, String part) {
        return email.indexOf(part);
    }


    private int getEndIndex(int start, String part) {
        return start + part.length();
    }


    /**
     * Retrieve the given {@link EmailPart} from the given email address
     * @param email the email address from which a part will be extracted
     * @param part the part to extract
     * @return the extracted part string
     */
    private String getPart(String email, EmailPart part) {
        switch (part) {
            case DOMAIN:
                return StringUtils.substringAfter(email, "@");
            case LOCALPART:
                return StringUtils.substringBefore(email, "@");
            case WHOLE:
            default:
                return email;
        }
    }


    /**
     * Read the contents of a {@link Reader} into a string
     *
     * @param reader the reader to be converted
     * @return the entire contents of the given reader
     * @throws IOException
     */
    private String readerToString(Reader reader) throws IOException {
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(arr, 0, arr.length)) != -1) {
            buffer.append(arr, 0, numCharsRead);
        }
        return buffer.toString();
    }


    private class Token {
        private final String token;
        private final EmailPart part;
        private final int start;
        private final int end;

        public Token(String token, EmailPart part, int start, int end) {
            this.token = token;
            this.part = part;
            this.start = start;
            this.end = end;
        }

        public String getToken() {
            return token;
        }

        public EmailPart getPart() {
            return part;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }
    }
}
