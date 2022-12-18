# Derive a cats `Order` using a `shapeless` `HList` or `Coproduct`

## Use cases
### Derive a typesafe `cats.kernel.Order` for a sum ADT using a `shapeless.Coproduct`.
Given a sum ADT:
```scala
sealed trait A
case object B extends A
case object C extends A
sealed trait D extends A
case object E extends E
```
where we might desire a `cats.kernel.Order` - specifically, we might desire the sorted order of the 
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

Using `order-shapeless`, we can specify a `Coproduct` with our desired ordering like
```scala
import io.masonedmison.ordershapeless.orderByCoproduct
import shapeless.{CNil, :+:}

// define Ordering as coproduct - must contain all elements within our sum adt, else we will get a compile error
type OrderForA = E :+: C :+: B :+: CNil
val forOrder: Order[A] = orderByCoproduct[OrderForA, A]

implicit val forOrdering = forOrder.toOrdering

val sortedAs = List(B, C, E).sorted // List(E, C, B)
```

Also note that this is typesafe, ie if we pass a `Coproduct` that _does not_ contain the same members of our sum ADT, we will get a compile error:
```scala
type OrderForA = C :+: B :+: CNil // missing E...
val forOrder: Order[A] = orderByCoproduct[OrderForA, A] // fails to compile with "could not find implicit value for parameter align..."
```

### Exclude field(s) when constructing an Order
In some cases, you might have a case class that has a set of fields of which you _don't_ want to be considered as part of sorting.
For example, given a case class like:
```scala
case class Coffee(id: UUID, name: String, origin: String, price: Double, hasBeenGround: Boolean)
```
We might want to exclude the field `id` as part of our `Order`.

We can easily specify this as follows:
```scala
import io.masonedmison.ordershapeless.{CaseClassField, exclude}
import shapeless.{::, HNil}

// We specify the fields to exlude as an HList of `CaseClassField`s
// A `CaseClassField[Name, T]` is an alias for a `shapeless.FieldType` where `Name` is a singleton type of the "name" of the field,
// and T is the type of the **field**.
type Exclude = CaseClassField["id", Int] :: HNil
val forOrder: Order[Coffee] = exclude[Coffee, Exclude]

val c1 = Coffee(1, "Kochere", "Ethiopa", 15.75, false)
val c2 = Coffee(2, "Dumo", "Costa Rica", 20.99, false)

forOrder.lt(c1, c2) // false since "Kochere" > "Dumo" and since id is not considered as part of ordering.
```

### Reorder fields when constructing an Order
Considering our `Coffee` case class from above, say that we would like to `Order` a `Coffee` using the fields `price`, `origin`, `name` in that specific order.

We can do this like:
```scala
// Note that our reordering musn't conatain _all of the_ fields of our case class, a subset will do.
type Reorder = CaseClassField["price", Double] :: CaseClassField["origin", String] :: CaseClassField["name": String] :: HNil
val forOrder: Order[Coffee] = reorderProduct[Coffee, Reorder]

val c1 = Coffee(1, "Kochere", "Ethiopa", 15.75, false)
val c2 = Coffee(2, "Dumo", "Costa Rica", 20.99, false)

forOrder.lt(c1, c2) // true since 15.75 < 20.99
```
