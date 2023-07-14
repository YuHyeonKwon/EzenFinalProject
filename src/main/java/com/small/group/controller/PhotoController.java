package com.small.group.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.small.group.dto.GroupDTO;
import com.small.group.dto.PhotoCommentDTO;
import com.small.group.dto.PhotoDTO;
import com.small.group.entity.Photo;
import com.small.group.entity.PhotoComment;
import com.small.group.entity.User;
import com.small.group.service.GroupMemberService;
import com.small.group.service.GroupService;
import com.small.group.service.PhotoCommentService;
import com.small.group.service.PhotoService;
import com.small.group.service.UserService;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.coobird.thumbnailator.Thumbnailator;


@RequestMapping("/photo")
@Controller
@RequiredArgsConstructor
@Slf4j
public class PhotoController {
	
	/*
	 * @Value
	 *  - application.properties에 설정한 값 읽어오기
	 *  - ${com.javalab.upload.path}
	 *    - com.javalab.upload.path=C:\\filetest
	 */
    private String uploadPath;
	
	private final GroupService groupService;
	private final UserService userService;
	private final PhotoService photoService;
	private final PhotoCommentService photoCommentService;
	private final GroupMemberService groupMemberService;
	
	/*
     *	[ 사진첩 관련 컨트롤러 맵핑 ]
     *	작성자 : 유성태 - 07/10 
     */
	
	/*
	 * 그룹 사진첩 앨범 이동
	 */
	@GetMapping("/photoList")
	public String getPhotoList(Model model,
							  @RequestParam("groupNo") Integer groupNo,
							  @ModelAttribute("photoDTO") PhotoDTO photoDTO,
							  HttpSession session) {
		
		User user = (User) session.getAttribute("user");
	    if (user == null) {
	    	return "redirect:/user/login";
	    }
		
	    // 그룹 번호에 해당하는 사진 목록 가져오기
	    List<PhotoDTO> photoList = photoService.getPhotoListByGroupNo(groupNo, user.getUserNo());
	    photoList.sort(Comparator.comparing(PhotoDTO::getPhotoNo).reversed());	//역순(최신순)으로
	    
	    GroupDTO groupDto = groupService.readGroup(groupNo);
	    
		// 로그인 된 회원 정보가 모임의 회원 정보가 같을 경우에 'upload' 버튼의 유무를 전달함.
		Integer isMemberIncludedGroup = groupMemberService.isMemberOfGroup(user.getUserNo(), groupNo);
		if (isMemberIncludedGroup > 0) {
			model.addAttribute("uploadButton", true);
		}else {
			model.addAttribute("uploadButton", false);
		}
		
		model.addAttribute("group", groupDto);
	    model.addAttribute("photoList", photoList);
	    model.addAttribute("groupNo", groupNo); // 그룹 번호도 모델에 추가
	    
	    // 확인을 위해 모델에 추가한 사진 목록을 로그로 출력
	    for (PhotoDTO photo : photoList) {
	        System.out.println(photo);
	    }
	    
	    return "photo/photoList";
	}

	/*
     * 사진 업로드 페이지로 이동
     */
    @GetMapping("/uploadPhoto")
    public String uploadPhoto(Model model,
                              @RequestParam("groupNo") Integer groupNo,
                              HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/user/login";
        }

        GroupDTO groupDto = groupService.readGroup(groupNo);

        // 현재 로그인한 사용자가 그룹의 회원인지 확인
        Integer isMemberIncludedGroup = groupMemberService.isMemberOfGroup(user.getUserNo(), groupNo);
        if (isMemberIncludedGroup > 0) {
            model.addAttribute("uploadButton", true);
        } else {
            model.addAttribute("uploadButton", false);
        }
        model.addAttribute("group", groupDto);
        model.addAttribute("groupNo", groupNo);

        return "photo/uploadPhoto";
    }

    @PostMapping("/uploadPhoto")
    @ResponseBody
    public String uploadPhoto(@ModelAttribute("photoDTO") PhotoDTO photoDTO,
    						@RequestParam("imageFile") MultipartFile imageFile,
    						Model model,
    						HttpSession session) {
    	User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/user/login";
		}
		
		if (!imageFile.isEmpty()) {
	        try {
	            String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
	            String uploadDir = "src/main/resources/static/image/photo/";
	            Path filePath = Paths.get(uploadDir + fileName);
	            Files.copy(imageFile.getInputStream(), filePath);
	            
	            // 저장된 파일의 정보를 PhotoDTO에 설정합니다.
	            photoDTO.setUuid(UUID.randomUUID().toString());
	            photoDTO.setImgName(fileName);
	            photoDTO.setPath(uploadDir);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		
    	photoService.insertPhoto(photoDTO);
     
        // 업로드된 사진 정보를 응답으로 반환 16:51
	    return "redirect:/photo/photoList?groupNo=" + photoDTO.getGroupNo();
    }// end uploadPhoto
    
	/**
	 * 파일 저장 경로를 년/월/일 단위로 만들어주는 메소드
	 */
	private String makeFolder() {
	
	    String str = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
	
	    String folderPath =  str.replace("//", File.separator);
	
	    // make folder --------
	    File uploadPathFolder = new File(uploadPath, folderPath);
	
	    if (uploadPathFolder.exists() == false) {
	        uploadPathFolder.mkdirs();
	    }
	    return folderPath;
	}//end makeFolder
	
	@GetMapping("/display")
    public ResponseEntity<byte[]> getFile(String fileName) {

        ResponseEntity<byte[]> result = null;

        try {
            String srcFileName =  URLDecoder.decode(fileName,"UTF-8");

            log.info("fileName: " + srcFileName);

            File file = new File(uploadPath + File.separator + srcFileName);

            log.info("file: " + file);

            HttpHeaders header = new HttpHeaders();

            //MIME타입 처리
            header.add("Content-Type", Files.probeContentType(file.toPath()));
            //파일 데이터 처리
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return result;
    }
    
    @PostMapping("/removeFile")
    public ResponseEntity<Boolean> removeFile(String fileName){

        String srcFileName = null;
        try {
            srcFileName = URLDecoder.decode(fileName,"UTF-8");
            File file = new File(uploadPath +File.separator+ srcFileName);
            boolean result = file.delete();

            File thumbnail = new File(file.getParent(), "s_" + file.getName());

            result = thumbnail.delete();

            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(false, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
        
        

    /*
     * 사진 상세 페이지로 이동
     */
    @GetMapping("/photoRead/{photoNo}")
    public String readPhoto(@PathVariable("photoNo") Integer photoNo, Model model) {
        // 사진 번호에 해당하는 사진 정보 가져오기
        PhotoDTO photoDTO = photoService.readPhoto(photoNo);
        model.addAttribute("photoDTO", photoDTO);
        
        // 사진 번호에 해당하는 댓글 목록 가져오기
        List<PhotoCommentDTO> commentList = photoCommentService.getPhotoCommentList(photoNo);
        model.addAttribute("commentList", commentList);
        
        // 댓글 입력을 위한 PhotoCommentDTO 객체 생성
        PhotoCommentDTO photoCommentDTO = new PhotoCommentDTO();
        photoCommentDTO.setPhotoCommentNo(photoNo);
        model.addAttribute("photoCommentDTO", photoCommentDTO);
        
        return "photo/photoRead";
    }
    
    /*
     * 사진 삭제 처리
     */
    @GetMapping("/deletePhoto/{photoNo}")
    public String deletePhoto(@PathVariable("photoNo") Integer photoNo) {
        // 사진 삭제
        photoService.deletePhoto(photoNo);
        return "redirect:/group/photoList";
    }

    /*
	 * 댓글 삭제
	 */
	@PostMapping("/photoCommentDelete")
	@ResponseBody
	public String deletePhotoComment(Model model,
								@RequestBody Integer photoCommentNo,
								HttpSession session) {
		
		PhotoCommentDTO photoCommentDTO = PhotoCommentDTO.builder()
								.photoCommentNo(photoCommentNo)
								.build();
		
		boolean deleted = photoCommentService.deletePhotoComment(photoCommentNo);
		
		String result = "";
		
		if (deleted) {
			result = "success";
		} else {
			result = "fail";
		}
		
		return result;
	}
	
	/*
	 * 댓글 저장  
	 */
	@PostMapping("/phototCommentInsert")
	@ResponseBody
	public String insertPhotoComment (
								@RequestBody PhotoCommentData formData,
								HttpSession session,
								Model model) {
		
		Integer groupNo = formData.getGroupNo();
    	Integer userNo = formData.getUserNo();
    	String photoCommentContent = formData.getPhotoCommentContent(); 
		
    	
    	PhotoCommentDTO photoCommentDTO = PhotoCommentDTO.builder()
    							.photoCommentContent(photoCommentContent)
    							.groupNo(groupNo)
    							.userNo(userNo)
    							.build();
		
		PhotoComment photoComment = photoCommentService.insertPhotoComment(photoCommentDTO);
		
		String result = "";
		
		if(photoComment != null) {
			result = "success";
		} else {
			result = "fail";
		}
		
		return result;
	}
    
   
    
}//end PhotoController

@Data
class PhotoCommentData {
	
	private Integer groupNo;
	private Integer userNo;
	private String photoCommentContent;

}