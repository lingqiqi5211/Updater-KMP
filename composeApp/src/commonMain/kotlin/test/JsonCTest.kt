package test

import utils.JsonCHelper

/**
 * Test class to validate JsonCHelper functionality
 * This demonstrates the improved JSON parsing tolerance
 */
object JsonCTest {
    
    fun testJsonCParsing() {
        // Test 1: JSON with single-line comments
        val jsonWithComments = """
        {
            "key1": "value1", // This is a comment
            "key2": 123
        }
        """
        
        // Test 2: JSON with trailing commas
        val jsonWithTrailingCommas = """
        {
            "key1": "value1",
            "key2": 123,
        }
        """
        
        // Test 3: JSON with multi-line comments
        val jsonWithMultiLineComments = """
        {
            "key1": "value1",
            /* This is a 
               multi-line comment */
            "key2": 123
        }
        """
        
        // Test 4: Standard JSON (should work)
        val standardJson = """
        {
            "key1": "value1",
            "key2": 123
        }
        """
        
        println("Testing JsonCHelper with various JSON formats:")
        
        // Test preprocessing
        println("Original JSON with comments:")
        println(jsonWithComments)
        println("Preprocessed:")
        println(JsonCHelper.preprocessJsonC(jsonWithComments))
        
        println("\nOriginal JSON with trailing commas:")
        println(jsonWithTrailingCommas)
        println("Preprocessed:")
        println(JsonCHelper.preprocessJsonC(jsonWithTrailingCommas))
        
        println("\nTesting lenient configuration features:")
        println("- ignoreUnknownKeys: Handles extra fields")
        println("- isLenient: More tolerant parsing")
        println("- coerceInputValues: Handles type mismatches")
        println("- allowSpecialFloatingPointValues: Handles NaN, Infinity")
    }
    
    /**
     * Example of how the JsonCHelper would be used in real scenarios
     * instead of the original Json.decodeFromString calls
     */
    data class TestData(
        val key1: String = "",
        val key2: Int = 0
    )
    
    fun demonstrateUsage() {
        val malformedJson = """
        {
            "key1": "test", // comment here
            "key2": 42,
            "unknownKey": "this will be ignored"
        }
        """
        
        try {
            // Old way (would fail with comments):
            // Json.decodeFromString<TestData>(malformedJson)
            
            // New way (tolerant):
            val result = JsonCHelper.decodeFromJsonC<TestData>(malformedJson)
            println("Successfully parsed: $result")
        } catch (e: Exception) {
            println("Parsing failed: ${e.message}")
        }
    }
}