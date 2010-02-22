About
-----

This is an implementation of [Orderly JSON](http://orderly-json.org/) for use on
the JVM.  The parser is written in Scala.

The Orderly reference implementation can translate bi-directionally between
Orderly and JSON schema.  At present, this implementation only translates to
JSON schema, not back into Orderly.

This project is covered by the [BSD license](http://www.opensource.org/licenses/bsd-license.php).


Goal
----

This implementation should produce the same JSON schema as the reference
implementation.  This is currently enforced by running the same set of tests as
the RI and comparing the output.  Outputs from this implementation and the RI are
first pretty-printed then compared as strings; currently all tests produce the
same output discounting any whitespace differences erased by the
pretty-printing.

If you find a case that produces different output, please let me know and I
will add it to the test suite.


Usage
-----

todo :-(


TODOs
-----

* Create a plain Java wrapper for the underlying Scala parser.
* Validate JSON input given the schema resulting from Orderly.

