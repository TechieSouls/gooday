package com.cg.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cg.bo.Diary;

@Repository
public interface DiaryRepository extends CrudRepository<Diary, Long>{
	
	public List<Diary> findByCreatedByIdOrderByDiaryTimeDesc(Long createdById);
}
