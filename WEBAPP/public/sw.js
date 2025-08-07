// Service Worker for Push Notifications
self.addEventListener('install', (event) => {
  console.log('Service Worker installing...');
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  console.log('Service Worker activating...');
  event.waitUntil(self.clients.claim());
});

// 푸시 메시지 수신 처리
self.addEventListener('push', (event) => {
  console.log('Push received:', event);
  
  let data = {};
  try {
    data = event.data ? event.data.json() : {};
  } catch (e) {
    console.log('Push data parse error:', e);
  }

  const options = {
    title: data.title || 'VOYZ Restaurant',
    body: data.body || '리뷰를 작성해 주세요! 🍽️',
    icon: '/favicon.ico',
    badge: '/favicon.ico',
    data: {
      url: data.url || '/',
      action: data.action || 'review'
    },
    actions: [
      {
        action: 'review',
        title: '리뷰 작성하기'
      },
      {
        action: 'dismiss',
        title: '나중에'
      }
    ],
    requireInteraction: true,
    tag: 'review-reminder'
  };

  event.waitUntil(
    self.registration.showNotification(options.title, options)
  );
});

// 알림 클릭 처리
self.addEventListener('notificationclick', (event) => {
  console.log('Notification clicked:', event);
  
  event.notification.close();
  
  if (event.action === 'review') {
    // 리뷰 페이지로 바로 이동
    event.waitUntil(
      clients.openWindow('/?page=review')
    );
  } else if (event.action === 'dismiss') {
    // 알림만 닫기
    return;
  } else {
    // 기본 클릭 - 리뷰 페이지로 이동
    event.waitUntil(
      clients.openWindow('/?page=review')
    );
  }
});

// 백그라운드 동기화 (선택적)
self.addEventListener('sync', (event) => {
  if (event.tag === 'review-reminder') {
    console.log('Background sync for review reminder');
    // 여기에 추가적인 동기화 로직을 넣을 수 있습니다
  }
});