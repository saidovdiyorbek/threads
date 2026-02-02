package dasturlash.attachservice

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
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
    fun exists(hash: InternalHashCheckRequest): Boolean
    fun listExists(hashes: InternalHashesCheckRequest): Boolean
    fun deleteList(hashes: List<String>)
    fun deleteFileFromFolder(folder: String, fileName: String): Boolean
    fun download(hash: String): ResponseEntity<Resource>
}

@Service
class AttachServiceImpl(
    @Value("\${attach.upload.folder}")private val folderName: String,
    @Value("\${attach.url}")private val attachUrl: String,

    private val repository: AttachRepository,
    private val hash: GenerateHash,
    private val securityUtil: SecurityUtil,
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
            userId = securityUtil.getCurrentUserId()
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

    override fun exists(hash: InternalHashCheckRequest): Boolean {
        repository.existsByHashAndUserIdAndDeletedFalse(hash.hash,hash.userId).takeIf { it }?.let {
            println("Attach topildi")
            return true
        }
        println("Attach topilmadi")
        throw AttachNotFoundException()
    }

    override fun listExists(hashes: InternalHashesCheckRequest): Boolean {
        val existsHashCount = repository.existsHashList(hashes.hashes, hashes.userId)
        if (existsHashCount != null && existsHashCount == hashes.hashes.size.toLong()) {
            return true
        }
        return false
    }
    @Transactional
    override fun deleteList(hashes: List<String>) {
        if (hashes.isNotEmpty()) {
            hashes.forEach { hash ->
                repository.findAttachByHashAndDeletedTrue(hash)?.let { attach ->
                    deleteFileFromFolder(attach.path, attach.originName!!)
                }
            }

            repository.deleteByHashList(hashes)

        }
    }

    override fun deleteFileFromFolder(folder: String, fileName: String): Boolean{
        return try {
            val filePath = Paths.get(folder, fileName)

            Files.deleteIfExists(filePath)
        }catch (e: Exception){
            println("Problem delete file ${e.message}")
            false
        }
    }

    override fun download(hash: String): ResponseEntity<Resource> {
        TODO("Not yet implemented")
    }
}