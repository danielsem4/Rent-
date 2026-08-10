---
name: kmp-data-layer
description: Building the data layer correctly — API calls, DTOs, mappers, Ktor services, error handling in the Rent KMP codebase
---

# Building the Data Layer in Rent

Use this skill when adding API calls, endpoints, services, networking, or backend integration.

> **Rent note:** Ktor, kotlinx-serialization, and the `core:data` networking helpers referenced
> below do **not** exist in Rent yet — add the dependencies to `gradle/libs.versions.toml` and
> create the `core:data` networking utilities (`HttpClientExt.kt`, `safeCall`, `UrlConstants.kt`)
> the first time you build the data layer. See the **kmp-error-handling** skill for the `safeCall`
> and `Result` implementations these helpers rely on.

## Architecture Overview

```
Domain Layer (interface + models)
  ↑ depends on nothing but core.domain
  │
Data Layer (Ktor implementation + DTOs + mappers)
  ↑ depends on domain + core.data
  │
Presentation Layer (consumes via DI)
```

## Step 1: Domain Service Interface

Location: `feature/<name>/domain/src/commonMain/kotlin/org/example/rent/<name>/domain/<name>/`

```kotlin
package org.example.rent.<name>.domain.<name>

import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult
import org.example.rent.core.domain.util.Result

interface <Name>Service {

    // GET that returns data
    suspend fun getItems(
        userId: String
    ): Result<List<ItemModel>, DataError.Remote>

    // GET single item
    suspend fun getItem(
        userId: String,
        itemId: String
    ): Result<ItemModel, DataError.Remote>

    // POST that returns nothing (just success/failure)
    suspend fun submitItem(body: SubmitRequest): EmptyResult<DataError.Remote>

    // POST that returns data
    suspend fun createItem(body: CreateRequest): Result<ItemModel, DataError.Remote>
}
```

**Key rules:**
- Always return `Result<T, DataError.Remote>` or `EmptyResult<DataError.Remote>` (which is `Result<Unit, DataError.Remote>`)
- Domain interfaces live in domain layer with NO framework dependencies
- Parameter types should be domain models, not DTOs

## Step 2: Domain Models

Location: `feature/<name>/domain/src/commonMain/kotlin/org/example/rent/<name>/domain/models/`

```kotlin
package org.example.rent.<name>.domain.models

// Simple data class, no annotations, no framework deps
data class ItemModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val value: Int = 0,
    val isActive: Boolean = true,
    val items: List<SubItem> = emptyList()
)
```

## Step 3: DTOs

Location: `feature/<name>/data/src/commonMain/kotlin/org/example/rent/<name>/data/dto/`

```kotlin
package org.example.rent.<name>.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val value: Int = 0,
    val is_active: Boolean = true,
    val items: List<SubItemDto> = emptyList()
)

// Request body DTO
@Serializable
data class SubmitRequestDto(
    val user_id: String,
    val item_id: String,
    val timestamp: String
)
```

**Key rules:**
- Always annotate with `@Serializable`
- Provide default values for ALL fields (prevents deserialization crashes on missing fields)
- Use `@SerialName("json_key")` when Kotlin field name differs from JSON key
- Field names typically match the API's snake_case convention

## Step 4: Mappers

Location: `feature/<name>/data/src/commonMain/kotlin/org/example/rent/<name>/data/mapper/`

```kotlin
package org.example.rent.<name>.data.mapper

import org.example.rent.<name>.data.dto.ItemDto
import org.example.rent.<name>.data.dto.SubItemDto
import org.example.rent.<name>.domain.models.ItemModel
import org.example.rent.<name>.domain.models.SubItem

// DTO -> Domain (extension function on DTO)
fun ItemDto.toDomain(): ItemModel {
    return ItemModel(
        id = id,
        name = name,
        description = description,
        value = value,
        isActive = is_active,
        items = items.map { it.toDomain() }
    )
}

fun SubItemDto.toDomain(): SubItem {
    return SubItem(
        id = id,
        label = label
    )
}

// Domain -> DTO (extension function on domain model)
// Convention: use toSerial() or toSerializable() or toDto()
fun ItemModel.toSerial(): ItemDto {
    return ItemDto(
        id = id,
        name = name,
        description = description,
        value = value,
        is_active = isActive,
        items = items.map { it.toSerial() }
    )
}

fun SubItem.toSerial(): SubItemDto {
    return SubItemDto(
        id = id,
        label = label
    )
}
```

**Key rules:**
- `toDomain()` on DTOs, `toSerial()`/`toDto()` on domain models
- Handle nested objects recursively: `.map { it.toDomain() }`
- Handle nullable lists: `items?.map { it.toDomain() } ?: emptyList()`

## Step 5: Ktor Service Implementation

Location: `feature/<name>/data/src/commonMain/kotlin/org/example/rent/<name>/data/<name>/`

```kotlin
package org.example.rent.<name>.data.<name>

import io.ktor.client.HttpClient
import org.example.rent.core.data.networking.get
import org.example.rent.core.data.networking.post
import org.example.rent.core.data.networking.put
import org.example.rent.core.domain.util.DataError
import org.example.rent.core.domain.util.EmptyResult
import org.example.rent.core.domain.util.Result
import org.example.rent.core.domain.util.map
import org.example.rent.<name>.data.dto.ItemDto
import org.example.rent.<name>.data.dto.SubmitRequestDto
import org.example.rent.<name>.data.mapper.toDomain
import org.example.rent.<name>.data.mapper.toSerial
import org.example.rent.<name>.domain.<name>.<Name>Service
import org.example.rent.<name>.domain.models.ItemModel

class Ktor<Name>Service(
    private val httpClient: HttpClient,
) : <Name>Service {

    // GET list of items
    override suspend fun getItems(
        userId: String
    ): Result<List<ItemModel>, DataError.Remote> {
        return httpClient.get<List<ItemDto>>(
            route = "api_endpoint/",
            queryParams = mapOf(
                "user_id" to userId
            )
        ).map { dtoList -> dtoList.map { it.toDomain() } }
    }

    // GET single item
    override suspend fun getItem(
        userId: String,
        itemId: String
    ): Result<ItemModel, DataError.Remote> {
        return httpClient.get<ItemDto>(
            route = "api_endpoint/$itemId/",
            queryParams = mapOf("user_id" to userId)
        ).map { it.toDomain() }
    }

    // POST with no response body (EmptyResult)
    override suspend fun submitItem(body: SubmitRequest): EmptyResult<DataError.Remote> {
        return httpClient.post(
            route = "submit_endpoint/",
            body = body.toSerial()
        )
    }

    // POST with response body
    override suspend fun createItem(body: CreateRequest): Result<ItemModel, DataError.Remote> {
        return httpClient.post<CreateRequestDto, ItemDto>(
            route = "create_endpoint/",
            body = body.toSerial()
        ).map { it.toDomain() }
    }
}
```

## Available HTTP Methods

From `core/data/src/commonMain/kotlin/org/example/rent/core/data/networking/HttpClientExt.kt`:

```kotlin
// GET - reified Response type
httpClient.get<ResponseType>(
    route = "endpoint/",
    queryParams = mapOf("key" to "value"),  // optional
)

// POST - reified Request and Response types
httpClient.post<RequestBody, ResponseType>(
    route = "endpoint/",
    queryParams = mapOf("key" to "value"),  // optional
    body = requestObject,
)

// PUT - reified Request and Response types
httpClient.put<RequestBody, ResponseType>(
    route = "endpoint/",
    body = requestObject,
)
```

All methods return `Result<T, DataError.Remote>`. Routes are relative to the base URL in `UrlConstants.kt`.

## Step 6: DI Module

Location: `feature/<name>/data/src/commonMain/kotlin/org/example/rent/<name>/data/di/`

```kotlin
package org.example.rent.<name>.data.di

import org.example.rent.<name>.data.<name>.Ktor<Name>Service
import org.example.rent.<name>.domain.<name>.<Name>Service
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val <name>DataModule = module {
    singleOf(::Ktor<Name>Service) bind <Name>Service::class
    // Use factoryOf for use cases (new instance each time):
    // factoryOf(::Get<Name>UseCase)
}
```

Register in `initKoin.kt`:
```kotlin
import org.example.rent.<name>.data.di.<name>DataModule
// Add to modules() block:
<name>DataModule,
```

## Error Handling Chain

1. **Ktor service** calls `httpClient.get/post()` which internally calls `safeCall()`
2. **`safeCall()`** handles platform-specific exceptions (network errors normalize to `IOException`)
3. **`responseToResult()`** maps HTTP status codes to `DataError.Remote` enum values
4. **ViewModel** handles results with `.onSuccess { }` and `.onFailure { }`
5. **UI** displays errors via `error.toUiText()` which maps `DataError` to localized string resources

## Result Type Utilities

```kotlin
import org.example.rent.core.domain.util.Result
import org.example.rent.core.domain.util.map
import org.example.rent.core.domain.util.onSuccess
import org.example.rent.core.domain.util.onFailure
import org.example.rent.core.domain.util.asEmptyResult

// Transform success value
result.map { dto -> dto.toDomain() }

// Handle success/failure (returns self for chaining)
result
    .onSuccess { data -> /* use data */ }
    .onFailure { error -> /* handle error */ }

// Convert Result<T, E> to EmptyResult<E> (discards data)
result.asEmptyResult()
```

## DataError.Remote Values

```
BAD_REQUEST, REQUEST_TIMEOUT, UNAUTHORIZED, FORBIDDEN, NOT_FOUND,
CONFLICT, TOO_MANY_REQUESTS, NO_INTERNET, PAYLOAD_TOO_LARGE,
SERVER_ERROR, SERVICE_UNAVAILABLE, SERIALIZATION_ERROR, UNKNOWN
```

## URL Constants

Base URLs are in `core/data/src/commonMain/kotlin/org/example/rent/core/data/networking/UrlConstants.kt`. Routes passed to `httpClient.get/post()` are relative to `BASE_URL`. Always include trailing slash on routes.
