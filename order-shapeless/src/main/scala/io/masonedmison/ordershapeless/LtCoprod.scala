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

package io.masonedmison.ordershapeless

import shapeless._
import shapeless.ops.coproduct.Align

abstract class LtCoprod[OrderCoprod <: Coproduct, TraitCoprod <: Coproduct, Trait](
    implicit
    gen: Generic.Aux[Trait, TraitCoprod],
    align: Align[TraitCoprod, OrderCoprod]
) {
  private def getIdx(c: Coproduct) = {
    @scala.annotation.tailrec
    def go(rem: Coproduct, acc: Int): Int = rem match {
      case Inl(_)    => acc
      case Inr(tail) => go(tail, acc + 1)
    }
    go(c, 0)
  }
  def apply(l: Trait, r: Trait): Boolean = {
    val x = align.apply(gen.to(l))
    val y = align.apply(gen.to(r))
    getIdx(x) < getIdx(y)
  }
}
