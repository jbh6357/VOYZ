import { useState } from 'react'

export const useMenu = () => {
  const [cart, setCart] = useState({})

  const addToCart = (item) => {
    setCart(prev => ({
      ...prev,
      [item.id]: (prev[item.id] || 0) + 1
    }))
  }

  const removeFromCart = (itemId) => {
    setCart(prev => {
      const newCart = { ...prev }
      if (newCart[itemId] > 1) {
        newCart[itemId]--
      } else {
        delete newCart[itemId]
      }
      return newCart
    })
  }

  const getTotalItems = () => {
    return Object.values(cart).reduce((total, count) => total + count, 0)
  }

  const getTotalPrice = (getAllItems) => {
    return Object.entries(cart).reduce((total, [itemId, count]) => {
      const item = getAllItems().find(item => item.id === parseInt(itemId))
      return total + (item ? item.price * count : 0)
    }, 0)
  }

  const clearCart = () => {
    setCart({})
  }

  return {
    cart,
    addToCart,
    removeFromCart,
    getTotalItems,
    getTotalPrice,
    clearCart
  }
}