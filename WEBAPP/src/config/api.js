// API Configuration
export const API_CONFIG = {
  BASE_URL: process.env.NODE_ENV === 'production' 
    ? 'http://13.125.251.36:8081/api' 
    : 'http://localhost:8081/api',
  TIMEOUT: 10000,
  HEADERS: {
    'Content-Type': 'application/json',
  }
};