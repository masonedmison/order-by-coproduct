package io.masonedmison

import shapeless.{ Coproduct, HList }
import cats.kernel.Order
import shapeless.labelled.FieldType
import shapeless.tag.Tagged

package object ordershapeless {
  import ordershapeless.macros.OrderMacros

  def exclude[Clz, Exclude <: HList]: Order[Clz] =
    macro OrderMacros.excludingImpl[Clz, Exclude]

  def orderByCoproduct[O <: Coproduct, Trait]: Order[Trait] =
    macro OrderMacros.orderBy[Trait, O]

  def reorderProduct[Clz, Reorder <: HList]: Order[Clz] =
    macro OrderMacros.reorderProduct[Clz, Reorder]

  type CaseClassField[Name <: String, T] = FieldType[Symbol with Tagged[Name], T]
}
