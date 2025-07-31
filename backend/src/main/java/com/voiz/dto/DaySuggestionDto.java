package com.voiz.dto;

import com.voiz.vo.SpecialDay;
import com.voiz.vo.SpecialDaySuggest;

import lombok.Data;

@Data
public class DaySuggestionDto {
	private SpecialDay specialDay;
	private SpecialDaySuggest specialDaySuggest;
	private boolean hasSuggest = false;
	
	public DaySuggestionDto(SpecialDay specialDay, SpecialDaySuggest specialDaySuggest) {
        this.specialDay = specialDay;
        this.specialDaySuggest = specialDaySuggest;
        if (specialDaySuggest!=null) {
        	hasSuggest = true;
        }
    }
	
}
