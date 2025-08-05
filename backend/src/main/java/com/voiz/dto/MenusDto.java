package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenusDto {
	private String menuName;
	private int menuPrice;
	private String menuDescription;
	private String imageUrl;
}
