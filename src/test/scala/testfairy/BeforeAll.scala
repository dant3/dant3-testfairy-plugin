package testfairy

import org.specs2.specification.{Step, Fragments}
import org.specs2.Specification

trait BeforeAll extends Specification {
    // see http://bit.ly/11I9kFM (specs2 User Guide)
    override def map(fragments: => Fragments) =
        Step(beforeAll) ^ fragments

    protected def beforeAll()
}
