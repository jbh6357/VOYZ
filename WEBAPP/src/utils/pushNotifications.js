// 웹 푸시 알림 관련 유틸리티 함수들

// VAPID 공개 키 (실제 프로덕션에서는 환경변수로 관리)
const VAPID_PUBLIC_KEY = 'BMUlRpAMJy1VfLrmZMvmC8I0JpbVCJoFVBKrGTGWG3dj4xzH9-7ZKJFgBvpvD5pGqc7rZMXLKV0Df3rN8K9Ow7Y'

// Base64 문자열을 Uint8Array로 변환
function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4)
  const base64 = (base64String + padding)
    .replace(/-/g, '+')
    .replace(/_/g, '/')

  const rawData = window.atob(base64)
  const outputArray = new Uint8Array(rawData.length)

  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i)
  }
  return outputArray
}

// 브라우저가 푸시 알림을 지원하는지 확인
export function isPushNotificationSupported() {
  return 'serviceWorker' in navigator && 'PushManager' in window
}

// Service Worker 등록
export async function registerServiceWorker() {
  if (!isPushNotificationSupported()) {
    throw new Error('Push notifications are not supported')
  }

  try {
    const registration = await navigator.serviceWorker.register('/sw.js')
    console.log('Service Worker registered:', registration)
    return registration
  } catch (error) {
    console.error('Service Worker registration failed:', error)
    throw error
  }
}

// 알림 권한 요청
export async function requestNotificationPermission() {
  if (!isPushNotificationSupported()) {
    return false
  }

  if (Notification.permission === 'granted') {
    return true
  }

  if (Notification.permission === 'denied') {
    return false
  }

  const permission = await Notification.requestPermission()
  return permission === 'granted'
}

// 푸시 구독 생성
export async function subscribeToPush(registration) {
  try {
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY)
    })
    
    console.log('Push subscription created:', subscription)
    return subscription
  } catch (error) {
    console.error('Failed to subscribe to push:', error)
    throw error
  }
}

// 서버에 구독 정보 저장 (실제 구현에서는 백엔드 API 호출)
export async function savePushSubscription(subscription, userId = null) {
  try {
    // 실제 프로덕션에서는 백엔드 API 호출
    const subscriptionData = {
      subscription: subscription,
      userId: userId,
      timestamp: new Date().toISOString()
    }
    
    // 로컬 스토리지에 임시 저장 (개발용)
    localStorage.setItem('pushSubscription', JSON.stringify(subscriptionData))
    console.log('Push subscription saved:', subscriptionData)
    
    return true
  } catch (error) {
    console.error('Failed to save push subscription:', error)
    return false
  }
}

// 알림 권한 상태 확인
export function getNotificationPermission() {
  if (!isPushNotificationSupported()) {
    return 'not-supported'
  }
  return Notification.permission
}

// 푸시 알림 초기화 (앱 시작 시 호출)
export async function initializePushNotifications() {
  try {
    if (!isPushNotificationSupported()) {
      console.log('Push notifications not supported')
      return { success: false, reason: 'not-supported' }
    }

    const registration = await registerServiceWorker()
    
    if (Notification.permission === 'granted') {
      const subscription = await subscribeToPush(registration)
      await savePushSubscription(subscription)
      return { success: true, subscription }
    }

    return { success: false, reason: 'permission-not-granted' }
  } catch (error) {
    console.error('Failed to initialize push notifications:', error)
    return { success: false, reason: 'error', error }
  }
}

// 즉시 테스트 알림 보내기 (개발용)
export function sendTestNotification(title = '테스트 알림', body = '알림이 정상적으로 작동합니다!') {
  if ('serviceWorker' in navigator && 'PushManager' in window) {
    navigator.serviceWorker.ready.then(function(registration) {
      registration.showNotification(title, {
        body: body,
        icon: '/favicon.ico',
        tag: 'test-notification'
      })
    })
  }
}

// 리뷰 리마인더 스케줄링 (로컬 - 실제로는 서버에서 처리)
export function scheduleReviewReminder(orderDetails, delayMinutes = 60) {
  const subscriptionData = localStorage.getItem('pushSubscription')
  if (!subscriptionData) {
    console.log('No push subscription found')
    return false
  }

  // 실제 프로덕션에서는 서버에서 처리
  console.log(`리뷰 리마인더가 ${delayMinutes}분 후에 예약되었습니다:`, orderDetails)
  
  // 개발/테스트용: 10초 후 알림 (실제로는 1시간)
  setTimeout(() => {
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.ready.then(registration => {
        registration.showNotification('VOYZ Restaurant', {
          body: '주문하신 음식은 어떠셨나요? 리뷰를 남겨주세요! 🍽️',
          icon: '/favicon.ico',
          data: { action: 'review', url: '/' },
          actions: [
            { action: 'review', title: '리뷰 작성하기' },
            { action: 'dismiss', title: '나중에' }
          ],
          requireInteraction: true,
          tag: 'review-reminder'
        })
      })
    }
  }, 10000) // 테스트용 10초 (실제로는 delayMinutes * 60 * 1000)

  return true
}