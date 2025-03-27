package backendMain

import coreTypes.Request
import coreTypes.Response
import coreTypes.Annotation
import htsjdk.tribble.readers.TabixReader
import io.github.reactivecircus.cache4k.Cache
import coreTypes.toAnnotation
import kotlin.time.Duration.Companion.hours

class FileController {
    private val cachedCalls: Cache<Request, List<Annotation>> =
        Cache.Builder<Request, List<Annotation>>()
            .expireAfterWrite(1.hours)
            .maximumCacheSize(1000)
            .build()

    private var _currentFile: TabixReader? = null

    private val pageSize: Int = 1000

    fun getData(request: Request): Response {
        val exactSameRequest = cachedCalls.get(request)

        if (exactSameRequest != null) return Response(exactSameRequest)

        val moreGeneralRequest = (cachedCalls.asMap() as Map<Request, List<Annotation>>).entries.find { (r) -> r.contains(request) }

        if (moreGeneralRequest != null) {
            val allAnnotations = moreGeneralRequest.value
            return Response(allAnnotations.filter { it.fits(request) })
        }

        val currentFile = _currentFile ?: return Response(emptyList(), "File not found")

        val lap = request.LAP
        val result =
            if (lap == null ) currentFile.sequentialSearch(request)
            else currentFile.binarySearch(request, lap)

        cachedCalls.put(request, result)
        return Response(result)
    }

    private fun TabixReader.annotationsFromSegment(chr: String, segNum: Int): List<Annotation> {
        val offset = segNum * pageSize
        val iterator = this.query("$chr:$offset-${offset+pageSize}")

        val dataSection = mutableListOf<Annotation>()
        while (iterator != null) {
            val nextLine = iterator.next() ?: break

            dataSection.add(nextLine.toAnnotation())
        }

        return dataSection.toList()
    }

    private fun TabixReader.sequentialSearch(request: Request): List<Annotation> {
        val foundAnnotations = mutableListOf<Annotation>()

        var searches = 1
        while (true) {
            try {
                val annotationsFound =
                    annotationsFromSegment(request.RAC, searches).filter { it.fits(request) }
                foundAnnotations.addAll(annotationsFound)
            } catch (t: Throwable) {
                break
            }
            searches++
        }

        return foundAnnotations
    }

    private fun TabixReader.firstAnnotationFromSegment(chr: String, segNum: Int): Annotation? {
        val offset = segNum * pageSize
        val iterator = this.query("$chr:$offset-${offset+pageSize}")

        return iterator?.next()?.toAnnotation()
    }

    private val cachedBinarySearch: Cache<Int, Annotation> =
        Cache.Builder<Int, Annotation>()
            .expireAfterWrite(1.hours)
            .maximumCacheSize(1000)
            .build()

    private val cachedIterationsNumber: Int = 6

    private fun TabixReader.getAnnotationFromMediumSegment(chr: String, segNum: Int, numberOfIterations: Int): Annotation? {
        var mediumAnnotation = cachedBinarySearch.get(segNum)

        if (mediumAnnotation == null) {
            mediumAnnotation = firstAnnotationFromSegment(chr, segNum)

            if (numberOfIterations < cachedIterationsNumber && mediumAnnotation != null) {
                cachedBinarySearch.put(segNum, mediumAnnotation)

            }
        }

        return mediumAnnotation
    }

    private fun TabixReader.binarySearch(request: Request, lapToSearch: Int): List<Annotation> {
        var leftSegment = 0
        var rightSegment = Int.MAX_VALUE / pageSize
        var numberOfIterations = 0

        while (leftSegment < rightSegment) {
            val medium = (leftSegment + rightSegment) / 2 + 1
            val mediumAnnotation = getAnnotationFromMediumSegment(request.RAC, medium, numberOfIterations)

            if (mediumAnnotation != null && lapToSearch >= mediumAnnotation.LAP) leftSegment = medium
            else rightSegment = medium - 1

            if (mediumAnnotation != null) {
                numberOfIterations++
            }
        }

        var foundSegment = annotationsFromSegment(request.RAC, leftSegment)
        val result = foundSegment.filter { it.fits(request) }.toMutableList()

        while (foundSegment.last().fits(request)) {
            leftSegment++
            foundSegment = annotationsFromSegment(request.RAC, leftSegment)

            result.addAll(foundSegment.filter { it.fits(request) })
        }

        return result
    }

    fun changeFile(newFilePath: String, newIndexPath: String) {
        _currentFile = TabixReader(newFilePath, newIndexPath)
        cachedCalls.invalidateAll()
        cachedBinarySearch.invalidateAll()
    }
}