# JSONC Implementation in Updater-KMP

This document describes the implementation of JSONC (JSON with Comments) support to replace the strict JSON parsing that was causing failures with malformed JSON responses.

## Problem Addressed

The original issue was that the strict JSON parser (`kotlinx.serialization.json.Json`) would fail when parsing JSON responses from Xiaomi servers that contained:
- Comments (single-line `//` or multi-line `/* */`)
- Trailing commas
- Unknown/extra fields
- Type mismatches
- Special floating-point values

## Solution Implemented

### JsonCHelper Utility

Created `utils/JsonCHelper.kt` with the following features:

1. **Lenient JSON Configuration**:
   ```kotlin
   val jsonc = Json {
       ignoreUnknownKeys = true           // Skip unknown fields
       isLenient = true                   // More tolerant parsing
       coerceInputValues = true           // Handle type mismatches
       allowStructuredMapKeys = true      // Support complex keys
       allowSpecialFloatingPointValues = true  // Handle NaN, Infinity
   }
   ```

2. **JSONC Preprocessing**:
   - Removes single-line comments (`//`)
   - Removes multi-line comments (`/* */`)
   - Removes trailing commas
   - Cleans up whitespace

3. **Safe Wrapper Functions**:
   - `decodeFromJsonC<T>(jsonString): T` - Preprocesses and decodes
   - `encodeToJsonC<T>(value): String` - Encodes with lenient config

### Migration Changes

Replaced all instances of:
```kotlin
// Old (strict parsing)
Json.decodeFromString<DataType>(jsonString)
Json.encodeToString(data)

// New (lenient parsing)
JsonCHelper.decodeFromJsonC<DataType>(jsonString)
JsonCHelper.encodeToJsonC(data)
```

### Files Updated

1. **`utils/DeviceListUtils.kt`** - Device list parsing from remote sources
2. **`RomInfo.kt`** - ROM information parsing from Xiaomi servers
3. **`Login.kt`** - Authentication response parsing
4. **`App.kt`** - Preference data parsing

## Benefits

1. **Improved Reliability**: No more parsing failures from malformed JSON
2. **JSONC Support**: Can handle JSON with comments from servers
3. **Better Error Recovery**: Graceful handling of unexpected data
4. **Centralized Configuration**: Consistent JSON behavior across the app
5. **Backward Compatibility**: Still handles standard JSON perfectly

## Testing

The implementation includes test examples in `test/JsonCTest.kt` demonstrating:
- Parsing JSON with comments
- Handling trailing commas
- Processing unknown fields
- Type coercion examples

## Usage Examples

### Before (would fail with comments):
```kotlin
val data = Json.decodeFromString<RomInfo>(responseWithComments)  // ❌ Fails
```

### After (tolerant parsing):
```kotlin
val data = JsonCHelper.decodeFromJsonC<RomInfo>(responseWithComments)  // ✅ Works
```

### Example JSONC Input:
```json
{
    "currentRom": {
        "version": "OS1.0.1.0",  // Latest version
        "size": 1234567890,
        "unknownField": "ignored"  // This won't break parsing
    },
    "latestRom": {
        "version": "OS1.0.2.0",
        /* Multi-line comment
           about the latest ROM */
        "size": 1345678901,
    }  // Trailing comma is OK
}
```

This implementation solves the original issue: "避免过于严格的 json 导致解析不出来" (Avoid overly strict JSON causing parsing failures).