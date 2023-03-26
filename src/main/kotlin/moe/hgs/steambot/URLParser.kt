package moe.hgs.steambot

import java.net.URI

object URLParser {

    enum class SteamUrlType {
        WORKSHOP,
        STORE,
        UNKNOWN
    }

    data class ParsedUrl(
        val url: URI,
        val appId: String,
        val urlType: SteamUrlType
    )

    fun getGetParametersMap(query: String): Map<String, String> {
        return query.split("&").associate {
            val (left, right) = it.split("=")
            left to right
        }
    }

    /* steam workshop
        web:   https://steamcommunity.com/sharedfiles/filedetails/?id=485936923
        steam: steam://url/CommunityFilePage/485936923

        steam store
        web:     https://store.steampowered.com/app/1118310/RetroArch/
        open:    steam://store/<id>
        launch:  steam://run/<id>//<args>/
        install: steam://install/<id>

        add friend
        add: steam://friends/add/<id>
    */
    fun parseSteamUrl(uri: URI): ParsedUrl {
        val path = uri.path.split("/")
        when (uri.scheme) {
            "steam" -> {
                when (uri.host) {
                    "url" -> {
                        if (path.size > 2 && path[1] == "CommunityFilePage") {
                            return ParsedUrl(uri, path[2], SteamUrlType.WORKSHOP)
                        }
                    }

                    "store", "run", "install" -> {
                        if (path.size > 1) {
                            return ParsedUrl(uri, path[1], SteamUrlType.STORE)
                        }
                    }
                }
            }

            "https" -> {
                when (uri.host) {
                    "steamcommunity.com" -> {
                        if (path.size > 2 && (path[1] == "sharedfiles" || path[1] == "workshop") &&
                            path[2] == "filedetails"
                        ) {
                            val getParams = getGetParametersMap(uri.query)
                            getParams["id"]?.let {
                                return ParsedUrl(uri, it, SteamUrlType.WORKSHOP)
                            }
                        }
                    }

                    "store.steampowered.com" -> {
                        if (path.size > 2 && path[1] == "app") {
                            return ParsedUrl(uri, path[2], SteamUrlType.STORE)
                        }
                    }
                }
            }
        }
        return ParsedUrl(uri, "", SteamUrlType.UNKNOWN)
    }
}