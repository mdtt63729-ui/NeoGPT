package com.neogpt.app.tools

import com.neogpt.app.domain.model.ToolResult
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipTool : Tool {
    override val name = "zip"
    override val description = "Create a ZIP archive from a project or directory"
    override suspend fun execute(params: Map<String, String>): ToolResult {
        val sourcePath = params["source"] ?: params["path"] ?: return ToolResult(name, false, "", "Missing 'source' parameter")
        val outputPath = params["output"] ?: return ToolResult(name, false, "", "Missing 'output' parameter")
        return runCatching {
            val source = File(sourcePath).canonicalFile
            val output = File(outputPath).canonicalFile
            if (!source.isDirectory) return@runCatching ToolResult(name, false, "", "Source directory not found")
            if (output.toPath().startsWith(source.toPath())) return@runCatching ToolResult(name, false, "", "Output ZIP must be outside source")
            output.parentFile?.mkdirs()
            if (output.exists()) output.delete()
            ZipOutputStream(output.outputStream().buffered()).use { zip ->
                source.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relative = source.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                    zip.putNextEntry(ZipEntry(relative))
                    FileInputStream(file).use { it.copyTo(zip, 16 * 1024) }
                    zip.closeEntry()
                }
            }
            ToolResult(name, true, output.absolutePath, "Created ${output.length()} byte ZIP archive")
        }.getOrElse { ToolResult(name, false, "", it.message ?: "ZIP creation failed") }
    }
}
