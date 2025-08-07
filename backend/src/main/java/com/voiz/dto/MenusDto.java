package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenusDto {
	private int menuIdx;
	private String menuName;
	private int menuPrice;
	private String menuDescription;
	private String imageUrl;
	private String category;
}
