package example

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

object TypeClass_1 extends AnyFlatSpec with Matchers {
  
    /**
     * Type class is Haskell way of polymorphism, or so-called ad-hoc polymorphism
     *      in OOP, it's called sub-type polymorphism
     * We can extend some functionality of library, even without having access to source code of library
    */

    
    /**
     * Type class is just a parameterized `trait` with number of abstract methods ..
     * .. that can be implemented in classes that extend that trait
     * 
     * * Subtype poly: we need to implement contract in classes that are a piece of domain model
     * * adhoc poly: we need to implement contract in classes that are placed in a completely different class ..
     * .. which is linked to "domain class" by type parameter
     */
    
    trait Eq[A] { // this is a TypeClass
        def areEquals(a: A, b: A): Boolean
    }

    /**
     * Type class Eq[A] is a contract of having an ability to check if 2 objects of type A are equal ..
     * .. based on some criteria implemented in areEquals method
     * 
     * Create instance of our TypeClass is to instantiate class extending mentioned `trait` (here `Eq[A]`) ..
     * .. with only difference that our TypeClass instance will be accessible as IMPLICIT OBJECTS
     */

    def moduloEqLong(divisor: Int): Eq[Int] = new Eq[Int] {
         override def areEquals(a: Int, b: Int): Boolean = a % divisor == b % divisor
    }

    implicit val modulo5EqLong: Eq[Int] = moduloEqLong(5)

    // can be shorter
    def moduloEq5Short: Eq[Int] = (a: Int, b: Int) => a % 5 == b % 5
    // this can be done because trait Eq[A] only has 1 abstract method (FI)

    // **********
    // TypeClass Resolution
    // **********

    /** 
     * How to use TypeClass instance and how to magically bind together typeclass Eq[A] ..
     * .. with correspoding object of type A 
    */

    def pairEquals[A](a: A, b: A)(implicit eq: Eq[A]): Option[(A, A)] = { 
        // pairEquals is parameterized 
        // --> work with any types..
        // .. provided instance of class `Eq[A] available in its implicit scope
        if(eq.areEquals(a,b)) Some((a,b))
        else None
    }

    /* 
    When compiler won’t find any instance that matches to the above declaration
    .. it will end up with compilation error warning about lack of proper instance in provided implicit scope. 
    Like in TypeClass_2
    */

    /** 
     * 1. Compiler will infer type of provided parameters by applying arguments to our function and assign it to alias `A`
     * 2. Preceding argument `eq: Eq[A]` with `implicit` ----> trigger suggestion to look for object to type `Eq[A]` in implicit scope
    */

    def main(args: Array[String]): Unit = {
        pairEquals(2,3) mustBe(None)
        pairEquals(2,2) mustBe(Some(2,2))
        // pairEquals(2, 7.2) mustBe(None)
    }

    // Hacking for ContextBound with no more implicitly
    object Eq {
        def apply[A](implicit eq: Eq[A]): Eq[A] = eq
    }
}
