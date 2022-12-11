## Order your sealed traits in a typesafe, explicit way using a shapeless coproduct!

### Motivation
In some cases we might have a `sealed trait` representing an enum like:
```scala
sealed trait A
case object B extends A
case object C extends A
sealed trait D extends A
case object E extends E
```
where we might desire a `cats.kernel.Order` or `scala.math.Ordering`. For example, we might desire the sorted order of the 
members of our sum adt to be like `E`, `C`, `B`. If we want to ensure implementation is _typesafe_, the only real option we are
left with is to define a pairwise comparison function. Perhaps we have something like this:
```scala
implicit def forOrdering: Ordering.fromLessThan { (a, b) match {
  case (E, _) => true
  case (_, E) => false
  case (D, _) => true
  case (_, D) => false
  case (B, B) => false
  }
}
```

The problem here is that this is quite verbose (even for a sum ADT with only 3 members!)

`order-shapeless` allows us to specify an `Ordering` using a `Shapeless` `Coproduct`. So for our desired ordering above, using `order-shapeless`, we could do the following:
```scala
import io.masonedmison.ordershapeless.semiauto.deriveOrdering
import shapeless._

// define Ordering as coproduct - must contain all elements within our sum adt, else we will get a compile error
type OrderForA = E :+: C :+: B :+: CNil
// we also need a `Generic[A]` instance for the generic representation (a `Coproduct` for A)
// we can easily get this from `Generic.apply` and from this instance, can be accessed by the `Repr` type member.
val aGen = Generic[A]
// derive an order for A
implicit val forOrdering[A]: Ordering[A] = deriveOrdering[OrderForA, A, aGen.Repr]

val sortedAs = List(B, C, E).sorted // List(E, C, B)
```