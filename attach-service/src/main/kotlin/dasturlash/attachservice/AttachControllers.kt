package dasturlash.attachservice

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/attaches")
class AttachController(
    private val service: AttachService
){
    @PostMapping("/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun upload(@RequestParam("files") files: List<MultipartFile>): List<AttachUrl> =
        service.upload(files)

}

@RestController
@RequestMapping("/internal/api/v1/attaches")
class AttachInternalController(
    private val service: AttachService
){
    @GetMapping("/{hash}/exists")
    fun exists(@PathVariable hash: String): Boolean = service.exists(hash)

    @PostMapping("/hashes/exists")
    fun listExists(@RequestBody hashes: List<String>): Boolean = service.listExists(hashes)

    @DeleteMapping("/deleteList")
    fun deleteList(@RequestBody hashes: List<String>) = service.deleteList(hashes)
}