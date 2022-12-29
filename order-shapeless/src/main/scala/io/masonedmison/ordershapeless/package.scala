/*
 * Copyright 2022 io.github.masonedmison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
