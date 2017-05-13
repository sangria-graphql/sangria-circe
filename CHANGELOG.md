## v1.1.0 (2017-05-13)

* Updated `circe` to version 0.8.0

## v1.0.1 (2017-01-18)

* Updated `circe` to version 0.7.0

## v1.0.0 (2017-01-16)

* 1.0 Release

## v0.6.1 (2016-11-20)

* Updated `circe` to version 0.6.1

## v0.6.0 (2016-11-10)

* Updated `circe` to version 0.6.0
* Cross-compile for scala 2.11 and 2.12
* Updated dependencies

## v0.5.3 (2016-10-23)

* Updated `circe` to version 0.5.4

## v0.5.2 (2016-10-14)

* Encode `Double.NaN` and infinity as `null` (#2). Big thanks to @jonas for this contribution!

## v0.5.1 (2016-10-08)

* Updated `circe` to version 0.5.3

## v0.5.0 (2016-09-01)

* Updated `circe` to version 0.5.0

## v0.4.4 (2016-05-01)

* Updated to sangria-marshalling-api v0.2.1

## v0.4.3 (2016-04-11)

* Updated `circe` to version 0.4.1

## v0.4.2 (2016-04-16)

* Updated `circe` to version 0.4.0

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