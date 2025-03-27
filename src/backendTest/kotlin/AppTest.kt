package backendMain

import coreTypes.Request
import coreTypes.Response
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class AppTest {
    @Test
    fun testSimpleSearch() {
        testApplication {
            environment {
                config = ApplicationConfig(null)
            }

            val client = client.config { install(WebSockets) }

            val error: String =
                client.post {
                    url("example/open_file")
                    parameter("filePath", "C:\\Users\\danil\\IdeaProjects\\GeneProject\\src\\backendTest\\resources\\clinvar.vcf.gz")
                    parameter("indexPath", "C:\\Users\\danil\\IdeaProjects\\GeneProject\\src\\backendTest\\resources\\clinvar.vcf.gz.tbi")
                }.body()

            assertEquals("", error)

            val request = Request(RAC = "MT", RAP = 930365)
            val response =
                client.get {
                    url("example/get_data")
                    parameter("RAC", request.RAC)
                    parameter("RAP", request.RAP)
                }.body<String>()

            assertEquals(
                "LAP = 1657, REFKEY = C, VCF_ID = CTT, CLNSIG = ., CLNREVSTAT = ., CLNVC = ALLELEID=920076;CLNDISDB=.;CLNDN=See_cases;CLNHGVS=NC_012920.1:m.1658_1659dup;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Uncertain_significance;CLNSIGSCV=SCV001366394;CLNVC=Duplication;CLNVCSO=SO:1000035;CLNVI=ClinGen:CA1139667970;GENEINFO=MT-TV:4577;ORIGIN=0;RS=2068678676",
                response
            )
        }
    }
    @Test
    fun testBinarySearch() {
        testApplication {
            environment {
                config = ApplicationConfig(null)
            }

            val client = client.config { install(WebSockets) }

            val error: String =
                client.post {
                    url("example/open_file")
                    parameter("filePath", "C:\\Users\\danil\\IdeaProjects\\GeneProject\\src\\backendTest\\resources\\clinvar.vcf.gz")
                    parameter("indexPath", "C:\\Users\\danil\\IdeaProjects\\GeneProject\\src\\backendTest\\resources\\clinvar.vcf.gz.tbi")
                }.body()

            assertEquals("", error)

            val request = Request(RAC = "MT", LAP = 1657)
            val response =
                Json.decodeFromString<Response>(
                    client.get {
                        url("example/get_data")
                        parameter("request", Json.encodeToString(request))
                    }.body<String>()
                )

            assertEquals(
                "RAP = 930365, REFKEY = C, VCF_ID = CTT, CLNSIG = ., CLNREVSTAT = ., CLNVC = ALLELEID=920076;CLNDISDB=.;CLNDN=See_cases;CLNHGVS=NC_012920.1:m.1658_1659dup;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Uncertain_significance;CLNSIGSCV=SCV001366394;CLNVC=Duplication;CLNVCSO=SO:1000035;CLNVI=ClinGen:CA1139667970;GENEINFO=MT-TV:4577;ORIGIN=0;RS=2068678676, RAP = 689850, REFKEY = C, VCF_ID = T, CLNSIG = ., CLNREVSTAT = ., CLNVC = ALLELEID=677687;CLNDISDB=MONDO:MONDO:0010789,MedGen:C0162671,OMIM:540000,Orphanet:550;CLNDN=MELAS_syndrome;CLNHGVS=NC_012920.1:m.1657C>T;CLNREVSTAT=criteria_provided,_single_submitter;CLNSIG=Benign;CLNSIGSCV=SCV000992912;CLNVC=single_nucleotide_variant;CLNVCSO=SO:0001483;CLNVI=ClinGen:CA913163439;GENEINFO=MT-TV:4577;ORIGIN=1;RS=1603218606",
                response.data.map { it.printAdditionalParams(request) }.joinToString { it }
            )
        }
    }
}