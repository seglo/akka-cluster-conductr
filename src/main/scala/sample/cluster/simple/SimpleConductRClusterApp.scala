package sample.cluster.simple

import language.postfixOps
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import com.typesafe.conductr.bundlelib.akka.{Env, StatusService}
import com.typesafe.conductr.lib.akka.ConnectionContext
import com.typesafe.config.ConfigFactory
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.util.Success

object SimpleConductRClusterApp {
  case object Ready

  def main(args: Array[String]): Unit = {
    startup()
  }

  def startup(): Unit = {
    val systemName = Env.mkSystemName("ClusterSystem")

    /**
      * Let ConductR get first dibs at overriding config (i.e. for network info)
      */
    val config = Env.asConfig(systemName)
      .withFallback(ConfigFactory.load("conductr.conf"))

    // Create an Akka system
    implicit val system = ActorSystem(systemName, config)
    implicit val log = Logging(system, "startup")
    /**
      * Define ConductR's akka ConnectionContext which takes implicit ActorRefFactory (ActorSystem)
      */
    implicit val cc = ConnectionContext()

    val instanceId = scala.util.Random.alphanumeric.take(5).mkString

    log.info("Starting instance {}", instanceId)

    /**
      * Create an actor that handles cluster domain events
      */
    val simpleClusterListener = system.actorOf(Props[SimpleClusterListener], name = "clusterListener")

    /**
      * Some ceremony to decide when to signal ConductR that the app is initialized
      */
    import system.dispatcher
    implicit val timeout = Timeout(5 seconds)
    (simpleClusterListener ? Ready) onComplete {
      /**
        * Signal ConductR we loaded successfully.
        */
      case Success(_: akka.Done) =>

        StatusService.signalStartedOrExit()
        log.info("ConductR signalled")
      case _ =>
    }
  }

}

