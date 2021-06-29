# Babashka paypal transactions download and classify

## Usage
Store paypal credentials in environment variables

PAYPAL_USERNAME and PAYPAL_PASSWORD

```shell
bb downloader.clj
```

saves paypal.json for the further processing

```shell
bb repository.clj
```

processes paypal.json and populates paypal.sqlite3 database with classified categories


### TODO

- reorganize inputs and outputs from stdin and stdout and pipe downloader.clj to repository.clj

