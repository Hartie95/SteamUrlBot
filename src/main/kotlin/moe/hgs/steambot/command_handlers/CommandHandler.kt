package moe.hgs.steambot.command_handlers

import discord4j.common.JacksonResources
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.discordjson.json.ApplicationCommandRequest
import org.reactivestreams.Publisher

abstract class CommandHandler(commandResourceName:String) {
    val commandRequest : ApplicationCommandRequest
    init {
        this.commandRequest = loadCommandRequestResource(commandResourceName)
    }

    abstract fun handleCommand(event: ChatInputInteractionEvent): Publisher<*>

    fun loadCommandRequestResource(fileName:String) : ApplicationCommandRequest{
        val json = ClassLoader.getSystemClassLoader().getResource("commands/$fileName")?.readText()?:throw Exception("Resource not found")
        return d4jMapper.objectMapper
            .readValue(json, ApplicationCommandRequest::class.java);
    }

    companion object {
        val d4jMapper = JacksonResources.create()
    }

}