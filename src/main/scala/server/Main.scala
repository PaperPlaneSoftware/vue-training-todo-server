package server

import com.typesafe.config.{Config, ConfigFactory}
import cats.effect._
import cats.implicits._
import io.getquill._
import org.http4s.server.blaze._
import org.http4s.server.Router
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.global

object Db {
  object Implicits {
    implicit val ctx = new PostgresJdbcContext(SnakeCase, "db")
  }
}

object Main extends IOApp {
  import Db.Implicits._

  def run(args: List[String]) = {
    val config = ConfigFactory.load()
    val port   = config.getInt("app.port")
    val host   = config.getString("app.host")

    val httpApp = Router("/todo" -> Todo.routes()).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
