package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import javax.websocket.OnMessage
import javax.websocket.server.ServerEndpoint

@SpringBootApplication
class Application

/**
 * Utility object that provides access to the context.
 */
object DI {
	lateinit var context: ApplicationContext
	fun register(applicationContext: ApplicationContext) {
		context = applicationContext
	}
	/**
	 * Here is the trick: this return a lazy bean request.
	 * That is, the bean will be retrieved the first time it is needed.
	 */
	inline fun <reified T> bean(): Lazy<T> {
		return lazy { context.getBean(T::class.java) ?: throw NullPointerException() }
	}
}

fun main(args: Array<String>) {
	DI.register(runApplication<Application>(*args))
}

@Configuration
@EnableWebSocket
class AppConfiguration {
	@Bean
	fun serverEndpointExporter(): ServerEndpointExporter = ServerEndpointExporter()
}

/**
 * The service that we want to inject.
 */
@Service
class GreetingService {
	fun greeting(msg: String) = "Hello $msg"
}

@Component
@ServerEndpoint("/ws")
class EchoServer {
	/**
	 * Injects the bean on demand!!!
	 */
	private val greetingService: GreetingService by DI.bean()

	@OnMessage
	fun echo(msg: String) = greetingService.greeting(msg)
}