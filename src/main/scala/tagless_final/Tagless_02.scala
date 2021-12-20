package tagless_final

import cats.effect.IO
import cats.data.State
import cats.Monad
import cats.implicits._

object Tagless_02 {

  case class Product(id: String, descr: String)
  case class ShoppingCart(id: String, products: List[Product])

  trait DomainModel_01 {
    // those functions perform some side effects
    def create(id: String): Unit
    def find(id: String): Option[ShoppingCart]
    def add(sc: ShoppingCart, product: Product): ShoppingCart
  }

  trait DomainModel_02 {
    // those functions produces side effects INSIDE SOME DECLARATIVE CONTEXTS
    // such contexts are called EFFECTS
    // Benefits:
    // - The description of the side effect is separated from its execution
    // - Continue to reason about our programs as pure functions
    def create(id: String): IO[Unit]
    def find(id: String): IO[Option[ShoppingCart]]
    def add(sc: ShoppingCart, product: Product): IO[ShoppingCart]
    // Problems:
    // - How to deal with the effects during testing? (not so obvious)
    // - How to implement ShoppingCart?
    //    - should we use a single concrete class?
    //    - should we use an abstract trait and separate implementations?
    // Enter *****Tagless Final pattern*****
  }

  /** Pattern's main aim: to use interfaces AS MUCH AS POSSIBLE
    */
  object Algebras {
    // Algebras represents the DSL specific to the domain we want to model
    // Algebras should be purely abstract
    // -> in DomainModel_02, for making ALGEBRAS we cannot refer directly to the IO type...
    // .. as it is a concrete implementation of an effect
    // ---> use HKT to make definitions abstract again
    trait ShoppingCarts[F[_]] { // F[_]: any generic type having a SINGLE type param (i.e. Either[String,T], IO[T])
      def create(id: String): F[Unit]
      def find(id: String): F[Option[ShoppingCart]]
      def add(sc: ShoppingCart, product: Product): F[ShoppingCart]
    }

    object ShoppingCarts {
      def apply[F[_]](implicit sc: ShoppingCarts[F]): ShoppingCarts[F] = sc
    }
  }

  object Intepreters {
    import Algebras._
    // Implement some concrete behaviours -> need interpreters
    // Interpreters: define how an algebra should behave on its term (the inputs/outputs of its function)

    // ***An algebra has at least 2 interpreters: a production and test interpreter***
    // test interpreters:
    type ShoppingCartRepo = Map[String, ShoppingCart] // persistent layer
    type ScRepoState[A] = State[ShoppingCartRepo, A] // functional flavor
    // ScRepoState[A]: a generic state transition from an instance of ShoppingCartRepo to another..
    // .. eventually producing a value of type A
    // It's equivalent to a function with type ShoppingCartRepo -> (ShoppingCartRepo, A)

    implicit object TestShoppingCartInterpreter
        extends ShoppingCarts[ScRepoState] {
      override def create(id: String): ScRepoState[Unit] =
        State.modify { carts =>
          val shoppingCart = ShoppingCart(id, List())
          carts + (id -> shoppingCart)
        }
      override def find(id: String): ScRepoState[Option[ShoppingCart]] =
        State.inspect { carts =>
          carts.get(id)
        }
      override def add(
          sc: ShoppingCart,
          product: Product
      ): ScRepoState[ShoppingCart] =
        State { carts =>
          val products = sc.products
          val updatedCart = sc.copy(products = product :: products)
          (carts + (sc.id -> updatedCart), updatedCart)
        }
    }
  }

  object Programs {
    import Intepreters._
    import Algebras._
    // Programs: the clients of algebras
    // A program is a piece of code that uses algebras and intepreters to implement business logic
    def createAndAddToCartGeneral[F[_]: Monad] = ???
    // Look at the above type parameter...
    // ... seems familiar? That's CONTEXT BOUND! That's the least bound of the type param

    // In order to retrieve an instance of a concrete interpreter..
    // .. --> 2 ways: implicit object resolution, and smart constructors

    object ImplicitObjectResolution {
      def createAndAddToCart[F[_]: Monad](product: Product, cartId: String)(
          implicit shoppingCarts: ShoppingCarts[F]
      ): F[Option[ShoppingCart]] =
        for {
          _ <- shoppingCarts.create(cartId)
          maybeSc <- shoppingCarts.find(cartId)
          maybeNewSc <- maybeSc.traverse(sc => shoppingCarts.add(sc, product))
        } yield maybeNewSc

      // another way is to refer directly to the algebra using the construct ShoopingCarts[F]
      def createAndToCart[
          F[_]: Monad: ShoppingCarts
      ]( // this adds an implicit param to function
          product: Product,
          cartId: String
      ): F[Option[ShoppingCart]] =
        for {
          _ <- ShoppingCarts[F].create(cartId)
          maybeSc <- ShoppingCarts[F].find(cartId)
          maybeNewSc <- maybeSc.traverse(sc =>
            ShoppingCarts[F].add(sc, product)
          )
        } yield maybeNewSc
    }

    object SmartConstructor {
      // 1st, we need our interpreter to become a class, not an Object
      class ShoppingCartsInterpreter private (repo: ShoppingCartRepo)
          extends ShoppingCarts[ScRepoState] {
        def add(sc: ShoppingCart, product: Product): ScRepoState[ShoppingCart] =
          ???
        def create(id: String): ScRepoState[Unit] = ???
        def find(id: String): ScRepoState[Option[ShoppingCart]] = ???
      }

      object ShoppingCartsInterpreter {
        def make(): ShoppingCartsInterpreter = {
          new ShoppingCartsInterpreter(repository)
        }
        private val repository: ShoppingCartRepo = Map()
      }

      case class ProgramWithDep[F[_]: Monad](carts: ShoppingCarts[F]) {
        def createAndToCart(
            product: Product,
            cartId: String
        ): F[Option[ShoppingCart]] = {
          for {
            _ <- carts.create(cartId)
            maybeSc <- carts.find(cartId)
            maybeNewSc <- maybeSc.traverse(sc => carts.add(sc, product))
          } yield maybeNewSc
        }
      }

      val program: ProgramWithDep[ScRepoState] = ProgramWithDep {
        ShoppingCartsInterpreter.make()
      }
      program.createAndToCart(Product("id", "a product"), "cart1")
    }

  }

  def main(args: Array[String]): Unit = {
    // Programs.ImplicitObjectResolution
    //   .createAndAddToCart(Product("butter", "milk"), "rack")
    // Programs.ImplicitObjectResolution.createAndToCart(
    //   Product("butter", "milk"),
    //   "rack"
    // )
  }
}
