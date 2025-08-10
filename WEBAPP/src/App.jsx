import { useState, useEffect, useRef, useCallback } from "react";

// Components
import LanguageSelector from "./components/UI/LanguageSelector.jsx";
import MenuSection from "./components/Menu/MenuSection.jsx";
import OrderPage from "./pages/OrderPage/index.jsx";
import ReviewModal from "./components/Review/ReviewModal.jsx";
import WriteReviewPage from "./pages/ReviewPage/index.jsx";
import NotificationPermissionModal from "./components/UI/NotificationPermissionModal.jsx";
import PaymentModal from "./components/Payment/PaymentModal.jsx";
import TossPaymentWidget from "./components/Payment/TossPaymentWidget.jsx";
import PayPalPaymentWidget from "./components/Payment/PayPalPaymentWidget.jsx";
import SuccessPage from "./pages/SuccessPage/index.jsx";

// Data & Utils
import { sampleMenuData } from "./constants/sampleData.js";
import { getMenusByUserId, getUrlParams } from "./api/menu.js";
import { getReviewsByMenuId } from "./api/review.js";
import { useMenu } from "./hooks/useMenu.js";
import { formatPrice } from "./utils/helpers.js";
import {
  isPushNotificationSupported,
  requestNotificationPermission,
  initializePushNotifications,
  scheduleReviewReminder,
} from "./utils/pushNotifications.js";
import Cookies from "js-cookie";
import { formatOrderText } from "./utils/helpers.js";

function App() {
  console.log("🔄 App 렌더링");

  // 상태 관리
  const [menuData, setMenuData] = useState(sampleMenuData); // 기본값으로 샘플 데이터 사용
  // const [userId, setUserId] = useState(null);
  // const [tableNumber, setTableNumber] = useState(null);
  const userIdRef = useRef(null);
  const tableNumberRef = useRef(null);
  const [isLoadingMenu, setIsLoadingMenu] = useState(false);
  const [menuError, setMenuError] = useState(null);
  const { userId: urlUserId, table: urlTable } = getUrlParams();
  //   const [selectedLang, setSelectedLang] = useState("ko");
  const [isTranslating, setIsTranslating] = useState(false);
  // 쿠키에서 읽어서 초기값 설정 (없으면 'ko' 기본)
  const [selectedLang, setSelectedLang] = useState(() => {
    return Cookies.get("selectedLang") || "ko";
  });
  const [reviewViewMode, setReviewViewMode] = useState("translated");
  ////////////////
  // userId
  const cookieUserId = document.cookie
    .split("; ")
    .find((row) => row.startsWith("userId="))
    ?.split("=")[1];

  if (cookieUserId) {
    userIdRef.current = decodeURIComponent(cookieUserId);
  } else if (urlUserId != null) {
    userIdRef.current = urlUserId;
    const expires = new Date(
      Date.now() + 7 * 24 * 60 * 60 * 1000
    ).toUTCString();
    document.cookie = `userId=${encodeURIComponent(
      urlUserId
    )}; expires=${expires}; path=/`;
  }

  // tableNumber
  const cookieTable = document.cookie
    .split("; ")
    .find((row) => row.startsWith("tableNumber="))
    ?.split("=")[1];

  if (cookieTable) {
    tableNumberRef.current = decodeURIComponent(cookieTable);
  } else if (urlTable != null) {
    tableNumberRef.current = urlTable;
    const expires = new Date(
      Date.now() + 7 * 24 * 60 * 60 * 1000
    ).toUTCString();
    document.cookie = `tableNumber=${encodeURIComponent(
      urlTable
    )}; expires=${expires}; path=/`;
  }
  //////////////////

  // 번역
  useEffect(() => {
    console.log(selectedLang);

    async function translateAllVisibleText() {
      setIsTranslating(true);

      const textNodes = [];
      const walker = document.createTreeWalker(
        document.body,
        NodeFilter.SHOW_TEXT,
        {
          acceptNode: (node) => {
            // 사용자에게 보이는 텍스트만 필터링
            const parent = node.parentElement;
            // 번역 대상에서 제외할 요소들을 체크합니다.
            const isExcluded =
              (parent &&
                parent.tagName === "H2" &&
                parent.classList.contains("section-title")) ||
              (parent &&
                parent.tagName === "H1" &&
                parent.classList.contains("restaurant-name")) ||
              (parent && parent.closest(".lang-grid")) ||
              (parent && parent.closest(".country-select")) ||
              (parent && parent.closest(".reviews-list.original-mode"));

            if (isExcluded) {
              return NodeFilter.FILTER_REJECT; // 제외 조건에 해당하면 걸러냅니다.
            }
            // 스크립트, 스타일 태그 등은 제외
            if (
              parent &&
              !["SCRIPT", "STYLE"].includes(parent.tagName) &&
              parent.style.display !== "none"
            ) {
              return NodeFilter.FILTER_ACCEPT;
            }
            return NodeFilter.FILTER_REJECT;
          },
        },
        false
      );

      let node;
      while ((node = walker.nextNode())) {
        if (node.nodeValue.trim().length > 0) {
          textNodes.push(node);
        }
      }

      const textsToTranslate = textNodes.map((node) => node.nodeValue);

      try {
        // 백엔드 API에 번역 요청
        const res = await fetch("http://localhost:8000/api/translate2", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            texts: textsToTranslate,
            targetLanguage: selectedLang,
          }),
        });

        if (!res.ok) throw new Error("API translation failed.");

        const { translated_texts } = await res.json();

        // 번역된 텍스트를 DOM에 적용
        textNodes.forEach((node, index) => {
          node.nodeValue = translated_texts[index];
        });
      } catch (error) {
        console.error("Translation error:", error);
      } finally {
        setIsTranslating(false);
      }
    }

    // 초기 페이지 번역
    translateAllVisibleText();

    // DOM 변경을 감지하는 MutationObserver 설정
    const observer = new MutationObserver((mutationsList, observer) => {
      // 'childList'는 자식 노드 추가/제거를 감지합니다.
      // 'subtree: true'는 모든 하위 노드까지 감지합니다.
      for (const mutation of mutationsList) {
        if (mutation.type === "childList" && mutation.addedNodes.length > 0) {
          // 새로운 노드가 추가될 때마다 번역 로직을 다시 실행합니다.
          // 모달 창이 열리면 새로운 노드들이 추가되므로 이 조건에 해당합니다.
          translateAllVisibleText();
        }
      }
    });

    // document.body의 DOM 변경을 감지 시작합니다.
    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ["class"],
      characterData: true,
    });

    // 컴포넌트가 언마운트될 때 옵저버 연결을 해제합니다.
    return () => observer.disconnect();
  }, [selectedLang]);

  // 2. selectedLang 변경 시 쿠키 저장
  useEffect(() => {
    const expires = new Date(
      Date.now() + 365 * 24 * 60 * 60 * 1000
    ).toUTCString();
    document.cookie = `selectedLang=${encodeURIComponent(
      selectedLang
    )}; expires=${expires}; path=/`;
  }, [selectedLang]);

  const newUrl =
    "/?userId=" + userIdRef.current + "&table=" + tableNumberRef.current; // + '&lang=' + selectedLang;

  // 앱 로드 시 알림 권한 확인 및 요청 + URL 파라미터 체크
  useEffect(() => {
    const checkNotificationPermission = async () => {
      // 이미 권한을 요청했는지 확인
      const hasAsked = localStorage.getItem("hasAskedForNotification");
      if (hasAsked) {
        setHasAskedForNotification(true);
        // 이미 권한이 있다면 푸시 알림 초기화
        if (Notification.permission === "granted") {
          await initializePushNotifications();
        }
        return;
      }

      // 푸시 알림을 지원하는 브라우저에서만 모달 표시
      if (isPushNotificationSupported()) {
        // 1초 후 모달 표시 (사용자가 페이지에 적응할 시간)
        setTimeout(() => {
          setShowNotificationModal(true);
        }, 1000);
      }
    };

    // URL 파라미터에서 userId와 table 추출 및 메뉴 로드
    const loadMenuFromUrl = async () => {
      const { userId: urlUserId, table: urlTable } = getUrlParams();

      if (urlUserId) {
        console.log("🔗 URL에서 파라미터 감지:", {
          userId: urlUserId,
          table: urlTable,
        });

        userIdRef.current = urlUserId;
        tableNumberRef.current = urlTable;

        // 실제 메뉴 데이터 로드
        try {
          setIsLoadingMenu(true);
          setMenuError(null);

          const menuItems = await getMenusByUserId(urlUserId, selectedLang);

          if (menuItems && menuItems.length > 0) {
            // API에서 받은 데이터를 기존 sampleMenuData 형식으로 변환
            const transformedMenuData = transformApiMenuData(menuItems);
            setMenuData(transformedMenuData);
            console.log("✅ 실제 메뉴 데이터 로드 완료");
          } else {
            console.log("📝 메뉴 데이터가 없어 샘플 데이터 사용");
            setMenuData(sampleMenuData);
          }
        } catch (error) {
          console.error("❌ 메뉴 로드 실패:", error);
          setMenuError(
            "메뉴를 불러오는데 실패했습니다. 샘플 메뉴를 표시합니다."
          );
          setMenuData(sampleMenuData);
        } finally {
          setIsLoadingMenu(false);
        }
      } else {
        console.log("🏠 userId가 없어 샘플 데이터 사용");
        setMenuData(sampleMenuData);
      }
    };

    // URL에서 결제 성공/실패 및 페이지 파라미터 확인
    const urlParams = new URLSearchParams(window.location.search);
    const paymentParam = urlParams.get("payment");
    const pageParam = urlParams.get("page");

    if (paymentParam === "success") {
      console.log("토스 결제 성공 페이지로 이동됨");

      // 저장된 장바구니 복원
      const savedItems = localStorage.getItem("pendingOrderItems");
      const savedCart = localStorage.getItem("pendingOrderCart");

      if (savedItems) {
        const items = JSON.parse(savedItems);
        setOrderedItems(items);
        localStorage.setItem("lastOrderedItems", JSON.stringify(items));

        // 임시 저장 데이터 삭제
        localStorage.removeItem("pendingOrderItems");
        localStorage.removeItem("pendingOrderCart");
      }

      // 결제 성공 처리
      const orderId = urlParams.get("orderId");
      const paymentKey = urlParams.get("paymentKey");
      const amount = urlParams.get("amount");

      setShowTossWidget(false);
      setShowPayPalWidget(false);
      setCurrentPage("success");

      // URL 정리
      window.history.replaceState({}, document.title, newUrl);
    } else if (paymentParam === "fail") {
      console.log("토스 결제 실패 페이지로 이동됨");
      const message = urlParams.get("message") || "결제가 실패했습니다.";
      handlePaymentError(message);

      // URL 정리
      window.history.replaceState({}, document.title, newUrl);
    } else if (pageParam === "review") {
      console.log("🔔 푸시 알림에서 리뷰 페이지로 이동됨");
      // 저장된 주문 정보가 있으면 리뷰 페이지로, 없으면 메뉴로
      const savedOrder = localStorage.getItem("lastOrderedItems");
      if (savedOrder) {
        try {
          const items = JSON.parse(savedOrder);
          setOrderedItems(items);
          setCurrentPage("writeReview");
        } catch (e) {
          console.error("주문 정보 파싱 실패:", e);
          setCurrentPage("menu");
        }
      } else {
        setCurrentPage("menu");
      }

      // URL 정리
      window.history.replaceState({}, document.title, newUrl);
    }

    // 함수들 실행
    loadMenuFromUrl();
    checkNotificationPermission();
  }, [selectedLang]);

  const [showReviews, setShowReviews] = useState(null);
  const [currentPage, setCurrentPage] = useState("menu");
  const [showPaymentModal, setShowPaymentModal] = useState(false);
  const [showTossWidget, setShowTossWidget] = useState(false);
  const [showPayPalWidget, setShowPayPalWidget] = useState(false);
  const [paymentError, setPaymentError] = useState(null);
  const [orderedItems, setOrderedItems] = useState([]);
  const [showNotificationModal, setShowNotificationModal] = useState(false);
  const [hasAskedForNotification, setHasAskedForNotification] = useState(false);

  const {
    cart,
    addToCart,
    removeFromCart,
    getTotalItems,
    getTotalPrice,
    clearCart,
  } = useMenu();

  // API에서 받은 메뉴 데이터를 sampleMenuData 형식으로 변환하는 함수
  const transformApiMenuData = (apiMenuItems) => {
    const menuByCategory = {};

    apiMenuItems.forEach(async (item, index) => {
      const category = item.category || "메인메뉴";
      const reviews = await getReviewsByMenuId(item.menuIdx);
      // 배열이므로 map으로 변환
      const transformedReviews = reviews.map((r) => ({
        user: r.userId,
        countryCode: r.nationality,
        text: r.comment,
      }));
      if (!menuByCategory[category]) {
        menuByCategory[category] = [];
      }

      // API 데이터를 기존 형식에 맞게 변환
      const transformedItem = {
        id: item.menuIdx,
        name: item.menuName,
        price:
          typeof item.menuPrice === "number" && item.menuPrice >= 0
            ? item.menuPrice
            : 0,
        description: item.menuDescription,
        image: (item.imageUrl && item.imageUrl.trim()) || null,
        category: item.category || "메뉴",
        rating:
          typeof item.rating === "number" && item.rating > 0
            ? item.rating
            : null,
        reviewCount:
          typeof item.reviewCount === "number" && item.reviewCount >= 0
            ? item.reviewCount
            : 0,
        reviews: transformedReviews.length > 0 ? transformedReviews : [],
      };

      menuByCategory[category].push(transformedItem);
    });

    return {
      restaurant: {
        name: userIdRef
          ? `${userIdRef.current.split("@")[0]}님의 레스토랑`
          : sampleMenuData.restaurant.name,
        subtitle: tableNumberRef
          ? `테이블 ${tableNumberRef.current}`
          : sampleMenuData.restaurant.subtitle,
      },
      menu: menuByCategory,
    };
  };

  // 토스페이먼츠는 TossPaymentWidget 컴포넌트에서 직접 로드

  const getAllItems = () => {
    return Object.values(menuData.menu).flat();
  };

  const handleOrderClick = () => {
    setCurrentPage("order");
  };

  const handlePaymentClick = () => {
    setShowPaymentModal(true);
  };

  const handleTossPayment = async () => {
    // 토스 결제 전에 장바구니 저장 (페이지 이동 대비)
    const cartItems = Object.keys(cart).map((itemId) => {
      const item = getAllItems().find((i) => i.id === parseInt(itemId));
      return { ...item, quantity: cart[itemId] };
    });
    localStorage.setItem("pendingOrderItems", JSON.stringify(cartItems));
    localStorage.setItem("pendingOrderCart", JSON.stringify(cart));

    setShowPaymentModal(false);
    setShowTossWidget(true);
  };

  const handlePayPalPayment = () => {
    setShowPaymentModal(false);
    setShowPayPalWidget(true);
  };

  const handlePaymentError = (errorMessage) => {
    setPaymentError(errorMessage);
    setShowTossWidget(false);
    setShowPayPalWidget(false);
    setTimeout(() => setPaymentError(null), 5000); // 5초 후 에러 메시지 제거
  };

  const handlePaymentComplete = (paymentDetails) => {
    console.log("🎉 결제 완료 함수 호출됨:", paymentDetails);
    setShowTossWidget(false);
    setShowPayPalWidget(false);

    // 주문한 아이템 저장
    const items = Object.keys(cart).map((itemId) => {
      const item = getAllItems().find((i) => i.id === parseInt(itemId));
      return { ...item, quantity: cart[itemId] };
    });
    console.log("📦 주문 아이템들:", items);
    setOrderedItems(items);

    // 리뷰 알림을 위해 주문 정보를 localStorage에 저장
    localStorage.setItem("lastOrderedItems", JSON.stringify(items));

    console.log("📄 페이지를 success로 변경");
    setCurrentPage("success");
    clearCart();
  };

  const handleBackToMenu = () => {
    setCurrentPage("menu");
    setOrderedItems([]);
  };

  const handleGoToReview = () => {
    console.log("📱 리뷰 페이지로 이동");
    setCurrentPage("writeReview");
  };

  const handleSubmitReview = (review) => {
    console.log("리뷰 작성:", review);
    // 여기서 실제로는 서버에 리뷰를 저장해야 합니다
    setCurrentPage("success");
  };

  const handleSkipReview = () => {
    setCurrentPage("success");
  };

  // 알림 권한 허용 처리
  const handleAllowNotifications = async () => {
    try {
      const granted = await requestNotificationPermission();
      if (granted) {
        await initializePushNotifications();
        console.log("Push notifications initialized successfully");
      }
      localStorage.setItem("hasAskedForNotification", "true");
      setHasAskedForNotification(true);
      setShowNotificationModal(false);
    } catch (error) {
      console.error("Failed to enable notifications:", error);
      setShowNotificationModal(false);
    }
  };

  // 알림 권한 거부 처리
  const handleDenyNotifications = () => {
    localStorage.setItem("hasAskedForNotification", "true");
    setHasAskedForNotification(true);
    setShowNotificationModal(false);
  };

  if (currentPage === "writeReview") {
    return (
      <WriteReviewPage
        orderedItems={orderedItems}
        selectedLang={selectedLang}
        onSubmitReview={handleSubmitReview}
        onSkip={handleSkipReview}
      />
    );
  }

  if (currentPage === "success") {
    return (
      <SuccessPage
        onBackToMenu={handleBackToMenu}
        onGoToReview={handleGoToReview}
        orderedItems={orderedItems}
        selectedLang={selectedLang}
      />
    );
  }

  if (currentPage === "order") {
    return (
      <>
        <OrderPage
          cart={cart}
          getAllItems={getAllItems}
          selectedLang={selectedLang}
          onAddToCart={addToCart}
          onRemoveFromCart={removeFromCart}
          onBackToMenu={() => setCurrentPage("menu")}
          onPaymentClick={handlePaymentClick}
          getTotalPrice={() => getTotalPrice(getAllItems)}
        />
        <PaymentModal
          isOpen={showPaymentModal}
          onClose={() => setShowPaymentModal(false)}
          onTossPayment={handleTossPayment}
          onPayPalPayment={handlePayPalPayment}
        />
        <TossPaymentWidget
          isOpen={showTossWidget}
          totalPrice={getTotalPrice(getAllItems)}
          selectedLang={selectedLang}
          onPaymentComplete={handlePaymentComplete}
          onPaymentError={handlePaymentError}
        />
        <PayPalPaymentWidget
          isOpen={showPayPalWidget}
          onClose={() => setShowPayPalWidget(false)}
          totalPrice={getTotalPrice(getAllItems)}
          onPaymentComplete={handlePaymentComplete}
          onPaymentError={handlePaymentError}
        />
      </>
    );
  }

  return (
    <div className="mobile-container">
      <header className="header">
        <h1 className="restaurant-name">{menuData.restaurant.name}</h1>
        <p className="restaurant-subtitle">{menuData.restaurant.subtitle}</p>

        <LanguageSelector
          selectedLang={selectedLang}
          onLanguageChange={setSelectedLang}
        />
      </header>

      {/* 메뉴 에러 표시 */}
      {menuError && (
        <div className="menu-error-toast">
          <div className="error-message">⚠️ {menuError}</div>
          <button onClick={() => setMenuError(null)}>×</button>
        </div>
      )}

      {/* 메뉴 로딩 상태 */}
      {isLoadingMenu && (
        <div className="loading-container">
          <div className="loading-spinner">🍽️</div>
          <p>메뉴를 불러오는 중...</p>
        </div>
      )}

      <main>
        {Object.entries(menuData.menu).map(([category, items]) => (
          <MenuSection
            key={category}
            category={category}
            items={items}
            selectedLang={selectedLang}
            cart={cart}
            onAddToCart={addToCart}
            onRemoveFromCart={removeFromCart}
            onShowReviews={setShowReviews}
          />
        ))}
      </main>

      {getTotalItems() > 0 && (
        <div className="order-summary">
          <button className="order-btn" onClick={handleOrderClick}>
            {formatOrderText(getTotalItems(), selectedLang)} -{" "}
            {formatPrice(getTotalPrice(getAllItems), selectedLang)}
          </button>
        </div>
      )}

      <ReviewModal
        key={reviewViewMode}
        item={showReviews}
        selectedLang={selectedLang}
        isOpen={!!showReviews}
        onClose={() => setShowReviews(null)}
        reviewViewMode={reviewViewMode}
        setReviewViewMode={setReviewViewMode}
      />

      {/* 알림 권한 요청 모달 */}
      <NotificationPermissionModal
        isOpen={showNotificationModal}
        onAllow={handleAllowNotifications}
        onDeny={handleDenyNotifications}
        selectedLang={selectedLang}
      />

      {/* 결제 에러 알림 */}
      {paymentError && (
        <div className="payment-error-toast">
          <div className="error-message">[오류] {paymentError}</div>
          <button onClick={() => setPaymentError(null)}>×</button>
        </div>
      )}
    </div>
  );
}

export default App;
