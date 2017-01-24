# Elasticsearch Email Tokenizer

This plugin enables email address tokenization.

[![Build Status](https://secure.travis-ci.org/jlinn/elasticsearch-analysis-email.png?branch=master)](http://travis-ci.org/jlinn/elasticsearch-analysis-email)

## Compatibility
| Elasticsearch Version | Plugin Version |
|-----------------------|----------------|
| 2.4.4 | 2.4.4 |
| 2.4.3 | 2.4.3 |
| 2.4.1 | 2.4.1 |
| 2.4.0 | 2.4.0 |
| 2.3.5 | 2.3.5 |
| 2.3.4 | 2.3.4 |
| 2.3.3 | 2.3.3 |
| 2.3.0 | 2.3.0 |
| 2.2.2 | 2.2.2 |
| 2.2.1 | 2.2.1 |
| 2.2.0 | 2.2.0 |
| 2.1.1 | 2.1.1 |
| 2.0.0 | 2.0.0 |
| 1.6.x, 1.7.x | 1.0.0 |

## Installation
```bash
bin/plugin install https://github.com/jlinn/elasticsearch-analysis-email/releases/download/v2.4.4/elasticsearch-analysis-email-2.4.4.zip
```

## Usage
### Options:
* `part`: Defaults to `null`. If left `null`, all email address parts will be tokenized. Options are `whole`, `localpart`, and `domain`.
* `tokenize_domain`: Defaults to `true`. If `true`, the domain will be further tokenized using a [reverse path hierarchy tokenizer](https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-pathhierarchy-tokenizer.html) with the delimiter set to `.`.
* `split_on_plus`: Defaults to `true`. If `true`, the localpart of the email address will be split on the first instance of `+`, and both the part preceding `+` and the whole localpart will be used as tokens.
* `split_localpart`: Defaults to `null`. This parameter expects an array of strings. If provided, the localpart will be split on each of the given strings.
* `allow_malformed`: Defaults to `false`. If `true`, malformed email addresses will not be rejected, but will be indexed without tokenization.

### Example:
Index settings:
```json
{
	"settings": {
		"analysis": {
			"tokenizer": {
				"email_domain": {
					"type": "email",
					"part": "domain"
				}
			},
			"analyzer": {
				"email_domain": {
					"tokenizer": "email_domain"
				}
			}
		}
	}
}
```

Perform an analysis request:
```bash
curl 'http://localhost:9200/index_name/_analyze?analyzer=email_domain&pretty' -d 'foo+bar@email.com'

{
  "tokens" : [ {
    "token" : "email.com",
    "start_offset" : 8,
    "end_offset" : 17,
    "type" : "domain",
    "position" : 1
  }, {
    "token" : "com",
    "start_offset" : 14,
    "end_offset" : 17,
    "type" : "domain",
    "position" : 2
  } ]
}
```