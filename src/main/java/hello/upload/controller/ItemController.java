package hello.upload.controller;

import hello.upload.domain.UploadFile;
import hello.upload.domain.Item;
import hello.upload.domain.ItemRepository;
import hello.upload.file.FileStore;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException, IOException {
//        private Long itemId;
//        private String itemName;
//        private List<MultipartFile> imageFiles;
//        private MultipartFile attachFile;
        // @ModelAttribute 로 위 요소를 가진 객체 ItemForm 객체에 매핑.
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());

//        public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
//            if (multipartFile.isEmpty()) {
//                return null;
//            }
//            String originalFilename = multipartFile.getOriginalFilename();
//            String storeFileName = createStoreFileName(originalFilename);
//            multipartFile.transferTo(new File(getFullPath(storeFileName)));
//            return new UploadFile(originalFilename, storeFileName);
//        }

//        private String createStoreFileName(String originalFilename) {
//            String ext = extractExt(originalFilename);
//            String uuid = UUID.randomUUID().toString();
//            return uuid + "." + ext;
//        }
//
//        private String extractExt(String originalFilename) {
//            int pos = originalFilename.lastIndexOf(".");
//            return originalFilename.substring(pos + 1);
//        }


        // ItemForm 에서 가져온 attachFile 이 비어있으면 null , 비어있지 않으면 MultipartFile 의 이름을 가져온다.
        // 가져온 originalFileName 의 확장자를 제거한 후 uuid + 확장자 제거한 이름 으로 storeFileName 생성.
        // fileDir + storeFileName 의 저장경로에 파일을 업로드.
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

//        public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
//            List<UploadFile> storeFileResult = new ArrayList<>();
//            for (MultipartFile multipartFile : multipartFiles) {
//                if (!multipartFile.isEmpty()) {
//                    storeFileResult.add(storeFile(multipartFile));
//                }
//            }
//            return storeFileResult;
//        }

        //        private List<MultipartFile> imageFiles;
        // ImageFiles 는 MultipartFile 타입의 List 이다.
        // ImageFiles List 를 for 문 돌면서 비어있지 않다면 ArrayList 에 담는다.
        // 이때

        // ItemForm 에서 가져온 attachFile 이 비어있으면 null , 비어있지 않으면 MultipartFile 의 이름을 가져온다.
        // 가져온 originalFileName 의 확장자를 제거한 후 uuid + 확장자 제거한 이름 으로 storeFileName 생성.
        // fileDir + storeFileName 의 저장경로에 파일을 업로드.

        // 를 반복.

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(attachFile);
        item.setImageFiles(storeImageFiles);
        itemRepository.save(item);

        redirectAttributes.addAttribute("itemId", item.getId());

        return "redirect:/items/{itemId}";
    }

    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id);
        model.addAttribute("item", item);
        return "item-view";
    }

    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {

        Item item = itemRepository.findById(itemId);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);

        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}

