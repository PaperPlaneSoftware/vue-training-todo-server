package com.example.http4sexp

import cats.effect._
import cats.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]) = {
    val httpApp = Router("/kanban" -> kanban.routes[IO]).orNotFound

    val port = sys.env.getOrElse("PORT", "5000").toInt

    BlazeServerBuilder[IO]
      .bindHttp(port, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
