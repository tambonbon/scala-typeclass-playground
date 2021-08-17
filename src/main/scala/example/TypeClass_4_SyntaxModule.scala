package example

import TypeClass_1.Eq

object TypeClass_4_SyntaxModule {

    /** 
     * Previously, `Eq[A].areEquals(a,b) is still verbose 
     * because we explicitly refer to type class instance..
     * .. which should be implicit
    */

    /** 
     * We're providing `syntax` or `ops` module by using IMPLICIT CONVERSION..
     * .. which allows us to extend API of some class without modifying its source code
    */

    implicit class EqSyntax[A: Eq](a: A) {
        def ===(b: A): Boolean = Eq[A].areEquals(a, b)
    }

    /** 
     * Remarks: Compiler converts: class `A` having instance of typeclass Eq[A] ---> class EqSyntax having 1 function ===
     * ===> We've added function === to class A WITHOUT SRC CODE MODIFICATION
     * 
     * Now we're allowed to apply method === whenever class EqSyntax is in scope
    */

    def pairEqualsSyntaxModule[A: Eq](a: A, b: A): Option[(A, A)] = {
        if (a === b) Some((a, b)) 
        else None
    }

    // Remarks: All technical aspects of typeclass are separated from our domain logic
    
}
