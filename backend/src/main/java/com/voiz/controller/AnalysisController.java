package com.voiz.controller;

import com.voiz.service.FastApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/predict")
public class AnalysisController {
    
    @Autowired
    private FastApiClient fastApiClient;
    
    @GetMapping("/data")
    public ResponseEntity<String> getDataFromFastApi(@RequestParam String endpoint) {
        try {
            ResponseEntity<String> response = fastApiClient.getDataFromFastApi(endpoint);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("FastAPI 연결 실패: " + e.getMessage());
        }
    }
    
    @PostMapping("/predict")
    public ResponseEntity<String> getPrediction(
            @RequestParam String modelName,
            @RequestBody Map<String, Object> inputData) {
        try {
            ResponseEntity<String> response = fastApiClient.getPrediction(modelName, inputData);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("예측 요청 실패: " + e.getMessage());
        }
    }
    
    @PostMapping("/analyze")
    public ResponseEntity<String> getAnalysis(
            @RequestParam String analysisType,
            @RequestBody Map<String, Object> parameters) {
        try {
            ResponseEntity<String> response = fastApiClient.getAnalysisResult(analysisType, parameters);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("분석 요청 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<String> testFastApiConnection() {
        try {
            ResponseEntity<String> response = fastApiClient.getDataFromFastApi("/health");
            return ResponseEntity.ok("FastAPI 연결 성공: " + response.getBody());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("FastAPI 연결 실패: " + e.getMessage());
        }
    }
} 