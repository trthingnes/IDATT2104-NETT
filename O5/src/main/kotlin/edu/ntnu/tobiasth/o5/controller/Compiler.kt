package edu.ntnu.tobiasth.o5.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileWriter
import java.lang.IllegalStateException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@RestController
class Compiler {
    @PostMapping("/compile")
    @CrossOrigin
    fun compile(@RequestBody data: Map<String, Any>): ResponseEntity<Any> {
        val logger = LoggerFactory.getLogger(this.javaClass)
        logger.info("Got new request to handle.")

        return try {
            if(!data.containsKey("code")) {
                throw IllegalStateException("Key 'code' is required.")
            }

            val code = data["code"] as String
            val tempFolder = File("docker/temp")

            tempFolder.mkdir()

            logger.info("Attempting to write file at ${Path.of("docker/temp/program.cpp").absolutePathString()}.")
            FileWriter("docker/temp/program.cpp").use { it.write(code) }

            Runtime.getRuntime().exec("docker rmi cpp-compiler").waitFor()
            val builder = Runtime.getRuntime().exec("docker build ./docker/ -t cpp-compiler")
            val buildError = String(builder.errorStream.readAllBytes())

            tempFolder.deleteRecursively()

            val runner = Runtime.getRuntime().exec("docker run --rm cpp-compiler")
            val runError = String(runner.errorStream.readAllBytes())
            val runOutput = String(runner.inputStream.readAllBytes())

            if(runError.isNotBlank()) {
                throw IllegalStateException("$buildError\n$runError")
            }

            logger.info("Finished handling request.")
            ResponseEntity.status(HttpStatus.OK).body(makeMap("output", "$buildError\nProgram output:\n$runOutput"))
        }
        catch (e: Exception) {
            logger.warn("Failed with message '${e.message}'.")
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(makeMap("output", e.message ?: ""))
        }
    }

    private fun makeMap(key: String, content: String): Map<String, String> {
        return mapOf(Pair(key, content))
    }
}