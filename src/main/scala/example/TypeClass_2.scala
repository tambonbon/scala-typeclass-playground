package example

import TypeClass_1.Eq

object TypeClass_2 {
 
    def pairEquals[A](a: A, b: A)(implicit eq: Eq[A]): Option[(A, A)] = {
        if(eq.areEquals(a,b)) Some((a,b))
        else None
    } 

    def main(args: Array[String]): Unit = {
        
    }
}
