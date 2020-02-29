package server

import cats.effect._
import io.circe.syntax._
import io.circe.generic.semiauto._
import io.getquill.{PostgresJdbcContext => PgCtx, _}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._
import org.slf4j.LoggerFactory

object Todo {
  val logger = LoggerFactory.getLogger(Todo.getClass());

  object Implicits {
    import Models._
    implicit val todoEncoder = deriveEncoder[Todo]
    implicit val todoDecoder = deriveDecoder[Todo]

    implicit val pagedResultEncoder = deriveEncoder[PagedResult]
    implicit val pagedResultDecoder = deriveDecoder[PagedResult]
  }

  object Models {
    case class Todo(
        who: String,
        task: String,
        complete: Boolean,
        id: Option[Int] = None
    )

    case class PagedResult(total: Int, todos: List[Todo])
  }

  object Services {
    import Db.Implicits.ctx
    import ctx.{IO => _, _}
    import Models._

    implicit val todoInsertMeta = insertMeta[Todo](_.id)

    def getAll: List[Todo] = ctx.run(query[Todo])

    def getPage(page: Int, search: Option[String]): List[Todo] = {
      val pageQuery = search match {
        case None =>
          quote {
            query[Todo].drop(10 * lift(page)).take(10)
          }

        case Some(search) =>
          quote {
            query[Todo]
              .filter(_.task like s"%${lift(search)}%")
              .drop(10 * lift(page))
              .take(10)
          }
      }

      ctx.run(pageQuery)
    }

    def get(id: Int): Option[Todo] =
      ctx
        .run(quote(query[Todo].filter(_.id == lift(Option(id))).take(1)))
        .headOption

    def insert(todo: Todo): Todo =
      ctx.run(query[Todo].insert(lift(todo)).returning(todo => todo))

    def update(todo: Todo): Unit =
      ctx.run(query[Todo].update(lift(todo)))

    def delete(id: Int): Unit =
      ctx.run(quote(query[Todo].filter(_.id == lift(Option(id))).delete))
  }

  def routes() = {
    import Implicits._
    import Models._
    import QueryParamDecoder._

    object SearchQP extends OptionalQueryParamDecoderMatcher[String]("search")

    HttpRoutes
      .of[IO] {
        case GET -> Root / "all"      => Ok(Services.getAll.asJson)
        case GET -> Root / IntVar(id) => Ok(Services.get(id).asJson)
        case GET -> Root / "page" / IntVar(page) :? SearchQP(search) =>
          Ok(Services.getPage(page, search).asJson)
        case req @ POST -> Root =>
          for {
            todo <- req.as[Todo]
            _    <- cats.effect.IO.pure(Services.insert(todo))
            res  <- Ok()
          } yield (res)
        case req @ PUT -> Root =>
          for {
            todo <- req.as[Todo]
            _    <- cats.effect.IO.pure(Services.update(todo))
            res  <- Ok()
          } yield (res)
        case DELETE -> Root / IntVar(id) => Ok(Services.delete(id))
      }
  }
}
