package data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object RomInfoHelper {
    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class RomInfo(
        @SerialName("AuthResult") val authResult: Int? = null,
        @SerialName("CurrentRom") val currentRom: Rom? = null,
        @SerialName("LatestRom") val latestRom: Rom? = null,
        @SerialName("IncrementRom") val incrementRom: Rom? = null,
        @SerialName("CrossRom") val crossRom: Rom? = null,
        @SerialName("Icon") val icon: Map<String, String>? = null,
        @SerialName("FileMirror") val fileMirror: FileMirror? = null,
        @SerialName("GentleNotice") val gentleNotice: GentleNotice? = null,
        @SerialName("HeadImages") val headImages: Map<String, String>? = null,
        @SerialName("Log") val log: Log? = null
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class Rom(
        val bigversion: String? = null,
        val branch: String? = null,
        @Serializable(with = ChangelogMapSerializer::class)
        val changelog: HashMap<String, Changelog>? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val osbigversion: String? = null,
        val type: String? = null,
        val version: String? = null,
        val isBeta: Int = 0,
        val isGov: Int = 0,
    )

    @Serializable
    data class Changelog(
        val txt: List<ChangelogItem>,
    )

    @Serializable
    data class ChangelogItem(
        val txt: String? = null,
        val image: List<Image>? = null
    )

    /**
     * Custom serializer for changelog map that handles two different formats:
     * 1. Direct array format: "key": [{"txt": "...", "image": [...]}, ...]
     * 2. Object format: "key": {"txt": ["string1", "string2"]}
     */
    object ChangelogMapSerializer : KSerializer<HashMap<String, Changelog>?> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ChangelogMap")

        override fun deserialize(decoder: Decoder): HashMap<String, Changelog>? {
            val jsonDecoder = decoder as? JsonDecoder ?: return null
            val jsonElement = jsonDecoder.decodeJsonElement()
            
            if (jsonElement !is JsonObject) return null
            
            val result = HashMap<String, Changelog>()
            
            for ((key, value) in jsonElement) {
                try {
                    when (value) {
                        is JsonArray -> {
                            // Format 1: Direct array of ChangelogItem objects
                            val items = value.mapNotNull { item ->
                                try {
                                    if (item is JsonObject) {
                                        ChangelogItem(
                                            txt = item["txt"]?.jsonPrimitive?.content,
                                            image = item["image"]?.jsonArray?.mapNotNull { img ->
                                                try {
                                                    if (img is JsonObject) {
                                                        Image(
                                                            path = img["path"]?.jsonPrimitive?.content ?: "",
                                                            h = img["h"]?.jsonPrimitive?.content,
                                                            w = img["w"]?.jsonPrimitive?.content
                                                        )
                                                    } else null
                                                } catch (e: Exception) {
                                                    null
                                                }
                                            }
                                        )
                                    } else null
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            result[key] = Changelog(items)
                        }
                        is JsonObject -> {
                            // Format 2: Object with "txt" field containing array of strings
                            val txtArray = value["txt"]?.jsonArray
                            if (txtArray != null) {
                                val items = txtArray.mapNotNull { txtElement ->
                                    try {
                                        ChangelogItem(txt = txtElement.jsonPrimitive.content)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }
                                result[key] = Changelog(items)
                            }
                        }
                        else -> {
                            // Skip unknown formats
                        }
                    }
                } catch (e: Exception) {
                    // Skip entries that fail to parse
                }
            }
            
            return result
        }

        override fun serialize(encoder: Encoder, value: HashMap<String, Changelog>?) {
            // Not implemented as we only need deserialization
            throw UnsupportedOperationException("Serialization is not supported")
        }
    }

    @Serializable
    data class Image(
        val path: String,
        val h: String? = null,
        val w: String? = null
    )

    @Serializable
    data class FileMirror(
        val icon: String,
        val image: String,
        val video: String,
        val headimage: String,
    )

    @Serializable
    data class GentleNotice(
        val text: String,
    )

    @Serializable
    data class Log(
        val moduleImg: Map<String, Map<String, String>>? = null
    )
}