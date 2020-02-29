package com.example.http4sexp

import com.typesafe.config.{Config, ConfigFactory}
import cats.effect._
import cats.implicits._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  def run(args: List[String]) = {
    val config = ConfigFactory.load()
    val port = config.getInt("app.port")
    val host = config.getString("app.host")

    val httpApp = Router("/kanban" -> kanban.routes[IO]).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
