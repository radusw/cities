import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import api.Api
import api.Api.Info
import io.circe.syntax._
import monix.cats._
import monix.eval.Task
import monix.execution.Scheduler
import services.{CountryServiceInterpreter, WeatherServiceInterpreter}

object Programs {
  implicit val clientSystem: ActorSystem = ActorSystem("client")
  implicit val clientMat: ActorMaterializer = ActorMaterializer()
  private val log = Logging(clientSystem, this.getClass)

  implicit val weatherService = new WeatherServiceInterpreter
  implicit val countryService = new CountryServiceInterpreter

  implicit val blockingOpsScheduler = Scheduler.io()
  
  val api = new Api()
  val programT = (city: String) =>
    api.program[Task](city).runAsync
      .map {
        case Right(d) =>
          StatusCodes.OK -> d.asJson
        case Left(err) =>
          StatusCodes.BadRequest -> err.msg.asJson
      }
  val htmlProgramT = (city: String) =>
    api.program[Task](city).runAsync
      .map {
        case Right(d) => d
        case Left(err) =>
          log.error(err.msg)
          Info(city, None, None)
      }
      .map(html.main.render)
}
