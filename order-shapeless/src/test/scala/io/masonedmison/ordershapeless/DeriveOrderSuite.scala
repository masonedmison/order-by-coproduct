package io.masonedmison.ordershapeless

import shapeless._
import munit.FunSuite
import semiauto.{deriveCatsOrder, deriveOrdering}
import DeriveOrderSuite._

class DeriveOrderSuite extends FunSuite {
  test("Order coproduct must contain same elements as trait coproduct or else will result in compile error.") {
    val expectedCompileTimeErr = "could not find implicit value for parameter align"
    assert(
      compileErrors("deriveCatsOrder[BadOrder, A, CoproductForA]")
        .contains(expectedCompileTimeErr)
    )
  }
  test("Trait coproduct must match trait or else will resultin compile error.") {
    val expectedCompileTimeErr ="could not find implicit value for parameter gen"
    assert(
      compileErrors("deriveCatsOrder[GoodOrder, A, BadCoproductForA]")
        .contains(expectedCompileTimeErr)
    )
  }
  test("Derived cats.kernel.Order should use Order coproduct to determine ordering.") {
    val catsOrderForA = deriveCatsOrder[GoodOrder, A, CoproductForA]
    assert(catsOrderForA.lt(C(), B()))
    assert(catsOrderForA.lt(B(), E()))
  }
  test("Derived Ordering should use Order corpoduct to dtermine ordering.") {
    val orderingForA = deriveOrdering[GoodOrder, A, CoproductForA]
    assert(orderingForA.lt(C(), B()))
    assert(orderingForA.lt(B(), E()))
  }
}

object DeriveOrderSuite {
  sealed trait A extends Product with Serializable
  case class B() extends A
  case class C() extends A
  sealed trait D extends A
  case class E() extends D
  type GoodOrder = C :+: B :+: E :+: CNil
  type BadOrder = E :+: B :+: CNil
  val aGen = Generic[A]
  type CoproductForA = aGen.Repr
  type BadCoproductForA = A :+: B :+: E :+: CNil
}