#!/usr/local/bin/bb
(ns downloader)

(require '[babashka.curl :as curl])
(require '[cheshire.core :as json])

(def username (System/getenv "PAYPAL_USERNAME"))

(def password (System/getenv "PAYPAL_PASSWORD"))

(def paypal-api "https://api-m.paypal.com/v1")
(def paypal-api-auth (str paypal-api "/oauth2/token"))
(def paypal-api-transactions (str paypal-api "/reporting/transactions"))

(def token
  (get
   (json/parse-string (:body (curl/post paypal-api-auth {:basic-auth [username password]
                                                    :form-params {"grant_type" "client_credentials"}
                                                    }))) "access_token"))
;; (curl/post paypal-api {:headers {"Accept" "application/json"}
;;                        :basic-auth [username password]
;;                        :form-params {"grant_type" "client_credentials"}
;;                        :debug true
;;                        })


(defn get-balance [token start_date end_date]
  (:body (curl/get paypal-api-transactions {:headers {"Authorization" (str "Bearer " token)
                                                      "Accept" "application/json"}
                                            :query-params {"start_date" start_date
                                                           "end_date" end_date
                                                           "fields" "payer_info"}})))

(spit "t.json"
      (get-balance token "2021-04-01T00:00:00-0200" "2021-04-30T23:59:00-0200"))
