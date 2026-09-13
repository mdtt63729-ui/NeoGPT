package com.neogpt.app.files

import android.content.ContentResolver
import android.net.Uri
import java.nio.charset.Charset
import java.util.zip.ZipInputStream

/** Local, dependency-free reader used when a text-capable model needs file context. */
object AttachmentContentReader {
    private const val MAX_TOTAL_CHARS = 140_000
    private const val MAX_FILE_CHARS = 45_000

    fun readForPrompt(resolver: ContentResolver, uri: Uri, name: String, mimeType: String): String {
        val safeName = name.ifBlank { "Attachment" }
        val lower = safeName.lowercase()
        return when {
            lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".apk") || mimeType.contains("zip") -> readZip(resolver, uri, safeName)
            isTextLike(lower, mimeType) -> readText(resolver, uri, safeName)
            lower.endsWith(".pdf") || mimeType == "application/pdf" -> readPdf(resolver, uri, safeName)
            lower.endsWith(".docx") || lower.endsWith(".xlsx") || lower.endsWith(".pptx") -> readOfficeZip(resolver, uri, safeName)
            else -> "Attachment: $safeName\nType: ${mimeType.ifBlank { "unknown" }}\nThis file is binary and does not expose readable text through the local reader."
        }
    }

    private fun readText(resolver: ContentResolver, uri: Uri, name: String): String {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read $name.")
        val text = bytes.toString(Charsets.UTF_8).ifBlank { bytes.toString(Charset.forName("ISO-8859-1")) }
        return "Attachment: $name\nReadable text:\n${text.take(MAX_TOTAL_CHARS)}"
    }

    private fun readZip(resolver: ContentResolver, uri: Uri, name: String): String {
        val out = StringBuilder("Attachment: $name\nArchive contents:\n")
        var entryCount = 0
        var textCount = 0
        resolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input.buffered()).use { zip ->
                while (out.length < MAX_TOTAL_CHARS) {
                    val entry = zip.nextEntry ?: break
                    entryCount++
                    if (!entry.isDirectory) {
                        out.append("• ").append(entry.name).append("\n")
                        if (isTextLike(entry.name.lowercase(), "")) {
                            val bytes = zip.readBytesLimited(180_000)
                            val text = bytes.toString(Charsets.UTF_8).ifBlank { bytes.toString(Charset.forName("ISO-8859-1")) }
                            if (text.isNotBlank()) {
                                textCount++
                                out.append("\n--- ").append(entry.name).append(" ---\n")
                                out.append(text.take(MAX_FILE_CHARS)).append("\n--- END ").append(entry.name).append(" ---\n")
                            }
                        }
                    }
                    zip.closeEntry()
                }
            }
        } ?: error("Unable to read $name.")
        out.append("\nArchive summary: $entryCount entries, $textCount text-readable entries inspected.")
        return out.toString().take(MAX_TOTAL_CHARS)
    }

    private fun readOfficeZip(resolver: ContentResolver, uri: Uri, name: String): String {
        // OOXML documents are ZIP containers. Extract XML text without requiring a
        // heavy office parser; this is enough to expose document text to the model.
        val raw = readZip(resolver, uri, name)
        return raw.replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").take(MAX_TOTAL_CHARS)
    }

    private fun readPdf(resolver: ContentResolver, uri: Uri, name: String): String {
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: error("Unable to read $name.")
        val raw = bytes.toString(Charset.forName("ISO-8859-1"))
        val result = StringBuilder("Attachment: $name\nPDF text extraction:\n")
        // Handles common uncompressed PDFs and many Flate-compressed content streams.
        Regex("\\((?:\\\\.|[^\\\\)])*\\)\\s*Tj").findAll(raw).take(500).forEach { match ->
            val token = match.value.substringBeforeLast(")").substringAfter("(")
            result.append(unescapePdf(token)).append(' ')
        }
        if (result.length <= "Attachment: $name\nPDF text extraction:\n".length) {
            Regex("\\[(.*?)\\]\\s*TJ", RegexOption.DOT_MATCHES_ALL).findAll(raw).take(200).forEach { match ->
                Regex("\\((?:\\\\.|[^\\\\)])*\\)").findAll(match.groupValues[1]).forEach { part ->
                    val token = part.value.substring(1, part.value.length - 1)
                    result.append(unescapePdf(token)).append(' ')
                }
            }
        }
        if (result.length <= "Attachment: $name\nPDF text extraction:\n".length) {
            result.append("No plain text layer was found locally. The PDF may be scanned/image-only or use an unsupported encoding.")
        }
        return result.toString().take(MAX_TOTAL_CHARS)
    }

    private fun unescapePdf(value: String): String = value
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\(", "(")
        .replace("\\)", ")")
        .replace("\\\\", "\\")

    private fun isTextLike(name: String, mimeType: String): Boolean {
        if (mimeType.startsWith("text/")) return true
        if (mimeType == "application/json" || mimeType == "application/xml" || mimeType == "application/javascript") return true
        return listOf(
            ".txt", ".md", ".markdown", ".json", ".xml", ".csv", ".tsv", ".html", ".htm", ".css", ".js", ".jsx",
            ".ts", ".tsx", ".kt", ".kts", ".java", ".gradle", ".properties", ".yml", ".yaml", ".toml", ".ini",
            ".py", ".rb", ".go", ".rs", ".swift", ".dart", ".c", ".h", ".cpp", ".hpp", ".cs", ".php", ".sql",
            ".sh", ".bash", ".zsh", ".fish", ".pro", ".gitignore", ".env", ".log"
        ).any { name.endsWith(it) }
    }

    private fun ZipInputStream.readBytesLimited(maxBytes: Int): ByteArray {
        val buffer = ByteArray(8192)
        val out = java.io.ByteArrayOutputStream()
        while (out.size() < maxBytes) {
            val read = read(buffer, 0, minOf(buffer.size, maxBytes - out.size()))
            if (read <= 0) break
            out.write(buffer, 0, read)
        }
        return out.toByteArray()
    }
}
