package com.cg.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cg.bo.Diary;
import com.cg.repository.DiaryRepository;

@Controller
public class DiaryController {
	
	@Autowired
	DiaryRepository diaryRepository;
	
	@Value("${cenes.diaryUploadPath}")
	private String diaryUploadPath;
	
	@Value("${cenes.domain}")
	private String domain;
	
	@RequestMapping(value="/api/diary/save",method=RequestMethod.POST)
	public ResponseEntity<Map<String,Object>> saveDairy(@RequestBody Diary diary) {

		if (diary != null && diary.getDiaryId() != null) {
			diary.setCreatedAt(new Date());
		}
		Map<String,Object> response = new HashMap<>();
		try {
			
			diary = diaryRepository.save(diary);
			response.put("success", true);
			response.put("data", diary);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("data", new Diary());
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}
	
	@RequestMapping(value="/api/diary/delete",method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> deleteDiary(@RequestParam("diaryId") Long diaryId) {
		Map<String,Object> response = new HashMap<>();
		try {
			
			diaryRepository.delete(diaryId);
			response.put("success", true);
			response.put("message", "Diary Deleted SuccessFully");
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("message", "Diary Can not be deleted.");
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}

	
	@RequestMapping(value="/api/diary/list",method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getDiaries(@RequestParam("userId") Long userId) {
		Map<String,Object> response = new HashMap<>();
		try {
			List<Diary> diaries = diaryRepository.findByCreatedByIdOrderByDiaryTimeDesc(userId);
			if (diaries != null && diaries.size() > 0) {
				response.put("data", diaries);
			} else {
				response.put("data", new ArrayList<>());
			}
			response.put("success", true);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
		} catch(Exception e){
			e.printStackTrace();
			response.put("success", false);
			response.put("data", new Diary());
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}

	@RequestMapping(value="/api/diary/get",method=RequestMethod.GET)
	public ResponseEntity<Map<String,Object>> getDiaryById(@RequestParam("diaryId") Long diaryId) {
		Map<String,Object> response = new HashMap<>();
		try {
			Diary diary = diaryRepository.findOne(diaryId);
			response.put("success", true);
			response.put("data", diary);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
			if (diary == null) {
				response.put("success", false);
				response.put("data", null);
				response.put("errorCode", HttpStatus.NOT_FOUND.ordinal());
				response.put("errorDetail", HttpStatus.NOT_FOUND.toString());
			}
		} catch(Exception e) {
			e.printStackTrace();
			response.put("success", false);
			response.put("data", new Diary());
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.toString());
		}
		return new ResponseEntity<Map<String,Object>>(response,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/api/diary/upload", method = RequestMethod.POST)
    public ResponseEntity<Map<String,Object>> uploadDiaryImages(@RequestParam("mediaFile") MultipartFile uploadfile,@RequestParam("userId") String userId) {
		Map<String,Object> response = new HashMap<>();
		String dirPath = diaryUploadPath.replaceAll("\\[userId\\]", userId);
		
		InputStream inputStream = null;
        OutputStream outputStream = null;
        String extension = uploadfile.getOriginalFilename().substring(uploadfile.getOriginalFilename().trim().lastIndexOf("."),uploadfile.getOriginalFilename().length());
        
        String fileName = UUID.randomUUID().toString()+extension;

        File f = new File(dirPath);
        if(!f.exists()) { 
        	try {
				f.mkdirs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }        
        File newFile = new File(dirPath+fileName);
        try {
            inputStream = uploadfile.getInputStream();

            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            outputStream = new FileOutputStream(newFile);
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
           
            String diaryPicUrl = "http://"+domain+"/assets/uploads/"+userId+"/diary/"+fileName;
            
            response.put("success", true);
			response.put("data", diaryPicUrl);
			response.put("errorCode", 0);
			response.put("errorDetail", null);
        } catch (Exception e) {
        	e.printStackTrace();
            response.put("success", false);
			response.put("data", "");
			response.put("errorCode", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			response.put("errorDetail", HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
        }
        return new ResponseEntity<Map<String,Object>>(response, HttpStatus.OK);
    }


}
