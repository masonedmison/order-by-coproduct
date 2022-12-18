package io.masonedmison.ordershapeless
import cats.kernel.Order

object implicits {
  implicit def orderForTagged[Name <: String, T](
      implicit
      order: Order[T]
  ): Order[CaseClassField[Name, T]] =
    Order.fromLessThan[CaseClassField[Name, T]](order.lt _)
}
