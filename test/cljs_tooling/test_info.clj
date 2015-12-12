(ns cljs-tooling.test-info
  (:require [clojure.tools.reader.edn :as edn]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.string :as s]
            [clojure.test :refer :all]
            [cljs-tooling.info :as info]
            [cljs-tooling.test-env :as test-env]
            [cljs-tooling.util.misc :as u]
            [cljs.core]
            [cljs.core.async.macros]
            [cljs.core.async.impl.ioc-macros]
            [om.core]))

(use-fixtures :once test-env/wrap-test-env)

(defn info
  [& args]
  (apply info/info test-env/*env* args))

(deftest unquote-test
  (is (= [1 2 3] (#'info/unquote-1 '(quote [1 2 3]))))
  (is (= [1 2 3] (#'info/unquote-1 [1 2 3])))
  (is (= nil (#'info/unquote-1 nil))))

(deftest info-test
  (testing "Resolution from current namespace"
    (let [plus (info '+ 'cljs.core )]
      (is (= (:name plus) (-> #'+ meta :name)))
      (is (= (:ns plus) 'cljs.core))))

  (testing "Resolution from other namespaces"
    (let [plus (info '+ 'cljs.core.async)]
      (is (= (:name plus) (-> #'+ meta :name)))
      (is (= (:ns plus) 'cljs.core))))

  (testing "Namespace itself"
    (is (= (-> (info 'cljs.core) keys sort)
           (sort '(:ns :name :line :file :doc :author)))))

  (testing "Aliased var"
    (let [res (info 'dispatch/process-messages 'cljs.core.async)]
      (is (= (select-keys res [:ns :column :line :name :arglists])
             '{:ns cljs.core.async.impl.dispatch
               :column 1
               :line 13
               :name process-messages
               :arglists ([])}))
      (is (.endsWith (:file res) "cljs/core/async/impl/dispatch.cljs"))))

  (testing "Fully-qualified var"
    (let [res (info 'clojure.string/trim 'cljs-tooling.test-ns)]
      (is (= (select-keys res [:ns :column :line :name :arglists :doc])
             '{:ns clojure.string
               :column 1
               :line 165
               :name trim
               :arglists ([s])
               :doc "Removes whitespace from both ends of string."}))))

  (testing "Namespace alias"
    (let [res (info 'dispatch 'cljs.core.async)]
      (is (= (select-keys res [:ns :line :name])
             '{:ns cljs.core.async.impl.dispatch
               :name cljs.core.async.impl.dispatch
               :line 1}))
      (is (.endsWith (:file res) "cljs/core/async/impl/dispatch.cljs"))))

  (testing "Macro namespace"
    (is (= (info 'cljs.core.async.macros)
           (info 'cljs.core.async.macros 'cljs.core.async)
           '{:author nil
             :ns cljs.core.async.macros
             :doc nil
             :file "cljs/core/async/macros.clj"
             :line 1
             :name cljs.core.async.macros})))

  (testing "Macro namespace alias"
    (is (= (info 'ioc)
           (info 'ioc 'om.core)
           nil))
    (is (= (info 'ioc 'cljs.core.async.impl.ioc-helpers)
           '{:author nil
             :doc nil
             :file "cljs/core/async/impl/ioc_macros.clj"
             :line 1
             :name cljs.core.async.impl.ioc-macros
             :ns cljs.core.async.impl.ioc-macros})))

  (testing "cljs.core macro"
    (is (= (info 'loop)
           (info 'cljs.core/loop)
           (info 'loop 'cljs.core.async)
           (info 'cljs.core/loop 'cljs.core.async)
           '{:ns cljs.core
             :doc "Evaluates the exprs in a lexical context in which the symbols in\n  the binding-forms are bound to their respective init-exprs or parts\n  therein. Acts as a recur target."
             :file "cljs/core.cljc"
             :column 1
             :line 732
             :name loop
             :arglists ([bindings & body])})))

  (testing "Macro"
    (is (= (info 'go)
           (info 'go 'om.core)
           nil))
    (is (= (info 'cljs.core.async.macros/go)
           (info 'go 'cljs.core.async)
           '{:ns cljs.core.async.macros
             :doc "Asynchronously executes the body, returning immediately to the\n  calling thread. Additionally, any visible calls to <!, >! and alt!/alts!\n  channel operations within the body will block (if necessary) by\n  'parking' the calling thread rather than tying up an OS thread (or\n  the only JS thread when in ClojureScript). Upon completion of the\n  operation, the body will be resumed.\n\n  Returns a channel which will receive the result of the body when\n  completed"
             :file "cljs/core/async/macros.clj"
             :column 1
             :line 4
             :name go
             :arglists ([& body])}))))
