import java.nio.file.{Files, Paths}

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import scala.util.Try

object Main extends App {
  Try(args(0)).foreach(System.setProperty("config.file", _))
  import config.AppConfig.config
  if (!config.logToFile) {
    System.setProperty("logback.configurationFile", "logback.stdout.xml")
  }

  val route = {
    import Programs.{programT, htmlProgramT}

    pathPrefix("api") {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
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
        val extPattern = """(.*)[.](.*)""".r
        val elmResDir = Paths.get(s"frontend/elm/")

        pathEnd {
          val page = elmResDir.resolve("index.html")
          val byteArray = Files.readAllBytes(page)
          complete(HttpResponse(StatusCodes.OK, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, byteArray)))
        } ~
          path(Segment) { resource =>
            val res = elmResDir.resolve(resource)
            if (res.getParent == elmResDir && Files.exists(res) && !Files.isDirectory(res)) {
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
        import util.TwirlSupport._

        pathEndOrSingleSlash {
          val page = htmlProgramT(config.defaultCity)
          complete(page)
        } ~
          path(Segment) { city =>
            val page = htmlProgramT(city)
            complete(page)
          }
      }
  }

  implicit val appSystem: ActorSystem = ActorSystem("app")
  implicit val appMat: ActorMaterializer = ActorMaterializer()
  private val log = Logging(appSystem, this.getClass)

  Http().bindAndHandle(Route.seal(route), config.http.interface, config.http.port)
  log.info(s"Server up at $config")
}
