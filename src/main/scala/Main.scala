import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import api.Api
import api.Api.Info
import monix.cats._
import monix.eval.Task
import monix.execution.Scheduler
import services.{CountryServiceInterpreter, WeatherServiceInterpreter}

import scala.util.Try

object Main extends App {
  Try(args(0)).foreach(System.setProperty("config.file", _))

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import util.TwirlSupport._
  import io.circe.syntax._
  import config.AppConfig.config

  if (!config.logToFile) {
    System.setProperty("logback.configurationFile", "logback.stdout.xml")
  }

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val scheduler = Scheduler(system.dispatcher)
  private val log = Logging(system, this.getClass)
  private val extPattern = """(.*)[.](.*)""".r

  implicit val weatherService = new WeatherServiceInterpreter
  implicit val countryService = new CountryServiceInterpreter
  val api = new Api()


  val programT = (city: String) =>
    api.program[Task](city).runAsync.map {
      case Right(d) =>
        StatusCodes.OK -> d.asJson

      case Left(err) =>
        StatusCodes.BadRequest -> err.msg.asJson
    }

  val htmlProgramT = (city: String) =>
    api.program[Task](city).runAsync.map {
      case Right(d) => d

      case Left(err) =>
        log.error(err.msg)
        Info(city, None, None)
    }

  val route =
    pathPrefix("api") {
      get {
        pathEndOrSingleSlash {
          complete(programT(config.defaultCity))
        } ~
          path(Segment) { city =>
            complete(programT(city))
          }
      }
    } ~
      pathPrefix("elm") {
        pathEnd {
          val page = Paths.get("frontend/elm/index.html")
          val byteArray = Files.readAllBytes(page)
          complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, byteArray)))
        } ~
        path(Segment) { resource =>
          val res = Paths.get(s"frontend/elm/$resource")
          if (Files.exists(res) && !Files.isDirectory(res)) {
            val ext = res.getFileName.toString match {
              case extPattern(_, extGroup) => extGroup
              case _ => ""
            }
            val byteArray = Files.readAllBytes(res)
            complete(HttpResponse(
              StatusCodes.OK,
              entity = HttpEntity(ContentType(MediaTypes.forExtension(ext), () => HttpCharsets.`UTF-8`), byteArray)
            ))
          }
          else {
            complete(HttpResponse(StatusCodes.NotFound, entity = "w00t"))
          }
        }
      } ~
      get {
        pathEndOrSingleSlash {
          val page = htmlProgramT(config.defaultCity).map(html.main.render)
          complete(page)
        } ~
          path(Segment) { city =>
            val page = htmlProgramT(city).map(html.main.render)
            complete(page)
          }
      }

  private val bindingFuture = Http()
    .bindAndHandle(route, config.http.interface, config.http.port)
  log.info(s"Server up at $config")

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete(_ => system.terminate())
    }
  })
}
