package com.small.group.dto;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * Serializable 
 *  - 자바 직렬화란 자바 시스템 내부에서 사용되는 객체 
 *   또는 데이터를 외부의 자바 시스템에서도 사용할 수 있도록 바이트(byte) 형태로
 *   데이터 변환하는 기술과 바이트로 변환된 데이터를 다시 객체로 변환하는 기술(역직렬화)
 *  - 시스템적으로 이야기하자면 JVM(Java Virtual Machine 이하 JVM)의
 *   메모리에 상주(힙 또는 스택)되어 있는 객체 데이터를 바이트 형태로 변환하는 기술과 직렬화된 바이트 형태의 데이터를 객체로 변환해서 JVM으로 상주시키는 형태
 */

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDTO {

	private Integer photoNo;
	
	@NotBlank(message = "제목을 입력하세요")
	@Size(min = 1, max = 50, message = "제목은 최대 50글자수만 작성이 가능합니다.")
	private String photoTitle;
	
	@NotBlank(message = "내용을 입력하세요")
	@Size(min = 1, max = 500, message = "내용은 최대 500글자수만 작성이 가능합니다.")
	private String photoContent;

	private Integer groupNo;
	private String groupName;
	
	private Integer userNo;
	private String userName;
	
	private String uuid;
	private String imgName;
	private String path;
	
    public String getImageURL(){
        try {
            return URLEncoder.encode(path+"/"+uuid+"_"+imgName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
    public String getThumbnailURL(){
        try {
            return URLEncoder.encode(path+"/s_"+uuid+"_"+imgName,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

	private LocalDateTime regDate;
	private LocalDateTime modDate;
}
