package backendMain

import coreTypes.Request
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*

fun main() {
    embeddedServer(Netty, port = 8080) {
        App().apply { main() }
    }.start(wait = true)
}

fun Application.main() {
    App().apply { main() }
}

class App {
    private val fileController = FileController()

    fun Application.main() {
        install(DefaultHeaders)
        install(CallLogging)

        routing {
            serviceRouting(fileController)
            staticResources("", "web")
        }
    }
}

suspend inline fun <reified T : Any> ApplicationCall.respond(message: T) {
    respond(message, typeInfo<T>())
}

fun Route.serviceRouting(fileController: FileController) {
    get("example/get_data") {
        val RAC = call.parameters["RAC"] ?: error("RAC not provided")
        val LAP = call.parameters["LAP"]?.let { if (it.isNotEmpty()) it.toInt() else null } ?: error("LAP is not a number")
        val RAP = call.parameters["RAP"]?.let { if (it.isNotEmpty()) it.toInt() else null } ?: error("RAP is not a number")
        val REFKEY = call.parameters["REFKEY"]

        val request = Request(RAC, LAP, RAP, REFKEY)
        val (response, error) = fileController.getData(request)
        val result =
            if (error != null) "Error: $error"
            else response.joinToString("\n") { it.printAdditionalParams(request) }

        call.respond(result)
    }

    post("example/open_file") {
        try {
            val fileName = call.parameters["filePath"] ?: error("filePath not provided")
            val indexPath = call.parameters["indexPath"] ?: error("indexPath not provided")

            fileController.changeFile(fileName, indexPath)
            call.respond("")
        } catch (t: Throwable) {
            call.respond(t.message ?: "Unknown error while opening file")
        }
    }
}