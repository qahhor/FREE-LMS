// FREE LMS Service Worker
// Version 1.0.0

const CACHE_NAME = 'free-lms-v1';
const DATA_CACHE_NAME = 'free-lms-data-v1';

// Static files to cache on install
const FILES_TO_CACHE = [
  '/',
  '/index.html',
  '/styles.css',
  '/main.js',
  '/polyfills.js',
  '/runtime.js',
  '/assets/icons/icon-192x192.png',
  '/assets/icons/icon-512x512.png',
  '/offline.html', // Offline fallback page
];

// API endpoints that should be cached
const API_CACHE_PATTERNS = [
  /\/api\/v1\/courses/,
  /\/api\/v1\/users\/me/,
  /\/api\/v1\/enrollments/,
];

// Install event - cache static files
self.addEventListener('install', (event) => {
  console.log('[ServiceWorker] Install');

  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[ServiceWorker] Caching app shell');
      return cache.addAll(FILES_TO_CACHE);
    })
  );

  self.skipWaiting();
});

// Activate event - clean up old caches
self.addEventListener('activate', (event) => {
  console.log('[ServiceWorker] Activate');

  event.waitUntil(
    caches.keys().then((keyList) => {
      return Promise.all(
        keyList.map((key) => {
          if (key !== CACHE_NAME && key !== DATA_CACHE_NAME) {
            console.log('[ServiceWorker] Removing old cache', key);
            return caches.delete(key);
          }
        })
      );
    })
  );

  self.clients.claim();
});

// Fetch event - serve from cache, fallback to network
self.addEventListener('fetch', (event) => {
  // Skip cross-origin requests
  if (!event.request.url.startsWith(self.location.origin)) {
    return;
  }

  const { request } = event;
  const url = new URL(request.url);

  // Handle API requests
  if (url.pathname.startsWith('/api/')) {
    event.respondWith(
      caches.open(DATA_CACHE_NAME).then((cache) => {
        return fetch(request)
          .then((response) => {
            // Only cache GET requests with successful responses
            if (request.method === 'GET' && response.status === 200) {
              // Check if URL matches cacheable API patterns
              const shouldCache = API_CACHE_PATTERNS.some(pattern =>
                pattern.test(url.pathname)
              );

              if (shouldCache) {
                cache.put(request, response.clone());
              }
            }
            return response;
          })
          .catch(() => {
            // If network fails, try to return cached version
            return cache.match(request).then((cachedResponse) => {
              if (cachedResponse) {
                console.log('[ServiceWorker] Returning cached API response');
                return cachedResponse;
              }
              // Return offline message for failed API calls
              return new Response(
                JSON.stringify({
                  error: 'offline',
                  message: 'Нет подключения к интернету. Попробуйте позже.'
                }),
                {
                  headers: { 'Content-Type': 'application/json' },
                  status: 503,
                }
              );
            });
          });
      })
    );
    return;
  }

  // Handle app shell requests
  event.respondWith(
    caches.match(request).then((response) => {
      if (response) {
        console.log('[ServiceWorker] Returning cached response', request.url);
        return response;
      }

      // Try to fetch from network
      return fetch(request)
        .then((response) => {
          // Don't cache non-successful responses
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response;
          }

          // Clone the response
          const responseToCache = response.clone();

          caches.open(CACHE_NAME).then((cache) => {
            cache.put(request, responseToCache);
          });

          return response;
        })
        .catch(() => {
          // If both cache and network fail, show offline page
          if (request.destination === 'document') {
            return caches.match('/offline.html');
          }
        });
    })
  );
});

// Background sync
self.addEventListener('sync', (event) => {
  console.log('[ServiceWorker] Background sync', event.tag);

  if (event.tag === 'sync-progress') {
    event.waitUntil(syncProgressData());
  }
});

// Push notifications
self.addEventListener('push', (event) => {
  console.log('[ServiceWorker] Push notification received', event);

  const data = event.data ? event.data.json() : {};
  const title = data.title || 'FREE LMS';
  const options = {
    body: data.body || 'У вас новое уведомление',
    icon: '/assets/icons/icon-192x192.png',
    badge: '/assets/icons/badge-72x72.png',
    data: data.url || '/',
    actions: [
      {
        action: 'open',
        title: 'Открыть',
      },
      {
        action: 'close',
        title: 'Закрыть',
      },
    ],
  };

  event.waitUntil(
    self.registration.showNotification(title, options)
  );
});

// Notification click
self.addEventListener('notificationclick', (event) => {
  console.log('[ServiceWorker] Notification clicked', event);

  event.notification.close();

  if (event.action === 'open' || !event.action) {
    const urlToOpen = event.notification.data;

    event.waitUntil(
      clients.matchAll({ type: 'window', includeUncontrolled: true })
        .then((clientList) => {
          // Check if there's already a window open
          for (const client of clientList) {
            if (client.url === urlToOpen && 'focus' in client) {
              return client.focus();
            }
          }
          // Open new window if none exist
          if (clients.openWindow) {
            return clients.openWindow(urlToOpen);
          }
        })
    );
  }
});

// Helper functions
async function syncProgressData() {
  try {
    // Get pending progress updates from IndexedDB
    const db = await openProgressDB();
    const pendingUpdates = await getPendingUpdates(db);

    // Send each update to server
    for (const update of pendingUpdates) {
      const response = await fetch('/api/v1/progress', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(update.data),
      });

      if (response.ok) {
        // Remove from pending updates
        await removePendingUpdate(db, update.id);
      }
    }

    console.log('[ServiceWorker] Progress data synced');
  } catch (error) {
    console.error('[ServiceWorker] Error syncing progress data', error);
    throw error; // Retry later
  }
}

function openProgressDB() {
  return new Promise((resolve, reject) => {
    const request = indexedDB.open('FreeLMSProgress', 1);

    request.onerror = () => reject(request.error);
    request.onsuccess = () => resolve(request.result);

    request.onupgradeneeded = (event) => {
      const db = event.target.result;
      if (!db.objectStoreNames.contains('pending')) {
        db.createObjectStore('pending', { keyPath: 'id', autoIncrement: true });
      }
    };
  });
}

function getPendingUpdates(db) {
  return new Promise((resolve, reject) => {
    const transaction = db.transaction(['pending'], 'readonly');
    const store = transaction.objectStore('pending');
    const request = store.getAll();

    request.onerror = () => reject(request.error);
    request.onsuccess = () => resolve(request.result);
  });
}

function removePendingUpdate(db, id) {
  return new Promise((resolve, reject) => {
    const transaction = db.transaction(['pending'], 'readwrite');
    const store = transaction.objectStore('pending');
    const request = store.delete(id);

    request.onerror = () => reject(request.error);
    request.onsuccess = () => resolve();
  });
}

console.log('[ServiceWorker] Loaded');
