package io.masonedmison.ordershapeless

import scala.reflect.macros.blackbox
import shapeless.{ Coproduct, HList }
import cats.Order

private[ordershapeless] object macros {

  class OrderMacros(val c: blackbox.Context) {
    import c.universe._
    final def excludingImpl[Clz, Exclude <: HList](
        implicit
        Clz: c.WeakTypeTag[Clz],
        Exclude: c.WeakTypeTag[Exclude]
    ): c.Expr[Order[Clz]] = {
      val gen   = c.freshName(TermName("gen"))
      val diff  = c.freshName(TermName("diff"))
      val order = c.freshName(TermName("order"))
      // TODO improve error messages
      val tree =
        q"""
          val $gen = _root_.shapeless.LabelledGeneric[$Clz]
          val _ = _root_.shapeless.ops.hlist.RemoveAll[$gen.Repr, $Exclude]
          val $diff = _root_.shapeless.ops.hlist.Diff[$gen.Repr, $Exclude]
          implicitly[$diff.Out =:!= _root_.shapeless.HNil]
          val $order = _root_.cats.kernel.Order[$diff.Out]
          _root_.cats.kernel.Order.fromLessThan[$Clz] { case (a, b) =>
            val x = $diff.apply($gen.to(a))
            val y = $diff.apply($gen.to(b))
            $order.lt(x, y)      
          }
        """
      c.Expr[Order[Clz]](tree)
    }

    final def orderBy[Clz, O <: Coproduct](
        implicit
        Clz: c.WeakTypeTag[Clz],
        O: c.WeakTypeTag[O]
    ): c.Expr[Order[Clz]] = {
      val gen      = c.freshName(TermName("gen"))
      val ltCoprod = c.freshName(TermName("ltCoprod"))
      c.Expr[Order[Clz]](
        q"""
          val $gen = _root_.shapeless.Generic[$Clz]
          val $ltCoprod = new _root_.io.masonedmison.ordershapeless.LtCoprod[$O, $gen.Repr, $Clz] {}
          _root_.cats.kernel.Order.fromLessThan[$Clz]($ltCoprod.apply(_, _))
          """
      )
    }

    final def reorderProduct[Clz, Reorder <: HList](
        implicit
        Clz: c.WeakTypeTag[Clz],
        Reorder: c.WeakTypeTag[Reorder]
    ): c.Expr[Order[Clz]] = {
      val gen       = c.freshName(TermName("gen"))
      val intersect = c.freshName(TermName("intersect"))
      val align     = c.freshName(TermName("align"))
      val order     = c.freshName(TermName("order"))
      c.Expr[Order[Clz]](
        q"""
          val $gen = _root_.shapeless.LabelledGeneric[$Clz]
          val $intersect = _root_.shapeless.ops.hlist.Intersection[$gen.Repr, $Reorder]
          val $align = _root_.shapeless.ops.hlist.Align[$intersect.Out, $Reorder]
          val $order = _root_.cats.kernel.Order[$Reorder]
          _root_.cats.kernel.Order.fromLessThan[$Clz] { case (l, r) =>
            val x = $align.apply($intersect.apply($gen.to(l)))
            val y = $align.apply($intersect.apply($gen.to(r)))
            $order.lt(x, y)
          }
        """
      )
    }
  }
  object OrderMacros {
    def excludingError(clz: String, exclude: String) =
      s"""Unable to generate Order instance for class: $clz and exclude fields: $exclude.
      |Ensure that the following are true:
      | * Clz is a case class
      | * Exclude comprises only `CaseClassField`s that a present in the case class `Clz`
      | * That you have `ordershapeless.implicits._` in scope.  
      """.stripMargin

  }
}
