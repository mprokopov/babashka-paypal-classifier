#!/usr/local/bin/bb
(ns downloader
  (:require [babashka.curl :as curl]
            [clojure.tools.cli :refer [parse-opts]]
            [cheshire.core :as json]
            [log :as logger])
  (:import [java.time ZonedDateTime]))

;; (require '[babashka.curl :as curl])
;; (require '[cheshire.core :as json])

(def username (System/getenv "PAYPAL_USERNAME"))
(def password (System/getenv "PAYPAL_PASSWORD"))

(def paypal-api "https://api-m.paypal.com/v1")
(def paypal-api-auth (str paypal-api "/oauth2/token"))
(def paypal-api-transactions (str paypal-api "/reporting/transactions"))

(def token
  (get
   (json/parse-string (:body (curl/post paypal-api-auth {:basic-auth [username password]
                                                         :form-params {"grant_type" "client_credentials"}
                                                         })))
   "access_token"))
;; (curl/post paypal-api {:headers {"Accept" "application/json"}
;;                        :basic-auth [username password]
;;                        :form-params {"grant_type" "client_credentials"}
;;                        :debug true
;;                        })


(def now (java.time.ZonedDateTime/now))
(def formatter (.ofPattern java.time.format.DateTimeFormatter "yyyy-MM-dd"))

(defn first-month-day-of-time [^java.time.ZonedDateTime time]
  (-> time (.with (java.time.temporal.TemporalAdjusters/firstDayOfMonth))
      (.with (java.time.LocalTime/MIN))))

(defn format-iso [^java.time.ZonedDateTime time]
  (-> time
      (.truncatedTo(java.time.temporal.ChronoUnit/SECONDS))
      (.format java.time.format.DateTimeFormatter/ISO_OFFSET_DATE_TIME)))

;; (spit "paypal.json"
;;       (get-balance token "2021-06-01T00:00:00-0200" "2021-06-30T23:59:00-0200"))

;; (println
;;  (get-balance token (format-iso (first-month-day-of-time now))
;;               (format-iso now)))

;; (require 'clojure.instant)
;; (clojure.instant/parse-timestamp "2021/05/01")

(def cli-options
  [["-o" "--out file" "output json file"
    :default "paypal.json"]
   ["-b" "--begin date" "report start date"
    :default (.format (first-month-day-of-time now) formatter)]
   ["-e" "--end date" "report end date"]
   ["-h" "--help"]])

(defn parse-begin-date
  ; "parses time using default system zone"
  ([date-string zone]
   (let [formatter (.ofPattern java.time.format.DateTimeFormatter "yyyy-MM-dd")]
     (-> (java.time.LocalDate/parse date-string formatter)
         (.with (java.time.temporal.TemporalAdjusters/firstDayOfMonth))
         .atStartOfDay
         (.atZone zone))))
  ([date-string]
   (parse-begin-date date-string (.systemDefault java.time.ZoneId))))

(defn parse-end-date
  "parses time using default system zone"
  ([date-string zone]
   (let [formatter (.ofPattern java.time.format.DateTimeFormatter "yyyy-MM-dd")]
     (-> (java.time.LocalDate/parse date-string formatter)
         .atStartOfDay
         (.plusDays 1)
         (.minusSeconds 1)
         (.atZone zone))))
  ([date-string]
   (parse-end-date date-string (.systemDefault java.time.ZoneId))))

(defn get-balance [token ^ZonedDateTime start-date ^ZonedDateTime end-date]
  (let [formatted-start-date (format-iso start-date)
        formatted-end-date (format-iso end-date)]
    ;; (log formatted-start-date)
    ;; (log formatted-end-date)
    (:body (curl/get paypal-api-transactions {:headers {"Authorization" (str "Bearer " token)
                                                        "Accept" "application/json"}
                                             :query-params {"start_date" formatted-start-date
                                                            "end_date" formatted-end-date
                                                            "fields" "payer_info"}}))))

(let [{:keys [options help summary]} (parse-opts *command-line-args* cli-options)
      {:keys [begin end out]} options
      begin-dt-str (parse-begin-date begin)
      end-dt-str (if end (parse-end-date end) now)]
  (if help
    (prn summary)
    (do
      (logger/log begin-dt-str)
      (logger/log end-dt-str)
      (spit out
            (get-balance token begin-dt-str end-dt-str))
      (logger/log (format "saved to %s" out)))))

(comment
  (parse-opts ["downloader.clj" "-b 2021-01-02" "-e 2021-05-04"] cli-options)
  (parse-opts ["downloader.clj" "-b 2021-01-02"] cli-options)
  (parse-opts ["downloader.clj" "-e 2021-05-04"] cli-options)

  (def dt
    (java.time.LocalDate/parse "2019-05-07" formatter))

  (def zone (.systemDefault java.time.ZoneId))

  (->
   dt
   (.with (java.time.temporal.TemporalAdjusters/lastDayOfMonth))
   .atStartOfDay
   (.plusDays 1)
   (.minusSeconds 1)
   (.atZone zone)
   #_format-iso)

  (def dt
    (java.time.LocalDate/of 2021 05 01))

  (def now (.with (java.time.ZonedDateTime/now) java.time.temporal.ChronoUnit/MONTHS))
  )
