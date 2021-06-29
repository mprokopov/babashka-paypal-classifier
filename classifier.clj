(ns classifier
  (:require [cheshire.core :as json]))

(def jsonObj
  (json/parse-string (slurp "paypal.json") true))

(def transactions
  (get jsonObj :transaction_details))

(defn is-bank-transaction
  "returns bank transaction if has bank_reference_id"
  [{:keys [transaction_info]}]
  (get transaction_info :bank_reference_id))

(defn get-paypal-ref-id
  "retrieve paypal transaction reference"
  [{:keys [transaction_info]}]
  (get transaction_info :paypal_reference_id))

(defn get-bank-ref-id
  "retrieve bank transaction reference"
  [{:keys [transaction_info]}]
  (get transaction_info :bank_reference_id))

(defn index-transactions
  "creates map with index using index-field"
  [transactions index-field]
  (into {}
        (map #(hash-map (get-in % [:transaction_info index-field]) %) transactions)))

(def indexer (partial index-transactions transactions))

(def indexed-transactions (indexer :transaction_id))
(def bank-transactions (indexer :bank_reference_id))

;; (->>
;;  (map #(when (is-bank-transaction %) (get-paypal-ref-id %)) transactions)
;;  (remove nil?)
;;   )

;; (get
;;  (into {} bank-transactions)
;;  "1012978278132"
;;  )

;; (indexed-transactions
;;  (get-paypal-ref-id
;;   (bank-transactions "1012978278132")))

(def corresponding-transaction (comp indexed-transactions get-paypal-ref-id  bank-transactions))

(defn get-payer-name [{:keys [payer_info]}]
  (let [{:keys [payer_name]} payer_info]
    (get payer_name :alternate_full_name)))

;; (get-payer-name
;;  (corresponding-transaction "1012978278132"))

;; (get-payer-name
;;  (corresponding-transaction "1013425445539"))

(def classifier {"Cornelsen Verlag GmbH" "Lena:Courses:Books"
                 "YOOX NET-A-PORTER GROUP S.P.A." "Lena:Clothes"
                 "YourCar GmbH" "Car"})

;; (classifier
;;  (get-payer-name
;;   (corresponding-transaction "1012978278132")))

;; (get-payer-name (corresponding-transaction "1013272375403"))

(defn classify [id]
  ;; {:pre [(assert id)]}
  (classifier
   (get-payer-name
    (corresponding-transaction id))))

;; (remove nil? (vals bank-transactions))

;; (map #(classify (get-bank-ref-id %)) (remove nil? (vals bank-transactions)))

;; (map #(get-bank-ref-id %) (remove nil? (vals bank-transactions)))

;; (keys bank-transactions)

(defn classifier-mapper [id]
  (hash-map id
            {:name
             (get-payer-name (corresponding-transaction id))
             :category (classify id)}))

;; (map #(get-payer-name (corresponding-transaction %))
;;      (remove nil? (keys bank-transactions)))

(def classified-transactions
  (into {}
        (map classifier-mapper
             (remove nil? (keys bank-transactions)))))

(let [[id] *command-line-args*]
  (prn (classified-transactions id)))
