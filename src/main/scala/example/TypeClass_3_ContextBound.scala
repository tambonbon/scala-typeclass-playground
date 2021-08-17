package example

import example.TypeClass_1.Eq
object TypeClass_3_ContextBound {

    /** 
     * We'd like to improve our code so that we can get rid of second argument list (implicit keyword)
     * 
     * ***Context Bound***  is declaration in type parameters list ..
     * .. which syntax `A : Eq` says that every type used as argument of `pairEquals` function.. 
     * .. must have implicit value of type Eq[A] in implicit scope
    */

    def pairEqualsImplicitly[A: Eq](a: A, b: A): Option[(A, A)] = { // no implicit argument ..
        if (implicitly[Eq[A]].areEquals(a, b)) Some((a, b)) 
        // .. use `implicitly[F[_]]` which pulls found implicit value by specifying which type we refer to
        else None
    }

    // Remarks: ContextBound is cool syntactic sugar but `implicitly` pollutes our code
    // Hacking: (after adding helper apply method in object Eq)

    def pairEqualsNoImplicitOrImplicitly[A: Eq](a: A, b: A): Option[(A, A)] = {
        if (Eq[A].areEquals(a, b)) Some ((a, b))
        else None
    }
}
