package io.masonedmison.ordershapeless

import cats.kernel.Order
import shapeless._
import shapeless.ops.coproduct.Align

object semiauto {

  def deriveCatsOrder[O <: Coproduct, Trait, TC <: Coproduct](
      implicit
      gen: Generic.Aux[Trait, TC],
      align: Align[TC, O]
  ): Order[Trait] = {
    val ltCoprod = new LtCoprod[O, TC, Trait] {}
    Order.fromLessThan(ltCoprod.apply(_, _))
  }
  def deriveOrdering[O <: Coproduct, Trait, TC <: Coproduct](
      implicit
      gen: Generic.Aux[Trait, TC],
      align: Align[TC, O]
  ): Ordering[Trait] = {
    val ltCoprod = new LtCoprod[O, TC, Trait] {}
    Ordering.fromLessThan(ltCoprod.apply(_, _))
  }

  private[semiauto] abstract class LtCoprod[OrderCoprod <: Coproduct, TraitCoprod <: Coproduct, Trait](
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

}
