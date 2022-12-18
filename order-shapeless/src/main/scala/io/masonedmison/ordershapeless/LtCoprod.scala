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
