package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.service.TablesService;
import com.voiz.vo.Tables;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tables")
@Tag(name = "tables", description = "테이블 관리 API")
public class TablesController {
	
	@Autowired
	private TablesService tablesService;
	
	@PostMapping("/{userId}")
    @Operation(summary = "테이블 목록 조회", description = "userId에 해당하는 모든 테이블 정보를 반환합니다.")
    public ResponseEntity<List<Tables>> getTablesByUserId(
            @PathVariable String userId) {
        
        List<Tables> tables = tablesService.getTablesByUserId(userId);
        return ResponseEntity.ok(tables);
    }

}
