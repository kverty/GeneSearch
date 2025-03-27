package coreTypes

import kotlinx.serialization.Serializable

@Serializable
data class Request(
    val RAC: String = "",
    val LAP: Int? = null,
    val RAP: Int? = null,
    val REFKEY: String? = null,
    val VCF_ID: String? = null,
    val CLNSIG: String? = null
) {
    fun contains(other: Request): Boolean =
        (RAC == other.RAC)
            && (LAP == null || LAP == other.LAP)
            && (RAP == null || RAP == other.RAP)
            && (REFKEY == null || REFKEY == other.REFKEY)
            && (VCF_ID == null || VCF_ID == other.VCF_ID)
            && (CLNSIG == null || CLNSIG == other.CLNSIG)
}

