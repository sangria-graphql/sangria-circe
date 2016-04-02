## v0.4.1 (2016-04-02)

* Updated `circe` to version 0.4.0-RC1

## v0.4.0 (2016-03-24)

* Updated to sangria-marshalling-api v0.2.0

## v0.3.1 (2016-02-28)

* Updated to latest version of marshalling API
  
## v0.3.0 (2016-02-27)

* Added support for `Encoder`/`Decoder`. This provides `ToInput` and `FromInput` instances for arbitrary tuples, case classes, etc. as long
  as you have appropriate `io.circe.Encoder`/`io.circe.Decoder` in scope (in most cases it's enough to just `import io.circe.generic.auto._`)

## v0.2.0 (2016-02-15)

* Updated `circe` to version 0.3.0

## v0.1.0 (2016-01-23)

* Initial release