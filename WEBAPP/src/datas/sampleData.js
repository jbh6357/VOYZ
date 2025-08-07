export const sampleMenuData = {
  restaurant: {
    name: "VOYZ Restaurant",
    subtitle: "Delicious Food & Great Service"
  },
  menu: {
    "Starters": [
      {
        id: 101,
        name: "Spring Rolls",
        description: "Crispy vegetable spring rolls served with sweet chili sauce",
        price: 12000,
        image: "/images/spring-rolls.jpg",
        rating: 4.8,
        reviewCount: 156,
        reviews: [
          {
            user: "John D.",
            countryCode: "US",
            text: {
              en: "Absolutely delicious! The best spring rolls I've ever had.",
              ko: "정말 맛있어요! 제가 먹어본 스프링롤 중 최고예요."
            }
          },
          {
            user: "김민수",
            countryCode: "KR",
            text: {
              en: "Perfect crispy texture! Goes well with the sauce.",
              ko: "바삭함이 완벽해요! 소스와 잘 어울립니다."
            }
          },
          {
            user: "Liu Wei",
            countryCode: "CN",
            text: {
              en: "Reminds me of home cooking. Very authentic!",
              ko: "집에서 먹던 맛이 나요. 정말 정통이에요!"
            }
          }
        ]
      },
      {
        id: 102,
        name: "Bruschetta",
        description: "Toasted bread topped with fresh tomatoes, basil, and garlic",
        price: 10000,
        image: "/images/bruschetta.jpg",
        rating: 4.6,
        reviewCount: 98,
        reviews: [
          {
            user: "Maria G.",
            countryCode: "IT",
            text: {
              en: "Authentic Italian taste! Fresh and flavorful.",
              ko: "정통 이탈리아 맛! 신선하고 풍미가 좋아요."
            }
          },
          {
            user: "박지영",
            countryCode: "KR",
            text: {
              en: "Fresh ingredients make all the difference!",
              ko: "신선한 재료가 모든 차이를 만들어요!"
            }
          }
        ]
      }
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
            countryCode: "CA",
            text: {
              en: "Perfectly cooked salmon! The lemon butter sauce is amazing.",
              ko: "완벽하게 조리된 연어! 레몬 버터 소스가 정말 훌륭해요."
            }
          },
          {
            user: "이준호",
            countryCode: "KR",
            text: {
              en: "High quality salmon, cooked to perfection.",
              ko: "고품질 연어, 완벽하게 조리되었습니다."
            }
          },
          {
            user: "Yuki T.",
            countryCode: "JP",
            text: {
              en: "The best salmon I've had outside of Japan!",
              ko: "일본 밖에서 먹어본 최고의 연어요리!"
            }
          }
        ]
      },
      {
        id: 202,
        name: "Chicken Parmesan",
        description: "Breaded chicken breast with marinara sauce and melted mozzarella",
        price: 25000,
        image: "/images/chicken-parm.jpg",
        rating: 4.7,
        reviewCount: 189,
        reviews: [
          {
            user: "Mike T.",
            countryCode: "US",
            text: {
              en: "Generous portion and tastes great! Highly recommend.",
              ko: "양이 푸짐하고 맛도 훌륭해요! 강력 추천합니다."
            }
          },
          {
            user: "최은정",
            countryCode: "KR",
            text: {
              en: "The cheese is so melty and delicious!",
              ko: "치즈가 정말 쭉쭉 늘어나고 맛있어요!"
            }
          }
        ]
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
            countryCode: "AU",
            text: {
              en: "Fresh veggies and perfectly seasoned. Great vegetarian option!",
              ko: "신선한 야채와 완벽한 양념. 훌륭한 채식 옵션이에요!"
            }
          },
          {
            user: "정수아",
            countryCode: "KR",
            text: {
              en: "Healthy and delicious! My go-to choice.",
              ko: "건강하고 맛있어요! 제가 자주 선택하는 메뉴입니다."
            }
          }
        ]
      }
    ],
    "Desserts": [
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
            countryCode: "GB",
            text: {
              en: "Decadent and rich! Perfect for chocolate lovers.",
              ko: "진한 초콜릿 맛! 초콜릿 애호가에게 완벽해요."
            }
          },
          {
            user: "김서연",
            countryCode: "KR",
            text: {
              en: "Not too sweet, just perfect!",
              ko: "너무 달지 않고 딱 좋아요!"
            }
          },
          {
            user: "Pierre D.",
            countryCode: "FR",
            text: {
              en: "Reminds me of French patisserie quality!",
              ko: "프랑스 파티세리 수준이에요!"
            }
          }
        ]
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
            text: {
              en: "Just like my grandmother used to make! Authentic and delicious.",
              ko: "할머니가 만들어주신 것과 똑같아요! 정통이고 맛있어요."
            }
          },
          {
            user: "한지민",
            countryCode: "KR",
            text: {
              en: "The coffee flavor is perfect, not overwhelming.",
              ko: "커피 맛이 완벽해요, 과하지 않아요."
            }
          },
          {
            user: "Sophie L.",
            countryCode: "FR",
            text: {
              en: "Light and fluffy, melts in your mouth!",
              ko: "가볍고 폭신해서 입에서 녹아요!"
            }
          }
        ]
      }
    ],
    "Beverages": [
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
            text: {
              en: "Great coffee! Strong and aromatic.",
              ko: "훌륭한 커피! 진하고 향이 좋아요."
            }
          },
          {
            user: "이민재",
            countryCode: "KR",
            text: {
              en: "Perfect morning pick-me-up!",
              ko: "완벽한 아침 활력소!"
            }
          }
        ]
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
            countryCode: "NZ",
            text: {
              en: "So fresh and refreshing! You can taste the quality.",
              ko: "정말 신선하고 상쾌해요! 품질을 느낄 수 있어요."
            }
          },
          {
            user: "박민지",
            countryCode: "KR",
            text: {
              en: "Real oranges, no artificial taste!",
              ko: "진짜 오렌지, 인공적인 맛이 없어요!"
            }
          }
        ]
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
            text: {
              en: "Always cold and refreshing!",
              ko: "항상 차갑고 상쾌해요!"
            }
          },
          {
            user: "김태현",
            countryCode: "KR",
            text: {
              en: "Good selection of sodas.",
              ko: "탄산음료 선택이 좋아요."
            }
          }
        ]
      }
    ]
  }
};