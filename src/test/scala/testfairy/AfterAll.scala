package testfairy

import org.specs2.Specification
import org.specs2.specification.{Step, Fragments}

trait AfterAll extends Specification {
    override def map(fragments: => Fragments) =
        fragments ^ Step(afterAll)

    protected def afterAll()
}
