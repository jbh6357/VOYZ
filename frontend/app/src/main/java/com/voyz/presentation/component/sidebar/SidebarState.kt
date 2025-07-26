package com.voyz.presentation.component.sidebar

data class SidebarState(
    val isOpen: Boolean = false,
    val menuItems: List<SidebarMenuItem> = emptyList()
)

data class SidebarMenuItem(
    val id: String,
    val title: String,
    val icon: Int? = null,
    val onClick: () -> Unit = {}
)