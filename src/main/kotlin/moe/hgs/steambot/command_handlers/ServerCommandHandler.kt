package moe.hgs.steambot.command_handlers

import com.ibasco.agql.core.enums.RateLimitType
import com.ibasco.agql.core.util.FailsafeOptions
import com.ibasco.agql.core.util.GeneralOptions
import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.SourceQueryOptions
import com.ibasco.agql.protocols.valve.source.query.info.SourceServer
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent
import discord4j.core.`object`.command.ApplicationCommandInteractionOption
import discord4j.core.`object`.command.ApplicationCommandInteractionOptionValue
import discord4j.core.spec.EmbedCreateSpec
import org.reactivestreams.Publisher
import java.net.InetSocketAddress
import java.util.concurrent.Executors


class ServerCommandHandler : CommandHandler("sourceServerCommand.json") {

    override fun handleCommand(event: ChatInputInteractionEvent): Publisher<*> {
        val commandInteraction = event.interaction.commandInteraction.get()
        var address = commandInteraction.getOption("address_port")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")
        var port = commandInteraction.getOption("port")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asLong)
            .orElse(27015)
        val password = commandInteraction.getOption("password")
            .flatMap(ApplicationCommandInteractionOption::getValue)
            .map(ApplicationCommandInteractionOptionValue::asString)
            .orElse("")

        if(address.contains(":")){
            val split = address.split(":")
            address = split[0]
            port = split[1].toLong()
        }
        val serverInfo = getServerInfo(address, port.toInt())

        /* steam workshop
            connect to server:
            steam://connect/<IP or DNS name>[:<port>][/<password>]
        */
        var url = "steam://connect/$address:$port"
        if (password.isNotEmpty()) {
            url += "/$password"
        }

        serverInfo?: return event.reply(url)

        return event.reply()
            .withEmbeds(buildStoreEmbed(serverInfo, url))
    }

    fun getServerInfo(address:String, port:Int) : SourceServer? {
        val queryOptions = SourceQueryOptions.builder()
            .option(FailsafeOptions.FAILSAFE_RATELIMIT_TYPE, RateLimitType.BURST)
            .build()

        SourceQueryClient(queryOptions).use { client ->
            val address2 = InetSocketAddress(address, port)
            return client.getInfo(address2).join().result
        }
    }

    fun buildStoreEmbed(appDetails: SourceServer, connectUrl:String): EmbedCreateSpec {
        return EmbedCreateSpec.builder()
            .title(appDetails.name)
            .description("<$connectUrl>")
            .addField("Game", appDetails.gameDescription, true)
            .addField("Game version", appDetails.gameVersion, true)
            .addField("Current map", appDetails.mapName, true)
            .addField("Players", "${appDetails.numOfPlayers}/${appDetails.maxPlayers}", true)
            .addField("Uses password", "${appDetails.isPrivateServer}", true)
            .addField("VAC", "${appDetails.isSecure}", true).build()
    }
}