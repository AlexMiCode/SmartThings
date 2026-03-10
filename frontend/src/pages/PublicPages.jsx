import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { currency, emptyCatalogFilters } from '../lib/format'
import { validateCheckout, validateLogin, validateRegister } from '../lib/validation'
import { EmptyState, Field, InlineError, ProductGrid, SectionTitle, StatePage } from '../components/Shared'

export function HomePage({ featuredProducts, categories, addToCart }) {
  return (
    <main className="page-wrap">
      <section className="hero-grid">
        <div className="hero-card hero-card-main">
          <p className="eyebrow">Smart Home Commerce</p>
          <h1>Магазин для экосистемы умного дома без лишнего шума</h1>
          <p className="lead">Освещение, безопасность, климат и автоматизация в одном понятном интерфейсе.</p>
          <div className="hero-actions">
            <Link className="button-link" to="/catalog">Перейти в каталог</Link>
            <Link className="button-link secondary-link" to="/register">Создать аккаунт</Link>
          </div>
        </div>
        <div className="hero-card stat-card">
          <span>Категорий: {categories.length}</span>
          <span>Хиты: {featuredProducts.length}</span>
          <span>Сценарий покупки: каталог → корзина → заказ</span>
        </div>
      </section>

      <section className="content-section">
        <SectionTitle eyebrow="Категории" title="Что можно купить" />
        <div className="category-grid">
          {categories.map(category => (
            <Link key={category} to="/catalog" className="category-card">
              <strong>{category}</strong>
              <span>Устройства и аксессуары</span>
            </Link>
          ))}
        </div>
      </section>

      <section className="content-section">
        <SectionTitle eyebrow="Рекомендуем" title="Популярные товары" />
        <ProductGrid products={featuredProducts} onAddToCart={addToCart} />
      </section>
    </main>
  )
}

export function CatalogPage({
  products,
  categories,
  catalogFilters,
  setCatalogFilters,
  loadProducts,
  addToCart,
  isLoadingProducts
}) {
  const [draft, setDraft] = useState(catalogFilters)

  useEffect(() => {
    setDraft(catalogFilters)
  }, [catalogFilters])

  function handleSubmit(event) {
    event.preventDefault()
    setCatalogFilters(draft)
    loadProducts(draft)
  }

  function resetFilters() {
    setDraft(emptyCatalogFilters)
    setCatalogFilters(emptyCatalogFilters)
    loadProducts(emptyCatalogFilters)
  }

  return (
    <main className="page-wrap">
      <section className="content-section split-layout">
        <aside className="panel sticky-panel">
          <SectionTitle eyebrow="Фильтры" title="Каталог" />
          <form className="stack" onSubmit={handleSubmit}>
            <Field label="Поиск">
              <input value={draft.search} onChange={event => setDraft({ ...draft, search: event.target.value })} placeholder="Название или описание" />
            </Field>
            <Field label="Категория">
              <select value={draft.category} onChange={event => setDraft({ ...draft, category: event.target.value })}>
                <option value="">Все категории</option>
                {categories.map(category => <option key={category} value={category}>{category}</option>)}
              </select>
            </Field>
            <Field label="Цена от">
              <input type="number" min="0" value={draft.minPrice} onChange={event => setDraft({ ...draft, minPrice: event.target.value })} />
            </Field>
            <Field label="Цена до">
              <input type="number" min="0" value={draft.maxPrice} onChange={event => setDraft({ ...draft, maxPrice: event.target.value })} />
            </Field>
            <div className="inline-actions">
              <button type="submit">Применить</button>
              <button type="button" className="secondary" onClick={resetFilters}>Сбросить</button>
            </div>
          </form>
        </aside>

        <section className="panel">
          <div className="section-head">
            <div>
              <p className="eyebrow">Каталог</p>
              <h2>Товары</h2>
            </div>
            <strong>{isLoadingProducts ? 'Загрузка...' : `${products.length} SKU`}</strong>
          </div>
          <ProductGrid products={products} onAddToCart={addToCart} />
        </section>
      </section>
    </main>
  )
}

export function ProductPage({ products, addToCart, loadProductById }) {
  const { productId } = useParams()
  const [product, setProduct] = useState(() => products.find(item => String(item.id) === productId) || null)
  const [loading, setLoading] = useState(!product)
  const [error, setError] = useState('')

  useEffect(() => {
    const existing = products.find(item => String(item.id) === productId)
    if (existing) {
      setProduct(existing)
      setLoading(false)
      return
    }

    let mounted = true
    setLoading(true)
    loadProductById(productId)
      .then(data => {
        if (mounted) {
          setProduct(data)
          setError('')
        }
      })
      .catch(nextError => {
        if (mounted) setError(nextError.message)
      })
      .finally(() => {
        if (mounted) setLoading(false)
      })

    return () => {
      mounted = false
    }
  }, [productId, products, loadProductById])

  if (loading) return <StatePage title="Загрузка товара" text="Получаем данные карточки..." />
  if (!product || error) return <StatePage title="Товар не найден" text={error || 'Этой карточки больше нет в каталоге.'} />

  return (
    <main className="page-wrap">
      <section className="content-section panel product-detail">
        <div className="detail-image" style={{ backgroundImage: `url(${product.imageUrl})` }} />
        <div className="detail-content">
          <span className="tag">{product.category}</span>
          <h1>{product.name}</h1>
          <p className="lead">{product.description}</p>
          <div className="detail-metadata">
            <span>Бренд: {product.brand}</span>
            <span>Остаток: {product.stockQuantity}</span>
            <span>Создан: {new Date(product.createdAt).toLocaleDateString('ru-RU')}</span>
          </div>
          <div className="detail-actions">
            <strong className="price-large">{currency(product.price)}</strong>
            <button onClick={() => addToCart(product)} disabled={product.stockQuantity < 1}>Добавить в корзину</button>
          </div>
        </div>
      </section>
    </main>
  )
}

export function CartPage({ cart, cartTotal, updateCartQuantity, clearCart }) {
  return (
    <main className="page-wrap">
      <section className="content-section split-layout">
        <section className="panel">
          <SectionTitle eyebrow="Корзина" title="Выбранные товары" />
          {!cart.length && <EmptyState title="Корзина пуста" text="Добавьте товары из каталога, чтобы оформить заказ." />}
          <div className="stack">
            {cart.map(item => (
              <article key={item.id} className="list-row">
                <div>
                  <Link to={`/products/${item.id}`} className="row-link">{item.name}</Link>
                  <span>{currency(item.price)}</span>
                </div>
                <div className="qty">
                  <button type="button" onClick={() => updateCartQuantity(item.id, item.quantity - 1)}>-</button>
                  <span>{item.quantity}</span>
                  <button type="button" onClick={() => updateCartQuantity(item.id, item.quantity + 1)}>+</button>
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="panel">
          <SectionTitle eyebrow="Итог" title="Сводка заказа" />
          <div className="summary-card">
            <span>Позиции: {cart.length}</span>
            <strong>{currency(cartTotal)}</strong>
          </div>
          <div className="stack">
            <Link className={`button-link ${!cart.length ? 'disabled-link' : ''}`} to={cart.length ? '/checkout' : '/cart'}>
              Перейти к оформлению
            </Link>
            <button type="button" className="secondary" onClick={clearCart} disabled={!cart.length}>Очистить корзину</button>
          </div>
        </aside>
      </section>
    </main>
  )
}

export function CheckoutPage({ auth, cart, cartTotal, submitOrder }) {
  const navigate = useNavigate()
  const [values, setValues] = useState({
    customerName: auth?.user?.fullName || '',
    customerEmail: auth?.user?.email || '',
    deliveryAddress: '',
    notes: ''
  })
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    const validationErrors = validateCheckout(values, cart)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    setSubmitting(true)
    try {
      const order = await submitOrder(values)
      navigate(`/checkout/success/${order.id}`)
    } catch (error) {
      setErrors({ form: error.message })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="page-wrap">
      <section className="content-section split-layout">
        <section className="panel">
          <SectionTitle eyebrow="Оформление" title="Данные покупателя" />
          <form className="stack" onSubmit={handleSubmit} noValidate>
            <Field label="Имя получателя" error={errors.customerName}>
              <input value={values.customerName} onChange={event => setValues({ ...values, customerName: event.target.value })} />
            </Field>
            <Field label="Email" error={errors.customerEmail}>
              <input type="email" value={values.customerEmail} onChange={event => setValues({ ...values, customerEmail: event.target.value })} />
            </Field>
            <Field label="Адрес доставки" error={errors.deliveryAddress}>
              <textarea value={values.deliveryAddress} onChange={event => setValues({ ...values, deliveryAddress: event.target.value })} />
            </Field>
            <Field label="Комментарий">
              <textarea value={values.notes} onChange={event => setValues({ ...values, notes: event.target.value })} />
            </Field>
            {errors.items && <InlineError text={errors.items} />}
            {errors.form && <InlineError text={errors.form} />}
            <button type="submit" disabled={submitting}>{submitting ? 'Оформляем...' : 'Подтвердить заказ'}</button>
          </form>
        </section>

        <aside className="panel">
          <SectionTitle eyebrow="Заказ" title="Состав" />
          <div className="stack">
            {cart.map(item => (
              <div key={item.id} className="list-row compact">
                <div>
                  <strong>{item.name}</strong>
                  <span>{item.quantity} шт.</span>
                </div>
                <strong>{currency(item.price * item.quantity)}</strong>
              </div>
            ))}
          </div>
          <div className="summary-card">
            <span>Итого</span>
            <strong>{currency(cartTotal)}</strong>
          </div>
        </aside>
      </section>
    </main>
  )
}

export function OrderSuccessPage() {
  const { orderId } = useParams()
  return <StatePage title={`Заказ #${orderId} оформлен`} text="Заказ сохранён в системе. Статус и состав доступны в личном кабинете." actions={<Link className="button-link" to="/account">Перейти в кабинет</Link>} />
}

export function AuthPage({ mode, login, register }) {
  const navigate = useNavigate()
  const isRegister = mode === 'register'
  const [values, setValues] = useState({ fullName: '', email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    const validationErrors = isRegister ? validateRegister(values) : validateLogin(values)
    setErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    setSubmitting(true)
    try {
      if (isRegister) await register(values)
      else await login(values)
      navigate('/account')
    } catch (error) {
      setErrors({ form: error.message })
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="page-wrap auth-wrap">
      <section className="panel auth-panel">
        <SectionTitle eyebrow={isRegister ? 'Регистрация' : 'Вход'} title={isRegister ? 'Создать аккаунт' : 'Войти в кабинет'} />
        <form className="stack" onSubmit={handleSubmit} noValidate>
          {isRegister && (
            <Field label="Имя" error={errors.fullName}>
              <input value={values.fullName} onChange={event => setValues({ ...values, fullName: event.target.value })} />
            </Field>
          )}
          <Field label="Email" error={errors.email}>
            <input type="email" value={values.email} onChange={event => setValues({ ...values, email: event.target.value })} />
          </Field>
          <Field label="Пароль" error={errors.password}>
            <input type="password" value={values.password} onChange={event => setValues({ ...values, password: event.target.value })} />
          </Field>
          {errors.form && <InlineError text={errors.form} />}
          <button type="submit" disabled={submitting}>{submitting ? 'Подождите...' : isRegister ? 'Зарегистрироваться' : 'Войти'}</button>
        </form>
        <p className="muted">
          {isRegister ? 'Уже есть аккаунт?' : 'Нет аккаунта?'}{' '}
          <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Войти' : 'Зарегистрироваться'}</Link>
        </p>
      </section>
    </main>
  )
}

export function AccountPage({ auth, accountOrders, logout }) {
  return (
    <main className="page-wrap">
      <section className="content-section split-layout">
        <aside className="panel">
          <SectionTitle eyebrow="Профиль" title="Личный кабинет" />
          <div className="profile-card">
            <strong>{auth.user.fullName}</strong>
            <span>{auth.user.email}</span>
            <span>{auth.user.role}</span>
          </div>
          <button className="secondary" onClick={logout}>Выйти</button>
        </aside>

        <section className="panel">
          <SectionTitle eyebrow="История" title="Мои заказы" />
          {!accountOrders.length && <EmptyState title="Заказов пока нет" text="Оформите первый заказ через корзину." />}
          <div className="stack">
            {accountOrders.map(order => (
              <article key={order.id} className="order-card">
                <div className="inline-between">
                  <strong>Заказ #{order.id}</strong>
                  <span className="tag">{order.status}</span>
                </div>
                <span>{order.deliveryAddress}</span>
                <span>{currency(order.totalAmount)}</span>
                <Link to={`/account/orders/${order.id}`}>Открыть детали</Link>
              </article>
            ))}
          </div>
        </section>
      </section>
    </main>
  )
}

export function OrderDetailsPage({ orders, backTo, title }) {
  const { orderId } = useParams()
  const order = orders.find(item => String(item.id) === orderId)

  if (!order) return <StatePage title="Заказ не найден" text="Эта запись не найдена в текущем контексте." />

  return (
    <main className="page-wrap">
      <section className="content-section panel">
        <div className="section-head">
          <div>
            <p className="eyebrow">{title}</p>
            <h2>Заказ #{order.id}</h2>
          </div>
          <span className="tag">{order.status}</span>
        </div>
        <div className="detail-metadata">
          <span>Получатель: {order.customerName}</span>
          <span>Email: {order.customerEmail}</span>
          <span>Адрес: {order.deliveryAddress}</span>
        </div>
        <div className="stack">
          {order.items.map(item => (
            <div key={`${order.id}-${item.productId}`} className="list-row compact">
              <div>
                <strong>{item.productName}</strong>
                <span>{item.quantity} шт.</span>
              </div>
              <strong>{currency(item.price * item.quantity)}</strong>
            </div>
          ))}
        </div>
        <div className="summary-card">
          <span>Итого</span>
          <strong>{currency(order.totalAmount)}</strong>
        </div>
        <Link className="button-link secondary-link" to={backTo}>Назад</Link>
      </section>
    </main>
  )
}
