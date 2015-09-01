(ns cljs-tooling.test-ns
  (:refer-clojure :exclude [unchecked-byte while])
  (:require [cljs.core.async :refer [sliding-buffer]]
            [clojure.string]
            [om.core :as om]))

(defrecord TestRecord [a b c])

(def x ::some-namespaced-keyword)
