Orderly4JVM [![Build Status](https://travis-ci.org/nparry/orderly4jvm.svg)](https://travis-ci.org/nparry/orderly4jvm)
===========

About
-----

This is an implementation of [Orderly JSON](http://orderly-json.org/) for use on
the JVM.  The parser is written in Scala.

A JSON schema validator is also included, so you can validate JSON values against
your Orderly definitions.

Prebuilt jars/POMs/etc are published to http://repository.nparry.com/releases,
which you should be able to add to your dependency manager of choice. In
Maven-ese, this would look something like...

    <repositories>
      <repository>
        <id>nparry-releases</id>
        <url>http://repository.nparry.com/releases</url>
        <releases>
          <enabled>true</enabled>
        </releases>
        <snapshots>
          <enabled>false</enabled>
        </snapshots>
      </repository>
    </repositories>

This implementation uses the same test suite as the reference implementation to
help ensure consistent behavior.  If you find a case that produces different
output, please let me know and I will add it to the test suite.

This project is covered by the [BSD license](http://www.opensource.org/licenses/bsd-license.php).


Usage
-----

You can use Orderly directly through Scala or through a set of Java interfaces.

From Java:

    import com.nparry.orderly.api.*;
    import com.nparry.orderly.*;
    import java.util.List;

    OrderlyFaactory factory = new DefaultOrderlyFactory();
    Orderly orderly = factory.getOrderly("integer {0,100};");

    List<Violation> noProblems = orderly.getViolations("50");
    List<Violation> notAllowed = orderly.getViolations("200");

From Scala:

    import com.nparry.orderly._
    import net.liftweb.json.JsonAST._

    val orderly = Orderly("integer {0,100};")

    val noProblems = orderly.validate(JInt(50))
    val notAllowed = orderly.validate(JInt(200))

An example of usage via the Scala console:

    scala> val orderly = Orderly("integer {0,100};")
    orderly: com.nparry.orderly.Orderly = 
    {
      "type":"integer",
      "minimum":0,
      "maximum":100
    }

    scala> val noProblems = orderly.validate(JInt(50))
    noProblems: List[com.nparry.orderly.Violation] = List()

    scala> val notAllowed = orderly.validate(JInt(200))
    notAllowed: List[com.nparry.orderly.Violation] = List(Violation(List(),200 is greater than maximum allowed value))

