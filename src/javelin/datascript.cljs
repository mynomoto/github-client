(ns javelin.datascript
  (:refer-clojure :exclude [select-keys pop])
  (:require
    [datascript.core :as datascript]
    [javelin.core :as j]
    [benefactor.util]))

(def empty-db datascript/empty-db)

(defn create-conn [& [schema]]
  (j/cell (empty-db schema)
    :meta {:listeners (j/cell {})}))

(def transact! datascript/transact!)

(def entity datascript/entity)

(def q datascript/q)

(defn touch
  "A nil safe touch because the entity may be nil sometimes."
  [entity]
  (when entity
    (datascript/touch entity)))

(defn select-keys
  "A nil safe select keys for use with entities so it don't try to fetch a nil key."
  [entity korks]
  (->> (benefactor.util/korks->ks korks)
       (remove nil?)
       (clojure.core/select-keys entity)))

(defn- safe-pop
  [x]
  (or (try (clojure.core/pop x) (catch js/Error e)) x))

(defn pop
  [db eid attr]
  (let [pop-fn (fn [db eid]
                 (let [new-vector (safe-pop (ffirst
                                              (q '{:find [?vector]
                                                   :in [$ ?eid ?attr]
                                                   :where [[?eid ?attr ?vector]]}
                                                db eid attr)))]
                   [{:db/id eid attr new-vector}]))]
    (transact! db [[:db.fn/call pop-fn eid]])))

(defn push
  [db eid attr value]
  (let [push-fn (fn [db eid]
                  (let [new-vector ((fnil conj [])
                                    (ffirst
                                      (q '{:find [?vector]
                                           :in [$ ?eid ?attr]
                                           :where [[?eid ?attr ?vector]]}
                                        db eid attr))
                                    value)]
                    [{:db/id eid attr new-vector}]))]
    (transact! db [[:db.fn/call push-fn eid]])))

(defn set-add
  [db eid attr value]
  (let [add-fn (fn [db eid]
                 (let [new-set ((fnil conj #{})
                                (ffirst
                                  (q '{:find [?set]
                                       :in [$ ?eid ?attr]
                                       :where [[?eid ?attr ?set]]}
                                    db eid attr))
                                value)]
                   [{:db/id eid attr new-set}]))]
    (transact! db [[:db.fn/call add-fn eid]])))

(defn set-remove
  [db eid attr value]
  (let [remove-fn (fn [db eid]
                    (let [new-set (disj (ffirst
                                          (q '{:find [?vector]
                                               :in [$ ?eid ?attr]
                                               :where [[?eid ?attr ?vector]]}
                                            db eid attr))
                                    value)]
                      [{:db/id eid attr new-set}]))]
    (transact! db [[:db.fn/call remove-fn eid]])))
