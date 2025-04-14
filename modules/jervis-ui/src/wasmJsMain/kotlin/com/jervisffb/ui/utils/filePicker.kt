package com.jervisffb.ui.utils

import com.jervisffb.utils.jervisLogger
import kotlinx.browser.document
import okio.Path
import okio.Path.Companion.toPath
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader

private fun saveWebFile(filename: String, content: String, mimeType: String = "application/json") {
    @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
    val blobParts: JsArray<JsAny?> =   content.let { arrayOf(content.toJsString() as JsAny?).toJsArray() }
    val blob = Blob(blobParts, BlobPropertyBag(type = mimeType))
    val url = URL.createObjectURL(blob)
    val a = document.createElement("a") as HTMLAnchorElement
    a.href = url
    a.download = filename
    document.body?.appendChild(a)
    a.click()
    document.body?.removeChild(a)
    URL.revokeObjectURL(url)
}

actual fun saveFile(
    dialogTitle: String,
    fileName: String,
    fileContent: String,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
) {
    saveWebFile(fileName, fileContent)
}

actual fun readFile(
    dialogTitle: String,
    extensionFilterDescription: String,
    extensionFilterFileType: String,
    onLoad: (Path?, Result<String>) -> Unit,
) {
    try {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.onchange = {
            val file = input.files?.item(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val fileContent = reader.result.toString()
                    val loadResult = Result.success(fileContent)
                    onLoad(file.name.toPath(), loadResult)
                }
                reader.onerror = {
                    onLoad(file.name.toPath(), Result.failure(Throwable("File read error: ${file.name}")))
                }
                reader.readAsText(file)
            } else {
                onLoad(null, Result.success(""))
            }
        }
        input.oncancel = {
            onLoad(null, Result.success(""))
        }
        input.onabort = {
            onLoad(null, Result.success(""))
        }
        input.onerror = { _, _, _, _, _ ->
            onLoad(null, Result.failure(Throwable("Unknown error reading file.")))
            null
        }
        input.click() // Opens the file dialog (requires user action)
    } catch (ex: Exception) {
        jervisLogger().w { "Failed to read file.\n${ex.stackTraceToString()}" }
        return onLoad(null, Result.failure(ex))
    }
}
