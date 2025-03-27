package frontendMain

import coreTypes.Request
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.browser.document
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    val client = Client(HttpClient { install(WebSockets) })

    var RAC: String = ""
    var LAP: Int? = null
    var RAP: Int? = null
    var REFKEY: String? = null
    var VCF_ID: String? = null
    var CLNSIG: String? = null

    document.addEventListener("DOMContentLoaded", {
        val sendFileButton = document.getElementById("sendFileButton") as HTMLElement
        val fileNameInput = document.getElementById("dataFileInput") as HTMLInputElement
        val fileIndexInput = document.getElementById("indexFileInput") as HTMLInputElement

        sendFileButton.addEventListener("click", {
            GlobalScope.launch {
                val result = client.openFile(fileNameInput.value, fileIndexInput.value)

                if (result.isNotEmpty()) {
                    writeMessage(result)
                }
            }
        })
    })

    document.addEventListener("DOMContentLoaded", {
        val sendPropertyButton = document.getElementById("sendProperty") as HTMLElement
        val propertyNameInput = document.getElementById("propertyNameInput") as HTMLInputElement
        val propertyValueInput = document.getElementById("propertyValueInput") as HTMLInputElement

        sendPropertyButton.addEventListener("click", {
            GlobalScope.launch {
                when (propertyNameInput.value) {
                    "RAC" -> RAC = propertyValueInput.value
                    "LAP" -> LAP = propertyValueInput.value.toInt()
                    "RAP" -> RAP = propertyValueInput.value.toInt()
                    "REFKEY" -> RAC = propertyValueInput.value
                    "VCF_ID" -> VCF_ID = propertyValueInput.value
                    "CLNSIG" -> CLNSIG = propertyValueInput.value
                }

                propertyNameInput.value = ""
                propertyValueInput.value = ""
            }
        })
    })

    document.addEventListener("DOMContentLoaded", {
        val requestAnnotationsButton = document.getElementById("requestAnnotations") as HTMLElement

        requestAnnotationsButton.addEventListener("click", {
            GlobalScope.launch {
                val request = Request(RAC, LAP, RAP, REFKEY, VCF_ID, CLNSIG)
                writeMessage("requestAnnotations clicked. Request = $request")
                val annotations = client.getData(request)

                RAC = ""
                LAP = null
                RAP = null
                REFKEY = null
                VCF_ID = null
                CLNSIG = null

                writeMessage(annotations)
            }
        })
    })
}

fun writeMessage(message: String) {
    val line = document.createElement("p") as HTMLElement
    line.className = "message"
    line.textContent = message

    val messagesBlock = document.getElementById("messages") as HTMLElement
    messagesBlock.appendChild(line)
    messagesBlock.scrollTop = line.offsetTop.toDouble()
}