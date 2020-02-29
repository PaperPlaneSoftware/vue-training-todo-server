package com.example.http4sexp

import cats.effect._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.getquill._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

object Todo {
  object Implicits {
    implicit val taskEncoder = deriveEncoder[Models.Task]
  }

  object Models {
    case class Task(id: Int, todo: String)
  }

  object Services {}

  def routes[F[_]: Sync](implicit ctx: PostgresJdbcContext[SnakeCase.type]) = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    import ctx._
    import Implicits._

    HttpRoutes.of[F] {
      case GET -> Root / "todo" / "all" =>
        val tasks = ctx.runIO(query[Models.Task])

        Ok(performIO(tasks).asJson)
    }
  }
}
