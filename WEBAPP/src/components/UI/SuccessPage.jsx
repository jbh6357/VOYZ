const SuccessPage = ({ onBackToMenu }) => {
  return (
    <div className="success-page">
      <div className="success-content">
        <div className="success-icon">[완료]</div>
        <h1>주문이 완료되었습니다!</h1>
        <p>맛있는 식사 되세요!</p>
        <button className="back-to-menu-btn" onClick={onBackToMenu}>
          메뉴로 돌아가기
        </button>
      </div>
    </div>
  )
}

export default SuccessPage