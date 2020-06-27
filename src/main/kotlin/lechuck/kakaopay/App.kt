package lechuck.kakaopay

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["lechuck.kakaopay"])
class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
