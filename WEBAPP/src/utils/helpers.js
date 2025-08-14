import { COUNTRY_FLAGS } from "../types/index.js";

export const getCountryFlag = (countryCode) => {
  return COUNTRY_FLAGS[countryCode] || "🌐";
};

export const getCountryName = (countryCode) => {
  const countryNames = {
    US: "United States",
    KR: "South Korea",
    IT: "Italy",
    CA: "Canada",
    JP: "Japan",
    AU: "Australia",
    GB: "United Kingdom",
    FR: "France",
    NZ: "New Zealand",
    CN: "China",
    ES: "Spain",
    DE: "Germany",
    RU: "Russia",
    AE: "UAE",
    TH: "Thailand",
    VN: "Vietnam",
    SA: "Saudi Arabia",
  };
  return countryNames[countryCode] || countryCode;
};

export const getItemName = (item, selectedLang) => {
  if (item.name && typeof item.name === "object") {
    return item.name[selectedLang] || item.name.ko || item.name.en || "메뉴";
  }
  return item.name || "메뉴";
};

export const getItemDescription = (item, selectedLang) => {
  if (item.description && typeof item.description === "object") {
    return (
      item.description[selectedLang] ||
      item.description.ko ||
      item.description.en ||
      null
    );
  }
  return item.description || null;
};

export const getReviewText = (review, selectedLang, reviewViewMode) => {
  console.log(review);

  if (!review || !review.text) {
    return "";
  }

  if (reviewViewMode === "original") {
    // 리뷰어의 국가에 따라 원본 언어 결정
    return review.countryCode === "KR" ? review.text.ko : review.text.en;
  }

  // 선택된 언어로 표시
  return review.text[selectedLang] || review.text.en || review.text.ko || "";
};

export const formatPrice = (price, selectedLang) => {
  return selectedLang === "ko"
    ? `${price.toLocaleString()}원`
    : `₩${price.toLocaleString()}`;
};

export const formatOrderText = (count, lang) => {
  const orderTexts = {
    ko: `주문하기 (${count}개)`,
    en: `Place Order (${count} item(s))`,
    ja: `注文する (${count}個)`,
    zh: `下单 (${count}件)`,
    es: `Hacer Pedido (${count} artículo(s))`,
    fr: `Commander (${count} article(s))`,
    de: `Bestellen (${count} Stück)`,
    ru: `Заказать (${count} шт.)`,
    ar: `اطلب الآن (${count} قطعة)`,
    th: `สั่งซื้อ (${count} ชิ้น)`,
    vi: `Đặt Hàng (${count} món)`,
  };

  // 해당 언어의 텍스트가 없으면 영어로 기본값 설정
  return orderTexts[lang] || orderTexts.en;
};

export const getFilteredReviews = (reviews, selectedCountryFilter) => {
  if (selectedCountryFilter === "all") {
    return reviews;
  }
  return reviews.filter(
    (review) => (review.nationality || review.countryCode) === selectedCountryFilter
  );
};

export const getUniqueCountries = (reviews, getCountryName) => {
  const countries = reviews.map((review) => {
    const countryCode = review.nationality || review.countryCode;
    return {
      code: countryCode,
      name: getCountryName(countryCode),
    };
  });
  return [...new Map(countries.map((item) => [item.code, item])).values()];
};
