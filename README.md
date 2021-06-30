# Babashka paypal transactions download and classify

set of scripts to download transactions from paypal and process these with classification function.
## Prerequisites
- babashka 0.4.5 and higher
- sqlite3

## Usage
Store paypal credentials in environment variables

PAYPAL_USERNAME and PAYPAL_PASSWORD

```shell
export PAYPAL_USERNAME=xxx
export PAYPAL_PASSWORD=yyy
bb downloader.clj
```

saves paypal.json for the further processing. 

Create paypal sqlit3 schema.
```shell
sqlite3 paypal.sqlite3 < create.sql
```

Execute
```shell
BABASHKA_CLASSPATH=$(pwd) bb repository.clj
```

processes paypal.json and populates paypal.sqlite3 database with classified categories

check it with

```shell
sqlite3 paypal.sqlite3 "select * from paypal"
```

### TODO

- reorganize inputs and outputs from stdin and stdout and pipe downloader.clj to repository.clj

