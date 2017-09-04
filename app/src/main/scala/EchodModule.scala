package mu.node.echod

import com.typesafe.config.ConfigFactory
import mu.node.echod.util.{FileUtils, KeyUtils}
import mu.node.echod.grpc.{EchoServer, UserContextServerInterceptor}
import mu.node.echod.services.EchoService

/*
 * Application dependencies
 */
trait EchodModule extends KeyUtils with FileUtils {
  lazy val config = ConfigFactory.load()
  lazy val echoService = new EchoService
  lazy val jwtVerificationKey = loadX509PublicKey(fileForTestResourcePath(config.getString("jwt.signature-verification-key")).getAbsolutePath)

  lazy val userContextServerInterceptor = new UserContextServerInterceptor(jwtVerificationKey)
  lazy val echoServer = EchoServer.build(config, echoService, userContextServerInterceptor,fileForTestResourcePath)
}
