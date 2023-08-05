import utest._
import org.chipsalliance.cde.config.{Config, Field, Parameters}

case object MyKey1 extends Field[Int](-1)
case object MyKey2 extends Field[Int](-1)
case object MyKey3 extends Field[Int]
case object MyKey4 extends Field[Int]

object SanityTest extends TestSuite {
  private val empty = Parameters.empty

  override val tests: Tests = Tests {
    test("Empty Parameter query returns default value") {
      empty(MyKey1) ==> -1
    }

    test("Query Field with no default value throws") {
      intercept[java.lang.IllegalArgumentException] {
        empty(MyKey3)
      }
    }

    test("SimpleAlterMethods") {
      test("alterMap") {
        val altered = empty.alterMap(Map((MyKey1, 1), (MyKey2, 2)))

        altered(MyKey1) ==> 1
        altered(MyKey2) ==> 2
      }
      test("alterPartial") {
        val altered = empty.alterPartial({ case MyKey1 => 1; case MyKey3 => 3 })

        altered(MyKey1) ==> 1
        altered(MyKey3) ==> 3
      }
    }

    test("ComplexAlterMethods") {
      val C1 = new Config((site, here, up) => {
        case MyKey1 => 1
        case MyKey2 => site(MyKey1)
      })
      val C2 = new Config((site, here, up) => {
        case MyKey1 => 2
        case MyKey3 => here(MyKey1)
        case MyKey4 => up(MyKey2)
      })
      val C3 = new Config((site, here, up) => {
        case MyKey1 => 3
        case MyKey2 => up(MyKey1)
      })

      test("Different chaining methods") {
        val C123 = C1.alter(C2).alter(C3)
        val AnotherC123 = C3.orElse(C2).orElse(C1)
        val MoreC123 = C2.alter(C3).orElse(C1)

        for (k <- Seq(MyKey1, MyKey2, MyKey3, MyKey4)) {
          assert(C123(k) == AnotherC123(k))
          assert(C123(k) == MoreC123(k))
        }
      }

      test("Alter indeed alters site") {
        C1(MyKey2) ==> 1
        C2(MyKey2) ==> -1
        C1.alter(C2)(MyKey2) ==> 2
      }

      test("Here is indeed here") {
        assert(C2(MyKey3) == C2(MyKey1))
        assert(C3(MyKey1) != C2(MyKey1))

        C2.alter(C3)(MyKey3) ==> C2(MyKey1)
      }

      test("Up is also up") {
        C3(MyKey2) ==> -1
        C3.orElse(C2).orElse(C1)(MyKey2) ==> 2
      }

      test("site() is always site()") {
        val C123 = C1.alter(C2).alter(C3)

        C123(MyKey1) ==> C3(MyKey1)
        // C123(MyKey4) ==> C2(up(MyKey2))
        // C2(up(MyKey2)) then evaluates C1(site(MyKey1))
        // site here still refers to C123 as query begins
        C123(MyKey4) ==> C123(MyKey1)
      }
    }
  }
}
