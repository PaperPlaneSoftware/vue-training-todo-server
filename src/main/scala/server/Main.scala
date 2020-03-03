package server

import com.typesafe.config.{Config, ConfigFactory}
import cats.effect._
import cats.implicits._
import io.getquill._
import org.http4s.server.blaze._
import org.http4s.server.middleware._
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

    val methodConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = true,
      allowedMethods = Some(Set("GET", "POST", "DELETE", "PUT")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )
    val todoController = CORS(Todo.routes())

    val httpApp = Router("/todo" -> todoController).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
