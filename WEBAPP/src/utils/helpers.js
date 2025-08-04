import { COUNTRY_FLAGS } from '../types/index.js'

export const getCountryFlag = (countryCode) => {
  return COUNTRY_FLAGS[countryCode] || '[??]'
}

export const getItemName = (item, selectedLang) => {
  const nameKey = `name${selectedLang.charAt(0).toUpperCase() + selectedLang.slice(1)}`
  return item[nameKey] || item.name
}

export const getItemDescription = (item, selectedLang) => {
  const descKey = `description${selectedLang.charAt(0).toUpperCase() + selectedLang.slice(1)}`
  return item[descKey] || item.description
}

export const getReviewText = (review, selectedLang, reviewViewMode) => {
  if (reviewViewMode === 'original') {
    return review.originalText
  }
  
  if (review.translations && review.translations[selectedLang]) {
    return review.translations[selectedLang]
  }
  
  return review.originalText
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
  const countries = reviews.map(review => ({
    code: review.countryCode,
    name: review.country,
    flag: getCountryFlag(review.countryCode)
  }))
  return [...new Map(countries.map(item => [item.code, item])).values()]
}