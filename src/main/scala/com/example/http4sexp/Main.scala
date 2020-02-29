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

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
