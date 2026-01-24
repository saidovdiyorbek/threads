package dasturlash.attachservice

import org.springframework.http.MediaType
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
    fun upload(@RequestParam("file") file: MultipartFile): AttachUrl =
        service.upload(file)

}