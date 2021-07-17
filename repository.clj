#!/usr/local/bin/bb
(ns repository
  (:require
   ;; [babashka.deps :as deps]
   [babashka.pods :as pods]
   [clojure.string :as string]
   [clojure.tools.cli :refer [parse-opts]]
   ;; [downloader :as crl]
   [log :as logger]
   [classifier :as classifier]))

;; (deps/add-deps '{:deps {honeysql/honeysql {:mvn/version "1.0.444"}}})

(pods/load-pod 'org.babashka/go-sqlite3 "0.0.1")
(require '[pod.babashka.go-sqlite3 :as sqlite])
                                        ;(require '[downloader :as downloader])

;; (require '[honeysql.core :as sql]
;;          '[honeysql.helpers :as helpers])

;; (def insert
;;   (-> (helpers/insert-into :paypal)
;;       (helpers/columns :id :name :category)
;;       (helpers/values
;;        (mapv #(vector (key %)
;;                       (:name (val %))
;;                       (:category (val %))) r/classified-transactions))
;;       sql/format))

;; (sqlite/execute! "foo.db" insert)

(defn create-table [target-file]
  (sqlite/execute! target-file ["create table paypal (bank_ref_id TEXT, name TEXT, category TEXT)"]))

(defn save-statements [target-file statements]
  (doseq [statement statements]
    (let [{:keys [category name]} (val statement)]
      (logger/log "parsed statement " statement)
      (sqlite/execute! target-file
                       ["insert or replace into paypal (bank_ref_id, name, category) values (?,?,?)"
                        (key statement)
                        name
                        category])))
  (logger/log (format "Target DB %s" target-file)))

(def cli-options
  [["-o" "--out file" "output DB"
    :default "paypal2.sqlite3"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Paypal transactions classifier"
        ""
        "Usage: repository.clj [options]"
        ""
        "Options:"
        options-summary
        ""
        "Please refer to the manual page for more information."]
       (string/join \newline)))

(let [{:keys [options summary]} (parse-opts *command-line-args* cli-options)
      {:keys [out help]} options]
  (if help
    (println (usage summary))
    (save-statements out classifier/classified-transactions)))

(comment
  (parse-opts ["aaa" "-h"] cli-options)

  (->
   (helpers/insert-into :paypal)
   (helpers/columns :id :name :category)
   (helpers/values
    (mapv #(vector (key %)
                   (:name (val %))
                   (:category (val %))) classifier/classified-transactions))
   (sql/format))
  )
