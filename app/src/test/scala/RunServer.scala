import mu.node.echod.EchodModule
import mu.node.echod.util.FileUtils
import org.slf4j.LoggerFactory

object RunServer extends App with FileUtils with EchodModule {

  LoggerFactory.getLogger(this.getClass).info("Starting gRPC server")
  echoServer.start().awaitTermination()
  System.in.read()
}



