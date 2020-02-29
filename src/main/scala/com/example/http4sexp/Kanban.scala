package com.example.http4sexp

import cats.effect._
import io.circe.syntax._
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._

object kanban {
  object models {
    case class Task(title: String, text: String)
  }

  object implicits {
    implicit val taskEncoder = deriveEncoder[models.Task]
  }

  object services {}

  def routes[F[_]: Sync] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    import implicits._

    HttpRoutes.of[F] {
      case GET -> Root / "all" => Ok(models.Task("", "").asJson)
    }
  }
}
