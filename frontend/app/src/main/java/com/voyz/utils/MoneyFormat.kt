package com.voyz.utils

object MoneyFormats {
    /**
     * 금액을 한국형 짧은 단위로 축약해 표기합니다.
     * 예) 9,000 -> 9천, 90,000 -> 9만, 900,000 -> 9십만, 9,000,000 -> 9백만,
     *    20,000,000 -> 2천만, 123,000,000 -> 1억(반올림 처리)
     */
    @JvmStatic
    fun formatShortKoreanMoney(amount: Number): String {
        val n = amount.toLong()
        if (n < 10_000L) return "${n}원"
        if (n < 100_000_000L) return "${n / 10_000}만원"
        val eok = n / 100_000_000
        val man = (n % 100_000_000) / 10_000
        return if (man > 0) "${eok}억 ${man}만원" else "${eok}억원"
    }
}


