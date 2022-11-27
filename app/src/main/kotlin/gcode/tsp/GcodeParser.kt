package gcode.tsp

import com.google.common.collect.Lists
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger

class GcodeParser {
    val logger: Logger = Logger.getLogger(GcodeParser::class.java.toString())

    fun fileToString(): ArrayList<String> {

        val resource = javaClass.classLoader.getResource("squares.nc")
        val resourcePath = Paths.get(resource.toURI())

        return Lists.newArrayList(Files.readAllLines(resourcePath))
    }

    fun breakUpG0(): GcodeFile {

        val allLines = fileToString()


        val gcodeFile = GcodeFile()

        var startingG0 = -1
        var preambleEndIndex = -1
        var lastG1Index = -1
        allLines.forEachIndexed { index, it ->
            if (it.startsWith("G0") && startingG0 == -1) {
                //first G0
                preambleEndIndex = index - 1
                startingG0 = index

                logger.info("G0 at index $index")
            }
            if (it.startsWith("G1")) {
                lastG1Index = index
            }
        }

        gcodeFile.preamble = allLines.subList(0, preambleEndIndex)


        var currentSentinel = ""
        gcodeFile.body = allLines.subList(startingG0, lastG1Index)
            .map {
                var ignoreSentinel = false
                if (it.startsWith("G0")) {
                    currentSentinel = "G0"
                    ignoreSentinel = true
                } else if (it.startsWith("G1")) {
                    currentSentinel = "G1"
                    ignoreSentinel = true
                }

                val prefix = if(ignoreSentinel) "" else currentSentinel

                GcodeLine(currentSentinel, it, "${prefix}${it}")
            }
        gcodeFile.conclusion = allLines.subList(lastG1Index, allLines.size - 1)
        println("lastG1index = $lastG1Index")
        return gcodeFile
    }


}

class G0Group {


}

data class GcodeLine(val command: String, val line: String, val expandedLine: String, val index)

class GcodeFile {
    var preamble: List<String> = arrayListOf()
    var body: List<GcodeLine> = arrayListOf()
    var conclusion: List<String> = arrayListOf()
}