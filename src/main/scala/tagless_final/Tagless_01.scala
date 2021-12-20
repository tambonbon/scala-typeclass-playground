package tagless_final

import cats.Monad
import cats.effect.IO
import cats.implicits._

// https://degoes.net/articles/tagless-horror
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

  // val ioProgram = consoleProgram[IO]
}
