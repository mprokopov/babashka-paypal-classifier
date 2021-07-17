# Babashka paypal transactions download and classify scripts

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

Saves paypal.json for the further processing. Downloader accepts -b for begin
date YYYY-MM-DD, default is beginning day of the current month and -e for end
date, default is current date time. Specify -o xxx.json for different output
target. Default output is paypal.json.

Create paypal sqlit3 schema.
```shell
sqlite3 paypal.sqlite3 < create.sql
```

Execute
```shell
BABASHKA_CLASSPATH=$(pwd) bb repository.clj
```

processes paypal.json and populates paypal.sqlite3 database with classified categories.
Keys: -o output.sqlite3, override output database target.

TODO: specify -f key for json input.

check it with

```shell
sqlite3 paypal.sqlite3 "select * from paypal"
```

### Example output
I normally use this from Makefile from my personal automated ledger system.

```
BABASHKA_CLASSPATH=/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal ../../paypal/downloader.clj -b `date '+%Y-%m-%d'`
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/downloader.clj:103:7 #object[java.time.ZonedDateTime 0x96bd505 2021-07-01T00:00+02:00[Europe/Berlin]]
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/downloader.clj:104:7 #object[java.time.ZonedDateTime 0x5e577aa 2021-07-17T11:02:38.160346+02:00[Europe/Berlin]]
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/downloader.clj:107:7 saved to paypal.json
BABASHKA_CLASSPATH=/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal ../../paypal/repository.clj -f paypal.json -o paypal2.sqlite3
nil
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/repository.clj:38:9 parsed statement  [1014509217597 {:name LogPay Financial Services GmbH, :category Transport:Commute}]
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/repository.clj:38:9 parsed statement  [1014656067296 {:name eBay, :category Apartment:eBay}]
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/repository.clj:38:9 parsed statement  [1014663771431 {:name Wayfair DE, :category Apartment:Wayfair}]
/Users/nexus/playground/csv2ledger/2021/sparda/../../paypal/repository.clj:44:5 Target DB paypal2.sqlite3
```

