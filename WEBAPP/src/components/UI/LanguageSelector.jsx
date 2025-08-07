import { useState } from 'react'
import { LANGUAGES } from '../../types/index.js'
import Modal from './Modal.jsx'

const LanguageSelector = ({ selectedLang, onLanguageChange }) => {
  const [showLangModal, setShowLangModal] = useState(false)

  return (
    <>
      <div className="language-selector">
        <button 
          className="current-lang-btn"
          onClick={() => setShowLangModal(true)}
        >
          {LANGUAGES[selectedLang].flag} {LANGUAGES[selectedLang].name} ▼
        </button>
      </div>

      <Modal 
        isOpen={showLangModal}
        onClose={() => setShowLangModal(false)}
        className="lang-modal"
      >
        <div className="modal-header">
          <h3>언어 선택 / Select Language</h3>
          <button className="close-btn" onClick={() => setShowLangModal(false)}>×</button>
        </div>
        <div className="lang-grid">
          {Object.entries(LANGUAGES).map(([code, lang]) => (
            <button
              key={code}
              className={`lang-option ${selectedLang === code ? 'active' : ''}`}
              onClick={() => {
                onLanguageChange(code)
                setShowLangModal(false)
              }}
            >
              <span className="lang-flag">{lang.flag}</span>
              <span className="lang-name">{lang.name}</span>
            </button>
          ))}
        </div>
      </Modal>
    </>
  )
}

export default LanguageSelector