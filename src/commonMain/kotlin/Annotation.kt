package coreTypes

import kotlinx.serialization.Serializable

@Serializable
data class Annotation(val RAC: String, val LAP: Int, val RAP: Int, val REFKEY: String, val VCF_ID: String, val CLNSIG: String, val CLNREVSTAT: String, val CLNVC: String) {
    fun fits(request: Request): Boolean =
        (RAC == request.RAC)
            && (request.LAP == null || LAP == request.LAP)
            && (request.RAP == null || RAP == request.RAP)
            && (request.REFKEY == null || REFKEY == request.REFKEY)
            && (request.VCF_ID == null || VCF_ID == request.VCF_ID)
            && (request.CLNSIG == null || CLNSIG == request.CLNSIG)

    fun printAdditionalParams(request: Request): String =
        buildString {
            if (request.RAC == null) append("RAC = $RAC, ")
            if (request.LAP == null) append("LAP = $LAP, ")
            if (request.RAP == null) append("RAP = $RAP, ")
            if (request.REFKEY == null) append("REFKEY = $REFKEY, ")
            if (request.VCF_ID == null) append("VCF_ID = $VCF_ID, ")
            if (request.CLNSIG == null) append("CLNSIG = $CLNSIG, ")
            append("CLNREVSTAT = $CLNREVSTAT, ")
            append("CLNVC = $CLNVC")
        }
}

fun String.toAnnotation(): Annotation {
    val params = this.split('\t')

    return Annotation(
        params[0],
        params[1].toInt(),
        params[2].toInt(),
        params[3],
        params[4],
        params[5],
        params[6],
        params[7]
    )
}