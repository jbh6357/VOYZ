package com.voyz.presentation.screen.management.review.util

object NationalityFlagMapper {
    private val map = mapOf(
        // 기존 국가들
        "한국" to "\uD83C\uDDF0\uD83C\uDDF7",
        "미국" to "\uD83C\uDDFA\uD83C\uDDF8", 
        "일본" to "\uD83C\uDDEF\uD83C\uDDF5",
        "중국" to "\uD83C\uDDE8\uD83C\uDDF3",
        "영국" to "\uD83C\uDDEC\uD83C\uDDE7",
        "독일" to "\uD83C\uDDE9\uD83C\uDDEA",
        
        // 웹앱 번역 지원 추가 국가들
        "스페인" to "\uD83C\uDDEA\uD83C\uDDF8",
        "프랑스" to "\uD83C\uDDEB\uD83C\uDDF7",
        "러시아" to "\uD83C\uDDF7\uD83C\uDDFA",
        "사우디아라비아" to "\uD83C\uDDF8\uD83C\uDDE6",
        "아랍에미리트" to "\uD83C\uDDE6\uD83C\uDDEA",
        "태국" to "\uD83C\uDDF9\uD83C\uDDED",
        "베트남" to "\uD83C\uDDFB\uD83C\uDDF3",
        "캐나다" to "\uD83C\uDDE8\uD83C\uDDE6",
        "호주" to "\uD83C\uDDE6\uD83C\uDDFA",
        "뉴질랜드" to "\uD83C\uDDF3\uD83C\uDDFF",
        "이탈리아" to "\uD83C\uDDEE\uD83C\uDDF9",
        
        // 국가 코드 지원
        "KR" to "\uD83C\uDDF0\uD83C\uDDF7",
        "US" to "\uD83C\uDDFA\uD83C\uDDF8",
        "JP" to "\uD83C\uDDEF\uD83C\uDDF5",
        "CN" to "\uD83C\uDDE8\uD83C\uDDF3",
        "GB" to "\uD83C\uDDEC\uD83C\uDDE7",
        "DE" to "\uD83C\uDDE9\uD83C\uDDEA",
        "ES" to "\uD83C\uDDEA\uD83C\uDDF8",
        "FR" to "\uD83C\uDDEB\uD83C\uDDF7",
        "RU" to "\uD83C\uDDF7\uD83C\uDDFA",
        "SA" to "\uD83C\uDDF8\uD83C\uDDE6",
        "AE" to "\uD83C\uDDE6\uD83C\uDDEA",
        "TH" to "\uD83C\uDDF9\uD83C\uDDED",
        "VN" to "\uD83C\uDDFB\uD83C\uDDF3",
        "CA" to "\uD83C\uDDE8\uD83C\uDDE6",
        "AU" to "\uD83C\uDDE6\uD83C\uDDFA",
        "NZ" to "\uD83C\uDDF3\uD83C\uDDFF",
        "IT" to "\uD83C\uDDEE\uD83C\uDDF9",
        
        // 영문 이름도 지원
        "Korea" to "\uD83C\uDDF0\uD83C\uDDF7",
        "United States" to "\uD83C\uDDFA\uD83C\uDDF8",
        "USA" to "\uD83C\uDDFA\uD83C\uDDF8",
        "Japan" to "\uD83C\uDDEF\uD83C\uDDF5",
        "China" to "\uD83C\uDDE8\uD83C\uDDF3",
        "United Kingdom" to "\uD83C\uDDEC\uD83C\uDDE7",
        "UK" to "\uD83C\uDDEC\uD83C\uDDE7",
        "Germany" to "\uD83C\uDDE9\uD83C\uDDEA",
        "Spain" to "\uD83C\uDDEA\uD83C\uDDF8",
        "France" to "\uD83C\uDDEB\uD83C\uDDF7",
        "Russia" to "\uD83C\uDDF7\uD83C\uDDFA",
        "Saudi Arabia" to "\uD83C\uDDF8\uD83C\uDDE6",
        "UAE" to "\uD83C\uDDE6\uD83C\uDDEA",
        "Thailand" to "\uD83C\uDDF9\uD83C\uDDED",
        "Vietnam" to "\uD83C\uDDFB\uD83C\uDDF3",
        "Canada" to "\uD83C\uDDE8\uD83C\uDDE6",
        "Australia" to "\uD83C\uDDE6\uD83C\uDDFA",
        "New Zealand" to "\uD83C\uDDF3\uD83C\uDDFF",
        "Italy" to "\uD83C\uDDEE\uD83C\uDDF9"
    )

    fun flagFor(nationality: String): String = map[nationality] ?: "\uD83C\uDF10"
}


