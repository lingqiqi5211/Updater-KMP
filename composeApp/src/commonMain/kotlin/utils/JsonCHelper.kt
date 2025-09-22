package utils

import kotlinx.serialization.json.Json

/**
 * JsonC (JSON with Comments) Helper
 * 
 * Provides a centralized JSON configuration that is more lenient and tolerant
 * compared to strict JSON parsing. This helps avoid parsing failures from
 * malformed JSON responses that might include comments, trailing commas,
 * or other non-standard JSON elements.
 */
object JsonCHelper {
    
    /**
     * Lenient JSON configuration that mimics JSONC behavior:
     * - Ignores unknown keys
     * - Allows coercing input values
     * - Ignores case mismatches
     * - More tolerant of malformed JSON
     */
    val jsonc = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        allowStructuredMapKeys = true
        allowSpecialFloatingPointValues = true
        useAlternativeNames = false
    }
    
    /**
     * Preprocesses JSON string to remove common JSONC elements:
     * - Single line comments (//)
     * - Multi-line comments (/* */)
     * - Trailing commas
     */
    fun preprocessJsonC(jsonString: String): String {
        return jsonString
            // Remove single-line comments
            .replace(Regex("//.*?(?=\\n|$)"), "")
            // Remove multi-line comments
            .replace(Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL), "")
            // Remove trailing commas before closing braces/brackets
            .replace(Regex(",\\s*([}\\]])"), "$1")
            // Clean up extra whitespace
            .replace(Regex("\\s+"), " ")
            .trim()
    }
    
    /**
     * Safe decode from JSONC string with preprocessing
     */
    inline fun <reified T> decodeFromJsonC(jsonString: String): T {
        val processedJson = preprocessJsonC(jsonString)
        return jsonc.decodeFromString<T>(processedJson)
    }
    
    /**
     * Safe encode to JSON string
     */
    inline fun <reified T> encodeToJsonC(value: T): String {
        return jsonc.encodeToString(value)
    }
}