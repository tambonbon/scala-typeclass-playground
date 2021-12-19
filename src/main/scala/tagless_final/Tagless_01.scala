package tagless_final

import cats.Monad

object Tagless_01 {

  /** Tagless-final involves creating typeclasses.. .. which describe
    * capabilities of a generic effect `F[_]`
    */

  // typeclass describing console-related capabilities of some effect F[_]
  trait Console[F[_]] {
    def putStrLn(line: String): F[Unit]
    val getStrLn: F[String]
  }

  // typeclass allow us to create methods that are polymorphic in the effect type F[_]
  def consoleProgram[F[_]: Console]: F[Unit] =
    implicitly[Console[F]].putStrLn("Hello World!")

  def consoleProgram[F[_]: Console: Monad]: F[String] = {
    val console = implicitly[Console[F]]

    import console._

    for {
      _ <- putStrLn("What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, $name, good to meet you!")
    } yield name
  }
}
