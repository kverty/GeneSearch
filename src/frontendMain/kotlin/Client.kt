package frontendMain

import coreTypes.Request
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class Client(private val client: HttpClient) {
    suspend fun openFile(dataPath: String, indexPath: String): String =
        client.post("example/open_file") {
            parameter("filePath", dataPath)
            parameter("indexPath", indexPath)
        }.body()

    suspend fun getData(dto: Request): String =
        client.get("example/get_data") {
            parameter("RAC", dto.RAC)
            if (dto.LAP != null) parameter("LAP", dto.LAP.toString())
            if (dto.RAP != null) parameter("RAP", dto.RAP.toString())
            if (dto.REFKEY != null) parameter("REFKEY", dto.REFKEY)
        }.body()
}