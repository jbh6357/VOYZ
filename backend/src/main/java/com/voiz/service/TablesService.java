package com.voiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.mapper.TablesRepository;
import com.voiz.vo.Tables;

@Service
public class TablesService {

	@Autowired
	private TablesRepository tablesRepository;
	
	public List<Tables> getTablesByUserId(String userId) {	
	    return tablesRepository.findByUserId(userId);
	}

}
