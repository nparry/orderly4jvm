About
-----

This is an implementation of [Orderly JSON](http://orderly-json.org/) for use on
the JVM.  The parser is written in Scala.

A JSON schema validator is also included, so you can validate JSON values against
your Orderly definitions.

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
    
    List<Violation> noProblems = orderly.getViolations("200");
    List<Violation> notAllowed = orderly.getViolations("50");

From Scala:

    import com.nparry.orderly._
    import net.liftweb.json.JsonAST._
    
    val orderly = Orderly("integer {0,100};")
    
    val noProblems = orderly.validate(JInt(200))
    val notAllowed = orderly.validate(JInt(50))


TODOs
-----

* Enhance Java APIs to use an actual Java JSON library.
* The JSON schema validator needs some work.
* Provide prebuilt jars with a Maven POM.

