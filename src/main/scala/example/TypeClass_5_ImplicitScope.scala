package example

object TypeClass_5_ImplicitScope {
  
    /** 
     * Implicit scope is a scope where compiler searches for implicit instances
     * 
     * Order:
     * 1. Local & inherited instances
     * 2. Imported instances
     * 3. Definitions from companion object of type clas or params
     * 
     * Most convenient way of getting instances of typeclass: place them in companion object of typeclass itself
    */

    // example:
    abstract class Sort[A, B] {
        def sorted[B >: A](implicit ord: math.Ordering[B]): List[A]
    }

    /** 
     * Typeclass instance will be searched in:
     * 1. Ordering companion object
     * 2. List companion object
     * B companion object (which can also be A companion object because of lower bounds)
    */
}
