import { COUNTRY_FLAGS } from '../types/index.js'

export const getCountryFlag = (countryCode) => {
  return COUNTRY_FLAGS[countryCode] || '[??]'
}

export const getItemName = (item, selectedLang) => {
  if (item.name && typeof item.name === 'object') {
    return item.name[selectedLang] || item.name.ko || item.name.en || '메뉴'
  }
  return item.name || '메뉴'
}

export const getItemDescription = (item, selectedLang) => {
  if (item.description && typeof item.description === 'object') {
    return item.description[selectedLang] || item.description.ko || item.description.en || null
  }
  return item.description || null
}

export const getReviewText = (review, selectedLang, reviewViewMode) => {
  if (!review || !review.text) {
    return ''
  }
  
  if (reviewViewMode === 'original') {
    // 리뷰어의 국가에 따라 원본 언어 결정
    return review.countryCode === 'KR' ? review.text.ko : review.text.en
  }
  
  // 선택된 언어로 표시
  return review.text[selectedLang] || review.text.en || review.text.ko || ''
}

export const formatPrice = (price, selectedLang) => {
  return selectedLang === 'ko' ? `${price.toLocaleString()}원` : `₩${price.toLocaleString()}`
}

export const getFilteredReviews = (reviews, selectedCountryFilter) => {
  if (selectedCountryFilter === 'all') {
    return reviews
  }
  return reviews.filter(review => review.countryCode === selectedCountryFilter)
}

export const getUniqueCountries = (reviews, getCountryFlag) => {
  const countryNames = {
    'US': 'United States',
    'KR': 'South Korea',
    'IT': 'Italy',
    'CA': 'Canada',
    'JP': 'Japan',
    'AU': 'Australia',
    'GB': 'United Kingdom',
    'FR': 'France',
    'NZ': 'New Zealand',
    'CN': 'China'
  }
  
  const countries = reviews.map(review => ({
    code: review.countryCode,
    name: countryNames[review.countryCode] || review.countryCode,
    flag: getCountryFlag(review.countryCode)
  }))
  return [...new Map(countries.map(item => [item.code, item])).values()]
}