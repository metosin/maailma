## 1.1.0 (2018-03-28)

Added optional `reader-opts` parameter to `resource` and `file`. This allows using
Maailma to read Integrant configuration files:

```clj
(m/resource "config.edn" {:readers {'ig/ref ig/ref}})
```

## 1.0.0 (2018-01-25)

We have been using this library for a couple of years now without new releases.
It's time to call it 1.0.0!

- Removed `maailma.encrypt` namespace.
- Add `maailma.core/env-var` for reading single environmental variables.
- Updated dependencies.

## 0.2.0 (2015-11-07)

Initial public release.
