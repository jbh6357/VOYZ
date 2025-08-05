import MenuItem from './MenuItem.jsx'

const MenuSection = ({ 
  category, 
  items, 
  selectedLang, 
  cart, 
  onAddToCart, 
  onRemoveFromCart, 
  onShowReviews 
}) => {
  return (
    <section className="menu-section">
      <h2 className="section-title">{category}</h2>
      
      {items.map((item) => (
        <MenuItem
          key={item.id}
          item={item}
          selectedLang={selectedLang}
          reviewViewMode="translated"
          cart={cart}
          onAddToCart={onAddToCart}
          onRemoveFromCart={onRemoveFromCart}
          onShowReviews={onShowReviews}
        />
      ))}
    </section>
  )
}

export default MenuSection