import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import org.webjars.WebJarAssetLocator

import scala.util.{Failure, Success, Try}

object WebJarsSupport {
  val webJarAssetLocator = new WebJarAssetLocator

  val rawWebJarPath = {
    extractUnmatchedPath { path =>
      Try(webJarAssetLocator.getFullPath(path.toString)) match {
        case Success(fullPath) =>
          encodeResponse {
            getFromResource(fullPath)
          }
        case Failure(_: IllegalArgumentException) =>
          reject
        case Failure(e) =>
          failWith(e)
      }
    }
  }

  val shortWebJarPath = {
    path(Segment / Segment) { (webjar, asset) =>
      Try(webJarAssetLocator.getFullPath(webjar, asset)) match {
        case Success(fullPath) =>
          encodeResponse {
            getFromResource(fullPath)
          }
        case Failure(_: IllegalArgumentException) =>
          reject
        case Failure(e) =>
          failWith(e)
      }
    }
  }
}

trait Service {
  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  val routes = {
    pathPrefix("js") {
      WebJarsSupport.shortWebJarPath
    } ~ path("") {
      encodeResponse {
        getFromResource("public/index.html")
      }
    }
  }
}

object AkkaHttpMicroservice extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
