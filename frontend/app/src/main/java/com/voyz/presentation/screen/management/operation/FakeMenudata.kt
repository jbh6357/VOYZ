package com.voyz.presentation.fake

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Int,
    val category: String, // "음식" or "주류"
    val imageUrl: String? = null,
    var isEditing: Boolean = false
)
val sampleMenuItems = listOf(
    MenuItem(
        id = 1,
        name = "김치찌개",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 2,
        name = "제육볶음",
        price = 9000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 3,
        name = "소주",
        price = 5000,
        category = "주류",
        imageUrl = null
    ),
    MenuItem(
        id = 4,
        name = "맥주",
        price = 6000,
        category = "주류",
        imageUrl = null
    ),
    MenuItem(
        id = 5,
        name = "김치찌개1",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 6,
        name = "김치찌개2",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 7,
        name = "김치찌개3",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 8,
        name = "김치찌개4",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 9,
        name = "김치찌개5",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),
    MenuItem(
        id = 10,
        name = "김치찌개6",
        price = 8000,
        category = "음식",
        imageUrl = null
    ),

)