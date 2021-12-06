# Injecting Beans on a WebSocket ServerEndpoint 

The support of `javax.webscket.server.ServerEndpoint` in Spring Framework is too limited because dependency 
injection is not supported. This is not an issue as Spring Frameworks promotes their own solution.

However, imagine that you really need to use the `javax.websocket.server` default library in a Spring app 
with the support of dependency injection. 

Do not despair. Below is a nice Kotlin trick that can help you:

```kotlin
object DI {
    lateinit var context: ApplicationContext
    fun register(applicationContext: ApplicationContext) {
        context = applicationContext
    }
    inline fun <reified T> bean(): Lazy<T> {
        return lazy { context.getBean(T::class.java) ?: throw NullPointerException() }
    }
}
```

`DI` can be seen as a lightweight facade that provides a clean and lazy access to the Spring application context.
You only needs to register the `ApplicationContext` as follows:

```kotlin
fun main(args: Array<String>) {
	DI.register(runApplication<Application>(*args))
}
```

Hence, the following works as expected:

```kotlin
@Configuration
@EnableWebSocket
class AppConfiguration {
    @Bean
    fun serverEndpointExporter(): ServerEndpointExporter = ServerEndpointExporter()
}

@Component
@ServerEndpoint("/ws")
class EchoServer  {
	private val greetingService: GreetingService by DI.bean()
	@OnMessage
	fun echo(msg: String) = greetingService.greeting(msg)
}
```

With the above code, the `greetingService` bean is injected on demand the first time `echo` is called.
