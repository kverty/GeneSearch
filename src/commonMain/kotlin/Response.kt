package coreTypes

import kotlinx.serialization.Serializable

@Serializable
data class Response(val data: List<Annotation>, val error: String? = null)