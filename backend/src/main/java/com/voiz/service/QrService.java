package com.voiz.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.voiz.mapper.TablesRepository;
import com.voiz.mapper.UsersRepository;
import com.voiz.vo.Tables;
import com.voiz.vo.Users;

@Service
public class QrService {
	
	private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String BASE_URL = "https://voyz.com/qr";
    
    @Autowired
    private TablesRepository tablesRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    public void generateQRUrl(String userId, int number) {
    	
    	Optional<Users> optionalUser = usersRepository.findByUserId(userId);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("해당 아이디가 존재하지 않습니다.");
        }
        
        // 1. 기존 테이블 중 가장 큰 tableNumber 가져오기
        Integer maxTableNumber = tablesRepository.findMaxTableNumberByUserId(userId);
        if (maxTableNumber == null) {
            maxTableNumber = 0;
        }
        
    	for(int i=1; i<=number; i++) {
    		int newTableNumber = maxTableNumber + i;

            Tables table = new Tables();
            table.setUserId(userId);
            table.setTableNumber(newTableNumber);
            table.setQrCode(BASE_URL + "/" + userId + "/" + newTableNumber);

            tablesRepository.save(table);
    	}
    }
    
	public byte[] generateQRCode(int tableIdx) throws WriterException, IOException {
		Optional<Tables> optionalTable = tablesRepository.findById(tableIdx);
		
		if (optionalTable.isEmpty()) {
            throw new RuntimeException("해당 테이블이 존재하지 않습니다.");
        }
		
		Tables table = optionalTable.get();
		String text = table.getQrCode();
		
		return generateQRCode(text, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}
	
    /**
     * QR 코드를 생성하고 바이트 배열로 반환
     */
    public byte[] generateQRCode(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        // QR 코드 설정
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        // BitMatrix 생성
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        
        // BufferedImage로 변환
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        
        // 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        
        return baos.toByteArray();
    }
}
