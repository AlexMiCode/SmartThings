import { useEffect, useMemo, useState } from 'react'

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'
const ADMIN_EMAIL = 'admin@smartthings.local'
const ADMIN_PASSWORD = 'admin123'

function currency(value) {
  return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', maximumFractionDigits: 0 }).format(value)
}

async function request(path, options = {}, token) {
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {})
    }
  })

  if (response.status === 204) {
    return null
  }

  const text = await response.text()
  const payload = text ? JSON.parse(text) : null

  if (!response.ok) {
    throw new Error(payload?.detail || payload?.message || 'Request failed')
  }

  return payload
}

const emptyProduct = {
  name: '',
  description: '',
  category: 'Lighting',
  brand: '',
  price: '',
  stockQuantity: '',
  imageUrl: '',
  featured: true
}

export default function App() {
  const [products, setProducts] = useState([])
  const [cart, setCart] = useState([])
  const [auth, setAuth] = useState(() => {
    const raw = localStorage.getItem('smartthings-auth')
    return raw ? JSON.parse(raw) : null
  })
  const [orders, setOrders] = useState([])
  const [notifications, setNotifications] = useState([])
  const [form, setForm] = useState({ email: '', password: '', fullName: '' })
  const [checkout, setCheckout] = useState({
    customerName: '',
    customerEmail: '',
    deliveryAddress: '',
    notes: ''
  })
  const [productForm, setProductForm] = useState(emptyProduct)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const isAdmin = auth?.user?.role === 'ADMIN'

  const cartTotal = useMemo(
    () => cart.reduce((sum, item) => sum + item.price * item.quantity, 0),
    [cart]
  )

  useEffect(() => {
    loadProducts()
  }, [])

  useEffect(() => {
    if (auth) {
      localStorage.setItem('smartthings-auth', JSON.stringify(auth))
      loadOrders(auth.token)
      if (isAdmin) {
        loadNotifications(auth.token)
      }
    } else {
      localStorage.removeItem('smartthings-auth')
      setOrders([])
      setNotifications([])
    }
  }, [auth, isAdmin])

  async function loadProducts() {
    try {
      const data = await request('/api/products')
      setProducts(data)
    } catch (err) {
      setError(err.message)
    }
  }

  async function loadOrders(token = auth?.token) {
    if (!token) return
    try {
      const data = await request('/api/orders', {}, token)
      setOrders(data)
    } catch (err) {
      setError(err.message)
    }
  }

  async function loadNotifications(token = auth?.token) {
    try {
      const data = await request('/api/notifications', {}, token)
      setNotifications(data)
    } catch (err) {
      setError(err.message)
    }
  }

  function addToCart(product) {
    setCart(current => {
      const existing = current.find(item => item.id === product.id)
      if (existing) {
        return current.map(item => item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item)
      }
      return [...current, { ...product, quantity: 1 }]
    })
    setMessage(`Товар "${product.name}" добавлен в корзину`)
  }

  function updateCart(productId, quantity) {
    if (quantity <= 0) {
      setCart(current => current.filter(item => item.id !== productId))
      return
    }
    setCart(current => current.map(item => item.id === productId ? { ...item, quantity } : item))
  }

  async function handleRegister(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      const data = await request('/api/auth/register', {
        method: 'POST',
        body: JSON.stringify(form)
      })
      setAuth(data)
      setCheckout(current => ({
        ...current,
        customerName: data.user.fullName,
        customerEmail: data.user.email
      }))
      setMessage('Регистрация выполнена')
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleLogin(event, credentials = form) {
    if (event) event.preventDefault()
    setError('')
    setMessage('')
    try {
      const data = await request('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: credentials.email, password: credentials.password })
      })
      setAuth(data)
      setCheckout(current => ({
        ...current,
        customerName: data.user.fullName,
        customerEmail: data.user.email
      }))
      setMessage('Вход выполнен')
    } catch (err) {
      setError(err.message)
    }
  }

  async function loginAsAdmin() {
    await handleLogin(null, { email: ADMIN_EMAIL, password: ADMIN_PASSWORD })
  }

  async function submitOrder(event) {
    event.preventDefault()
    setError('')
    setMessage('')

    if (!auth?.token) {
      setError('Для оформления заказа необходимо войти')
      return
    }
    if (!cart.length) {
      setError('Корзина пуста')
      return
    }

    try {
      await request('/api/orders', {
        method: 'POST',
        body: JSON.stringify({
          ...checkout,
          items: cart.map(item => ({ productId: item.id, quantity: item.quantity }))
        })
      }, auth.token)
      setCart([])
      setCheckout(current => ({ ...current, notes: '', deliveryAddress: '' }))
      setMessage('Заказ успешно создан')
      loadOrders(auth.token)
      if (isAdmin) loadNotifications(auth.token)
      loadProducts()
    } catch (err) {
      setError(err.message)
    }
  }

  async function createProduct(event) {
    event.preventDefault()
    setError('')
    setMessage('')
    try {
      await request('/api/products', {
        method: 'POST',
        body: JSON.stringify({
          ...productForm,
          price: Number(productForm.price),
          stockQuantity: Number(productForm.stockQuantity)
        })
      }, auth.token)
      setProductForm(emptyProduct)
      setMessage('Товар создан')
      loadProducts()
    } catch (err) {
      setError(err.message)
    }
  }

  function logout() {
    setAuth(null)
    setMessage('Сессия завершена')
  }

  return (
    <div className="page">
      <header className="hero">
        <div>
          <p className="eyebrow">Smart Home Commerce</p>
          <h1>SmartThings Store</h1>
          <p className="hero-copy">
            Шаблон витрины интернет-магазина для товаров умного дома. Можно сразу запускать,
            проверять сервисы вручную и позже спокойно переписывать UI под свои задачи.
          </p>
          <div className="hero-actions">
            <button onClick={loadProducts}>Обновить каталог</button>
            <button className="secondary" onClick={loginAsAdmin}>Войти как admin</button>
          </div>
        </div>
        <div className="status-card">
          <span>{auth ? `Пользователь: ${auth.user.fullName}` : 'Гость'}</span>
          <span>{auth ? `Роль: ${auth.user.role}` : 'Каталог доступен без входа'}</span>
          <span>{cart.length} позиций в корзине</span>
        </div>
      </header>

      {(message || error) && (
        <section className={`banner ${error ? 'error' : 'success'}`}>
          {error || message}
        </section>
      )}

      <main className="layout">
        <section className="panel catalog">
          <div className="section-head">
            <div>
              <p className="eyebrow">Каталог</p>
              <h2>Товары</h2>
            </div>
            <strong>{products.length} SKU</strong>
          </div>
          <div className="product-grid">
            {products.map(product => (
              <article key={product.id} className="product-card">
                <div className="product-image" style={{ backgroundImage: `url(${product.imageUrl})` }} />
                <div className="product-body">
                  <span className="tag">{product.category}</span>
                  <h3>{product.name}</h3>
                  <p>{product.description}</p>
                  <div className="meta">
                    <span>{product.brand}</span>
                    <span>Остаток: {product.stockQuantity}</span>
                  </div>
                  <div className="card-footer">
                    <strong>{currency(product.price)}</strong>
                    <button onClick={() => addToCart(product)} disabled={product.stockQuantity < 1}>
                      В корзину
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        </section>

        <aside className="sidebar">
          <section className="panel">
            <div className="section-head">
              <div>
                <p className="eyebrow">Авторизация</p>
                <h2>Аккаунт</h2>
              </div>
            </div>
            {!auth ? (
              <form className="stack" onSubmit={handleLogin}>
                <input placeholder="Имя" value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })} />
                <input placeholder="Email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
                <input placeholder="Пароль" type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
                <div className="inline-actions">
                  <button type="submit">Войти</button>
                  <button type="button" className="secondary" onClick={handleRegister}>Регистрация</button>
                </div>
              </form>
            ) : (
              <div className="stack">
                <div className="profile-card">
                  <strong>{auth.user.fullName}</strong>
                  <span>{auth.user.email}</span>
                  <span>{auth.user.role}</span>
                </div>
                <button className="secondary" onClick={logout}>Выйти</button>
              </div>
            )}
          </section>

          <section className="panel">
            <div className="section-head">
              <div>
                <p className="eyebrow">Корзина</p>
                <h2>Оформление</h2>
              </div>
              <strong>{currency(cartTotal)}</strong>
            </div>
            <div className="stack cart-list">
              {cart.length === 0 && <p className="muted">Пока пусто</p>}
              {cart.map(item => (
                <div key={item.id} className="cart-item">
                  <div>
                    <strong>{item.name}</strong>
                    <span>{currency(item.price)}</span>
                  </div>
                  <div className="qty">
                    <button onClick={() => updateCart(item.id, item.quantity - 1)}>-</button>
                    <span>{item.quantity}</span>
                    <button onClick={() => updateCart(item.id, item.quantity + 1)}>+</button>
                  </div>
                </div>
              ))}
            </div>
            <form className="stack" onSubmit={submitOrder}>
              <input placeholder="Имя получателя" value={checkout.customerName} onChange={e => setCheckout({ ...checkout, customerName: e.target.value })} />
              <input placeholder="Email" type="email" value={checkout.customerEmail} onChange={e => setCheckout({ ...checkout, customerEmail: e.target.value })} />
              <textarea placeholder="Адрес доставки" value={checkout.deliveryAddress} onChange={e => setCheckout({ ...checkout, deliveryAddress: e.target.value })} />
              <textarea placeholder="Комментарий" value={checkout.notes} onChange={e => setCheckout({ ...checkout, notes: e.target.value })} />
              <button type="submit">Оформить заказ</button>
            </form>
          </section>

          <section className="panel">
            <div className="section-head">
              <div>
                <p className="eyebrow">История</p>
                <h2>Заказы</h2>
              </div>
            </div>
            <div className="stack order-list">
              {!orders.length && <p className="muted">После входа здесь появятся ваши заказы</p>}
              {orders.map(order => (
                <article key={order.id} className="order-card">
                  <div className="inline-between">
                    <strong>Заказ #{order.id}</strong>
                    <span className="tag">{order.status}</span>
                  </div>
                  <span>{order.deliveryAddress}</span>
                  <span>{currency(order.totalAmount)}</span>
                </article>
              ))}
            </div>
          </section>

          {isAdmin && (
            <>
              <section className="panel">
                <div className="section-head">
                  <div>
                    <p className="eyebrow">Админка</p>
                    <h2>Новый товар</h2>
                  </div>
                </div>
                <form className="stack" onSubmit={createProduct}>
                  <input placeholder="Название" value={productForm.name} onChange={e => setProductForm({ ...productForm, name: e.target.value })} />
                  <textarea placeholder="Описание" value={productForm.description} onChange={e => setProductForm({ ...productForm, description: e.target.value })} />
                  <input placeholder="Категория" value={productForm.category} onChange={e => setProductForm({ ...productForm, category: e.target.value })} />
                  <input placeholder="Бренд" value={productForm.brand} onChange={e => setProductForm({ ...productForm, brand: e.target.value })} />
                  <input placeholder="Цена" type="number" value={productForm.price} onChange={e => setProductForm({ ...productForm, price: e.target.value })} />
                  <input placeholder="Остаток" type="number" value={productForm.stockQuantity} onChange={e => setProductForm({ ...productForm, stockQuantity: e.target.value })} />
                  <input placeholder="URL изображения" value={productForm.imageUrl} onChange={e => setProductForm({ ...productForm, imageUrl: e.target.value })} />
                  <button type="submit">Создать товар</button>
                </form>
              </section>

              <section className="panel">
                <div className="section-head">
                  <div>
                    <p className="eyebrow">Уведомления</p>
                    <h2>Лог</h2>
                  </div>
                </div>
                <div className="stack order-list">
                  {!notifications.length && <p className="muted">Пока уведомлений нет</p>}
                  {notifications.map(item => (
                    <article key={item.id} className="order-card">
                      <strong>#{item.orderId}</strong>
                      <span>{item.message}</span>
                    </article>
                  ))}
                </div>
              </section>
            </>
          )}
        </aside>
      </main>
    </div>
  )
}
