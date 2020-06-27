package lechuck.kakaopay

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Log {
    fun logger(): Logger = LoggerFactory.getLogger(this.javaClass)
}
