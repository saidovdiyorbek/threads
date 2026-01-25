package dasturlash.attachservice

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Calendar
import kotlin.io.path.Path

interface AttachService {
    fun upload(files: List<MultipartFile>): List<AttachUrl>
    fun generatedDataBaseFolder(): String
    fun getExtension(fileName: String?): String
    fun saveAttach(file: MultipartFile, pathFolder: String, hash: String, extension: String): String
    fun createAttach(file: MultipartFile, hash: String, extension: String, pathFolder: String, fullPath: String): Attach
    fun openUrl(hash: String): String
    fun isExists(hash: String): Boolean
    fun exists(hash: String): Boolean
}

@Service
class AttachServiceImpl(
    @Value("\${attach.upload.folder}")private val folderName: String,
    @Value("\${attach.url}")private val attachUrl: String,

    private val repository: AttachRepository,
    private val hash: GenerateHash
) : AttachService {
    @Transactional
    override fun upload(files: List<MultipartFile>): List<AttachUrl> {
        val attachUrls: MutableList<AttachUrl> = mutableListOf()
        files.forEach { file ->
            val pathFolder: String = generatedDataBaseFolder()
            val extension = getExtension(file.originalFilename)
            val hash: String = hash.generateHash()
            val fullFilePath = saveAttach(file, pathFolder, hash, extension)

            val attach = createAttach(file, hash, extension, pathFolder, fullFilePath)
            attachUrls.add(AttachUrl(attach.hash, openUrl(attach.hash)))
        }
        return attachUrls
    }

    override fun generatedDataBaseFolder(): String {
        val cal: Calendar = Calendar.getInstance()
        val folder: String = "${cal.get(Calendar.YEAR)}/" +
                "${cal.get(Calendar.MONTH)}/" +
                "${cal.get(Calendar.DATE)}"
        return folder
    }

    override fun getExtension(fileName: String?): String {
        if(fileName == null) return ""
        val lastIndex = fileName.lastIndexOf('.')
        return fileName.substring(lastIndex + 1)
    }

    override fun saveAttach(
        file: MultipartFile,
        pathFolder: String,
        hash: String,
        extension: String
    ): String {
        val path = Path(folderName)
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
            var fullFileName: String = ""
            if (file.originalFilename.isNullOrBlank()){
                fullFileName = "$hash.$extension"
            }
            fullFileName = file.originalFilename?.let{it}.toString()
            val fullPath: Path = Paths.get("$folderName/$pathFolder/$fullFileName")
            Files.createDirectories(fullPath.parent)
            Files.write(fullPath, file.bytes)

            return fullPath.toString()

        }catch (e: IOException) {
            throw FileCreateException()
        }
    }

    override fun createAttach(
        file: MultipartFile,
        hash: String,
        extension: String,
        pathFolder: String,
        fullPath: String
    ): Attach {
        val attach = repository.save(Attach(
            originName = file.originalFilename,
            size = file.size,
            type = file.contentType,
            path = "$folderName/$pathFolder",
            hash = hash,
            fullPath = fullPath,
        ))
        return attach
    }

    override fun openUrl(hash: String): String {
        if (isExists(hash)) {
            return "$attachUrl/open/$hash"
        }
        return ""
    }

    override fun isExists(hash: String): Boolean {
        return repository.existsByHashAndDeletedFalse(hash)
    }

    override fun exists(hash: String): Boolean {
        repository.existsByHashAndDeletedFalse(hash).takeIf { it }?.let {
            return true
        }
        throw AttachNotFoundException()
    }
}