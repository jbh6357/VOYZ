export const sampleMenuData = {
  restaurant: {
    name: "VOYZ Restaurant",
    subtitle: "합정 원조 떡갈비 전문점",
  },
  menu: {
    Starters: [
      {
        id: 101,
        name: "Spring Rolls",
        description:
          "Crispy vegetable spring rolls served with sweet chili sauce",
        price: 12000,
        image: "/images/spring-rolls.jpg",
        rating: 4.8,
        reviewCount: 156,
        reviews: [
          {
            user: "John D.",
            countryCode: "US",
            rating: 5.0,
            text: "Absolutely delicious! The best spring rolls I've ever had.",
          },
          {
            user: "김민수",
            countryCode: "KR",
            rating: 4.5,
            text: "바삭함이 완벽해요! 소스와 잘 어울립니다.",
          },
          {
            user: "Liu Wei",
            countryCode: "CN",
            rating: 4.8,
            text: "这让我想起了家常菜. 非常真实!",
          },
        ],
      },
      {
        id: 102,
        name: "Bruschetta",
        description:
          "Toasted bread topped with fresh tomatoes, basil, and garlic",
        price: 10000,
        image: "/images/bruschetta.jpg",
        rating: 4.6,
        reviewCount: 98,
        reviews: [
          {
            user: "Maria G.",
            countryCode: "IT",
            rating: 4.8,
            text: "Autentico gusto italiano! Fresco e saporito.",
          },
          {
            user: "박지영",
            countryCode: "KR",
            rating: 4.5,
            text: "신선한 재료가 모든 차이를 만들어요!",
          },
        ],
      },
    ],
    "Main Courses": [
      {
        id: 201,
        name: "Grilled Salmon",
        description: "Fresh Atlantic salmon with lemon butter sauce",
        price: 35000,
        image: "/images/salmon.jpg",
        rating: 4.9,
        reviewCount: 234,
        reviews: [
          {
            user: "Sarah L.",
            countryCode: "US",
            rating: 5.0,
            text: "Perfectly cooked salmon! The lemon butter sauce is amazing.",
          },
          {
            user: "이준호",
            countryCode: "KR",
            rating: 4.8,
            text: "고품질 연어, 완벽하게 조리되었습니다.",
          },
          {
            user: "Yuki T.",
            countryCode: "JP",
            rating: 5.0,
            text: "日本以外で食べたサーモンの中で最高です！",
          },
        ],
      },
      {
        id: 202,
        name: "Chicken Parmesan",
        description:
          "Breaded chicken breast with marinara sauce and melted mozzarella",
        price: 25000,
        image: "/images/chicken-parm.jpg",
        rating: 4.7,
        reviewCount: 189,
        reviews: [
          {
            user: "Mike T.",
            countryCode: "US",
            rating: 4.7,
            text: "Generous portion and tastes great! Highly recommend.",
          },
          {
            user: "최은정",
            countryCode: "KR",
            rating: 4.6,
            text: "치즈가 정말 쭉쭉 늘어나고 맛있어요!",
          },
        ],
      },
      {
        id: 203,
        name: "Vegetable Stir Fry",
        description: "Fresh mixed vegetables in a savory sauce",
        price: 18000,
        image: "/images/stir-fry.jpg",
        rating: 4.5,
        reviewCount: 145,
        reviews: [
          {
            user: "Linda K.",
            countryCode: "US",
            rating: 4.5,
            text: "Fresh veggies and perfectly seasoned. Great vegetarian option!",
          },
          {
            user: "정수아",
            countryCode: "KR",
            rating: 4.4,
            text: "건강하고 맛있어요! 제가 자주 선택하는 메뉴입니다.",
          },
        ],
      },
    ],
    Desserts: [
      {
        id: 301,
        name: "Chocolate Cake",
        description: "Rich chocolate cake with chocolate ganache",
        price: 8000,
        image: "/images/chocolate-cake.jpg",
        rating: 4.8,
        reviewCount: 267,
        reviews: [
          {
            user: "Emma R.",
            countryCode: "US",
            text: "Decadent and rich! Perfect for chocolate lovers.",
          },
          {
            user: "김서연",
            countryCode: "KR",
            text: "너무 달지 않고 딱 좋아요!",
          },
          {
            user: "Pierre D.",
            countryCode: "FR",
            text: "Ça me rappelle la qualité de la pâtisserie française!",
          },
        ],
      },
      {
        id: 302,
        name: "Tiramisu",
        description: "Classic Italian dessert with coffee and mascarpone",
        price: 9000,
        image: "/images/tiramisu.jpg",
        rating: 4.9,
        reviewCount: 312,
        reviews: [
          {
            user: "Antonio P.",
            countryCode: "IT",
            text: "Proprio come faceva mia nonna! Autentico e delizioso.",
          },
          {
            user: "한지민",
            countryCode: "KR",
            text: "커피 맛이 완벽해요, 과하지 않아요.",
          },
          {
            user: "Sophie L.",
            countryCode: "FR",
            text: "La lumière et souffrance, fond dans ta bouche !",
          },
        ],
      },
    ],
    Beverages: [
      {
        id: 401,
        name: "Coffee",
        description: "Freshly brewed coffee",
        price: 4000,
        image: "/images/coffee.jpg",
        rating: 4.4,
        reviewCount: 423,
        reviews: [
          {
            user: "David C.",
            countryCode: "US",
            text: "Great coffee! Strong and aromatic.",
          },
          {
            user: "이민재",
            countryCode: "KR",
            text: "완벽한 아침 활력소!",
          },
        ],
      },
      {
        id: 402,
        name: "Fresh Orange Juice",
        description: "Freshly squeezed orange juice",
        price: 6000,
        image: "/images/orange-juice.jpg",
        rating: 4.7,
        reviewCount: 289,
        reviews: [
          {
            user: "Rachel S.",
            countryCode: "US",
            text: "So fresh and refreshing! You can taste the quality.",
          },
          {
            user: "박민지",
            countryCode: "KR",
            text: "진짜 오렌지, 인공적인 맛이 없어요!",
          },
        ],
      },
      {
        id: 403,
        name: "Soft Drinks",
        description: "Coke, Sprite, or Fanta",
        price: 3000,
        image: "/images/soft-drinks.jpg",
        rating: 4.2,
        reviewCount: 156,
        reviews: [
          {
            user: "Kevin W.",
            countryCode: "US",
            text: "Always cold and refreshing!",
          },
          {
            user: "김태현",
            countryCode: "KR",
            text: "탄산음료 선택이 좋아요.",
          },
        ],
      },
    ],
  },
};
