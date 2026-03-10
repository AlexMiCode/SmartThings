import { Link, NavLink } from 'react-router-dom'
import { currency } from '../lib/format'

export function AppHeader({ auth, cartCount, isAdmin, onAdminLogin }) {
  return (
    <header className="topbar">
      <div className="brand-block">
        <Link to="/" className="brand-link">SmartThings Store</Link>
        <span className="brand-copy">Устройства и сценарии для умного дома</span>
      </div>

      <nav className="nav-links">
        <NavLink to="/" end>Главная</NavLink>
        <NavLink to="/catalog">Каталог</NavLink>
        <NavLink to="/cart">Корзина ({cartCount})</NavLink>
        {auth ? <NavLink to="/account">Кабинет</NavLink> : <NavLink to="/login">Войти</NavLink>}
        {isAdmin && <NavLink to="/admin">Админка</NavLink>}
      </nav>

      <div className="topbar-actions">
        {auth ? (
          <div className="topbar-user">
            <strong>{auth.user.fullName}</strong>
            <span>{auth.user.role}</span>
          </div>
        ) : (
          <button className="secondary" onClick={onAdminLogin}>Войти как admin</button>
        )}
      </div>
    </header>
  )
}

export function SectionTitle({ eyebrow, title }) {
  return (
    <div className="section-head">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
      </div>
    </div>
  )
}

export function Field({ label, error, children }) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
      {error && <InlineError text={error} />}
    </label>
  )
}

export function InlineError({ text }) {
  return <span className="inline-error">{text}</span>
}

export function EmptyState({ title, text, compact = false }) {
  return (
    <div className={`empty-state ${compact ? 'compact' : ''}`}>
      <strong>{title}</strong>
      <span>{text}</span>
    </div>
  )
}

export function StatePage({ title, text, actions }) {
  return (
    <main className="page-wrap">
      <section className="panel state-panel">
        <h1>{title}</h1>
        <p className="lead">{text}</p>
        {actions || <Link className="button-link" to="/catalog">Перейти в каталог</Link>}
      </section>
    </main>
  )
}

export function ProductGrid({ products, onAddToCart }) {
  if (!products.length) {
    return <EmptyState title="Ничего не найдено" text="Попробуйте изменить фильтры или зайдите позже." compact />
  }

  return (
    <div className="product-grid">
      {products.map(product => (
        <article key={product.id} className="product-card">
          <Link to={`/products/${product.id}`} className="product-image-link">
            <div className="product-image" style={{ backgroundImage: `url(${product.imageUrl})` }} />
          </Link>
          <div className="product-body">
            <span className="tag">{product.category}</span>
            <Link to={`/products/${product.id}`} className="product-title">{product.name}</Link>
            <p>{product.description}</p>
            <div className="meta">
              <span>{product.brand}</span>
              <span>Остаток: {product.stockQuantity}</span>
            </div>
            <div className="card-footer">
              <strong>{currency(product.price)}</strong>
              <button onClick={() => onAddToCart(product)} disabled={product.stockQuantity < 1}>В корзину</button>
            </div>
          </div>
        </article>
      ))}
    </div>
  )
}

export function AdminTabs() {
  return (
    <nav className="admin-tabs">
      <NavLink to="/admin" end>Обзор</NavLink>
      <NavLink to="/admin/products">Товары</NavLink>
      <NavLink to="/admin/orders">Заказы</NavLink>
      <NavLink to="/admin/notifications">Уведомления</NavLink>
    </nav>
  )
}

export function StatPanel({ title, value, text }) {
  return (
    <article className="panel stat-panel">
      <span className="eyebrow">{title}</span>
      <strong className="stat-value">{value}</strong>
      <span>{text}</span>
    </article>
  )
}
