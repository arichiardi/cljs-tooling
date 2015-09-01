# cljs-tooling

[![Continuous Integration status](https://travis-ci.org/clojure-emacs/cljs-tooling.svg)](http://travis-ci.org/clojure-emacs/cljs-tooling)

A Clojure library designed to provide tooling support for ClojureScript.
Currently it provides var info and auto-completion based on compiler state.

It is the basis for ClojureScript features in
[cider-nrepl](https://github.com/clojure-emacs/cider-nrepl),
including source navigation and auto-completion, but is separate for
the sake of test-harnesses and decoupled development.

## Artifacts

With leiningen:

     [cljs-tooling "0.1.7"]

## Usage

```clojure
;; env is pulled from cljs compiler state
=> (completions @cljs.env/*compiler* "al" 'cljs.core)
("alength" "alter-meta!")
```

## Contributors

* [Gary Trakhman](http://github.com/gtrak)
* [Michael Griffiths](http://github.com/cichli)
* [Juho Teperi](http://github.com/Deraen)

## License

Copyright © 2014-2015 Gary Trakhman

Distributed under the Eclipse Public License, the same as Clojure.
