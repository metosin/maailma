# Maailma [![Build Status](https://travis-ci.org/metosin/maailma.svg?branch=master)](https://travis-ci.org/metosin/maailma)

[![Clojars Project](http://clojars.org/metosin/maailma/latest-version.svg)](http://clojars.org/metosin/maailma)

[API Docs](http://metosin.github.io/maailma/maailma.core.html).

## Project statement

Unlike more general of our libraries (like
[compojure-api](https://github.com/metosin/compojure-api) and
[ring-swagger](https://github.com/metosin/ring-swagger)) this project is
primarily intended for use in Metosin's projects. Feel free to use, but
don't expect full support.

- We might remove features if we think they are not useful anymore
- We will reject PRs and issues about features we wouldn't use ourselves

## Features

- Represents application configuration as a single map which is read once on startup
    - Should work well with [Component](https://github.com/stuartsierra/component)
- Reads configuration from multiple sources and recursively merges it
    - EDN files in classpath – shipped with JAR, useful for default options
    - EDN files in filesystem – created by hand or by deploy tooling
    - Environment variables
    - Java properties
    - Override parameter – useful for overriding options for [test systems](https://github.com/metosin/palikka/blob/master/test/palikka/core_test.clj#L9)
    - User selects the sources themselves
    - Extendable with functions
- EDN readers can be provided as option, which allows use with [Integrant](https://github.com/weavejester/integrant)

## Example

```clj
(ns backend.system
  (:require [maailma.core :as m]))

(defn system [override]
  (let [env (m/build-config
              (m/resource "config-defaults.edn")
              (m/env "prefix")
              (m/env-var "SERVER_PORT" [:http :port])
              (m/properties "prefix")
              (m/file "./config-local.edn")
              override)]
    ...))
```

### Integrant example

Add reader options to `resource` and `file` calls:

```clj
(ig/load-namespaces
  (m/build-config
    (m/resource "config.edn" {:readers {'ig/ref ig/ref}})
    (m/file "config-local.edn" {:readers {'ig/ref ig/ref}})))
```

(Not supported with `read-config!` function)

## Notes

- Is it necessary to refresh configuration of running application?
    - Most fixes done to running production systems over REPL are very small fixes to integration logic
    - It's possible that some integration settings need to be changed quickly?
    - Probably not necessary. Most logic is isolated to pure functions and the can be replaced over REPL without touching components or configuration.

## License

Copyright © 2015-2018 [Metosin Oy](http://www.metosin.fi).

Distributed under the Eclipse Public License 2.0.
