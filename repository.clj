(ns repository
  (:require
   ;; [babashka.deps :as deps]
   [babashka.pods :as pods]
   [downloader :as crl]
   [r :as r]))

;; (deps/add-deps '{:deps {honeysql/honeysql {:mvn/version "1.0.444"}}})

(pods/load-pod 'org.babashka/go-sqlite3 "0.0.1")
(require '[pod.babashka.go-sqlite3 :as sqlite])

;; (require '[honeysql.core :as sql]
;;          '[honeysql.helpers :as helpers])

(def insert
  (-> (helpers/insert-into :paypal)
      (helpers/columns :id :name :category)
      (helpers/values
       (mapv #(vector (key %)
                      (:name (val %))
                      (:category (val %))) r/classified-transactions))
      sql/format))

;; (sqlite/execute! "foo.db" insert)

(comment
  (->
   (helpers/insert-into :paypal)
   (helpers/columns :id :name :category)
   (helpers/values
    (mapv #(vector (key %)
                   (:name (val %))
                   (:category (val %))) r/classified-transactions))
   (sql/format)
   )
  

  (doseq [statement r/classified-transactions]
    (let [{:keys [category name]} (val statement)]
      (sqlite/execute! "foo.db"
                       ["insert or replace into paypal (id, name, category) values (?,?,?)"
                        (key statement)
                        name
                        category
                        ]))))


(crl/get-balance crl/token "2021-04-01T00:00:00-0200" "2021-04-30T23:59:00-0200")

;; (def now (java.time.ZonedDateTime/now))

;; (java.time.LocalDateTime/parse "2021-05-01T00:00:00")

;; (.format java.time.format.DateTimeFormatter/ISO_OFFSET_DATE_TIME now)

;; (->
;;  (java.time.LocalDate/of 2021 05 02)
;;  (.with (java.time.temporal.TemporalAdjusters/firstDayOfMonth)))

;; (->
;;  (java.time.LocalDate/of 2021 06 02)
;;  (.with (java.time.temporal.TemporalAdjusters/firstDayOfMonth)))

;; (->
;;  (java.time.LocalDate/of 2021 05 02)
;;  (.with (java.time.temporal.TemporalAdjusters/lastDayOfMonth)))

;; (def now (java.time.LocalDateTime/now))

;; (-> now
;;     (.with (java.time.temporal.TemporalAdjusters/firstDayOfMonth))
;;     (.with (java.time.LocalTime/MIN)))

;; (-> now
;;     (.with (java.time.temporal.TemporalAdjusters/lastDayOfMonth))
;;     (.with (java.time.LocalTime/MAX))
;;     (.format java.time.format.DateTimeFormatter/ISO_OFFSET_DATE_TIME)
;;     )

