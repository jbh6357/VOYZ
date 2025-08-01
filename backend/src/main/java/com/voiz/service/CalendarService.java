package com.voiz.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.DaySuggestionDto;
import com.voiz.dto.ForecastResponseDto;
import com.voiz.dto.ReminderDto;
import com.voiz.dto.WeatherDto;
import com.voiz.mapper.CalendarRepository;
import com.voiz.mapper.MarketingRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.mapper.SpecialDayRepository;
import com.voiz.mapper.SpecialDaySuggestRepository;
import com.voiz.mapper.UsersRepository;
import com.voiz.mapper.WeatherRepository;
import com.voiz.util.GeoConverter;
import com.voiz.vo.Marketing;
import com.voiz.vo.SpecialDaySuggest;
import com.voiz.vo.Users;
import com.voiz.vo.Weather;

@Service
public class CalendarService {

	@Autowired
	private MarketingRepository marketingRepository;
	
	@Autowired
	private ReminderRepository reminderRepository;
	
	@Autowired
	private SpecialDaySuggestRepository specialDaySuggestRepository;
	
	@Autowired
	private SpecialDayRepository specialDayRepository;
	
	@Autowired
	private CalendarRepository calendarRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private GeoConverter geoConverter;
	
	@Autowired
	private WeatherRepository weatherRepository;

	public Marketing getMarketing(int marketingIdx) {
		Optional<Marketing> marketing = marketingRepository.findByMarketingIdx(marketingIdx);
		if(marketing.isPresent()) {
			return marketing.get();
		}else {
			return null;
		}
	}

	public void createReminder(int ssuIdx, String userId) {
		System.out.println("=== createReminder ===");
		System.out.println("userId: " + userId);
		System.out.println("ssuIdx: " + ssuIdx);

		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		System.out.println("reminderIdx: " + reminderIdx);
		
		Marketing marketing = new Marketing();
		
		Optional<SpecialDaySuggest> optionalSuggest = specialDaySuggestRepository.findById(ssuIdx);
		
		if(!optionalSuggest.isPresent()) {
			throw new RuntimeException("잘못된 제안 아이디 정보");
		}
		
		SpecialDaySuggest suggest = optionalSuggest.get();
		
		marketing.setContent(suggest.getContent());
		marketing.setTitle(suggest.getTitle());
		marketing.setStartDate(suggest.getStartDate());
		marketing.setEndDate(suggest.getEndDate());
		marketing.setReminder_idx(reminderIdx);
		marketing.setStatus("진행전");
		marketing.setType("1");
		
		marketing.setDescription(suggest.getDescription());
		marketing.setTargetCustomer(suggest.getTargetCustomer());
		marketing.setSuggestedAction(suggest.getSuggestedAction());
		marketing.setExpectedEffect(suggest.getExpectedEffect());
		marketing.setConfidence(suggest.getConfidence());
		marketing.setPriority(suggest.getPriority());
		
		System.out.println("Before save - startDate: " + marketing.getStartDate() + ", endDate: " + marketing.getEndDate());
		marketingRepository.save(marketing);
		System.out.println("After save - marketingIdx: " + marketing.getMarketingIdx());
		System.out.println("=== End createReminder ===");
	}

	public List<Marketing> getMarketingListByUserAndMonth(String userId, int year, int month) {
		System.out.println("=== getMarketingListByUserAndMonth ===");
		System.out.println("userId: " + userId + ", year: " + year + ", month: " + month);
		
		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		System.out.println("reminderIdx: " + reminderIdx);
		
		// 해당 월 기준 전월 ~ 다음월 범위 계산
	    YearMonth ym = YearMonth.of(year, month);
	    LocalDate from = ym.minusMonths(1).atDay(1);          // 전월 1일
	    LocalDate to = ym.plusMonths(1).atEndOfMonth();       // 다음월 말일
	    System.out.println("Date range: " + from + " ~ " + to);

	    List<Marketing> result = marketingRepository.findByReminderIdxAndDateRange(reminderIdx, from, to);
	    System.out.println("Query result count: " + result.size());
	    for (Marketing m : result) {
	    	System.out.println("Marketing: " + m.getMarketingIdx() + ", " + m.getTitle() + ", " + m.getStartDate() + "~" + m.getEndDate());
	    }
	    System.out.println("=== End getMarketingListByUserAndMonth ===");
	    
	    return result;
	}

	public SpecialDaySuggest getSpecialDaySuggestion(int ssuIdx) {
		Optional<SpecialDaySuggest> suggestion = specialDaySuggestRepository.findBySsuIdx(ssuIdx);
		if(suggestion.isPresent()) {
			return suggestion.get();
		}else {
			return null;
		}
	}

	public List<DaySuggestionDto> getDaySuggestionsByUserAndMonth(String userId, int year, int month) {
		int calendarIdx = calendarRepository.findCalendarIdxByUserId(userId);
		
		// 해당 월 기준 전월 ~ 다음월 범위 계산
	    YearMonth ym = YearMonth.of(year, month);
	    LocalDate from = ym.minusMonths(1).atDay(1);          // 전월 1일
	    LocalDate to = ym.plusMonths(1).atEndOfMonth();       // 다음월 말일
		
	    List<DaySuggestionDto> daySuggestionList = specialDayRepository.findSpecialDaysWithSuggestion(userId, calendarIdx, from, to);
		return daySuggestionList;
	}



	 public List<ForecastResponseDto<WeatherDto>> getWeatherForCalendar(String userId) {
        
        Users user = usersRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        String storeAddress = user.getStoreAddress();
        if (storeAddress == null || storeAddress.isBlank()) {
            return Collections.emptyList();
        }

        String dongName = geoConverter.convertAddressToCoordinates(storeAddress)
                .map(geoData -> geoData.dongName())
                .orElseThrow(() -> new RuntimeException("주소에서 '동' 정보를 추출할 수 없습니다: " + storeAddress));
        
        LocalDate now = LocalDate.now();
        YearMonth ym = YearMonth.of(now.getYear(), now.getMonthValue());
        LocalDate from = ym.minusMonths(1).atDay(1);
        LocalDate to = ym.plusMonths(1).atEndOfMonth();

        List<Weather> rawWeatherList = weatherRepository.findWeatherByDongNameAndDateRange(dongName, from, to);

        return rawWeatherList.stream()
            .collect(Collectors.groupingBy(Weather::getForecastDate))
            .values().stream()
            .map(dailyWeather -> {
                if (dailyWeather.isEmpty()) return null;

                // 1. 상세 데이터를 담은 WeatherDto 생성 (헬퍼 메서드 호출)
                WeatherDto detailsDto = createDailyWeatherDto(dailyWeather);

                // 2. 상세 데이터를 기반으로 자연어 문장 생성 (헬퍼 메서드 호출)
                String description = generateCalendarDescription(detailsDto);

                // 3. 문장과 상세 데이터를 최종 DTO(ForecastResponseDto)에 담아 반환
                return new ForecastResponseDto<>(description, detailsDto);
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(dto -> dto.getDetails().getForecastDate()))
            .collect(Collectors.toList());
    }
    
    /**
     * 하루치 날씨 리스트를 받아 대표 DTO를 생성하는 헬퍼 메서드
     */
    private WeatherDto createDailyWeatherDto(List<Weather> dailyWeather) {
        LocalDate date = dailyWeather.get(0).getForecastDate();
        Double tmx = dailyWeather.stream().map(Weather::getTmx).filter(Objects::nonNull).findFirst().orElseGet(() -> dailyWeather.stream().mapToDouble(Weather::getTemp).max().orElse(0.0));
        Double tmn = dailyWeather.stream().map(Weather::getTmn).filter(Objects::nonNull).findFirst().orElseGet(() -> dailyWeather.stream().mapToDouble(Weather::getTemp).min().orElse(0.0));
        Integer reh = (int) dailyWeather.stream().mapToInt(Weather::getReh).average().orElse(0);
        Integer pop = dailyWeather.stream().mapToInt(Weather::getPop).max().orElse(0);
        
        Weather representativeWeather = dailyWeather.stream().filter(w -> "1200".equals(w.getHour())).findFirst().orElse(dailyWeather.get(0));
        
        return new WeatherDto(date, tmx, tmn, reh, pop, representativeWeather.getSky(), representativeWeather.getPty());
    }

    /**
     * 캘린더에 표시될 자연어 예보 문장을 생성합니다.
     */
    private String generateCalendarDescription(WeatherDto forecast) {
        String skyState;
        switch (forecast.getSky()) {
            case 1: skyState = "맑음"; break;
            case 3: skyState = "구름많음"; break;
            case 4: skyState = "흐림"; break;
            default: skyState = "정보없음"; break;
        }

        if (forecast.getPty() > 0) {
            switch (forecast.getPty()) {
                case 1: skyState = "비"; break;
                case 2: skyState = "비/눈"; break;
                case 3: skyState = "눈"; break;
                case 4: skyState = "소나기"; break;
            }
        }

        return String.format(
            "%d월 %d일: 최고 %.0f°, 최저 %.0f°, %s",
            forecast.getForecastDate().getMonthValue(),
            forecast.getForecastDate().getDayOfMonth(),
            forecast.getTmx(),
            forecast.getTmn(),
            skyState
        );
    }


}
