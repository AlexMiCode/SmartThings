import { useEffect, useMemo, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { buildProductQuery, request } from './lib/api'
import { emptyCatalogFilters, readStorage } from './lib/format'
import { AppHeader, StatePage } from './components/Shared'
import {
  AccountPage,
  AuthPage,
  CartPage,
  CatalogPage,
  CheckoutPage,
  HomePage,
  OrderDetailsPage,
  OrderSuccessPage,
  ProductPage
} from './pages/PublicPages'
import {
  AdminNotificationsPage,
  AdminOrdersPage,
  AdminOverviewPage,
  AdminProductsPage
} from './pages/AdminPages'

const ADMIN_EMAIL = 'admin@smartthings.local'
const ADMIN_PASSWORD = 'admin123'

function ProtectedRoute({ auth, children }) {
  return auth ? children : <Navigate to="/login" replace />
}

function GuestRoute({ auth, children }) {
  return auth ? <Navigate to="/account" replace /> : children
}

function AdminRoute({ auth, children }) {
  if (!auth) return <Navigate to="/login" replace />
  if (auth.user.role !== 'ADMIN') return <Navigate to="/403" replace />
  return children
}

function AppShell() {
  const navigate = useNavigate()
  const [auth, setAuth] = useState(() => readStorage('smartthings-auth', null))
  const [cart, setCart] = useState(() => readStorage('smartthings-cart', []))
  const [products, setProducts] = useState([])
  const [catalogFilters, setCatalogFilters] = useState(emptyCatalogFilters)
  const [accountOrders, setAccountOrders] = useState([])
  const [notifications, setNotifications] = useState([])
  const [adminOrders, setAdminOrders] = useState([])
  const [selectedAdminUserId, setSelectedAdminUserId] = useState('')
  const [flash, setFlash] = useState(null)
  const [pageError, setPageError] = useState('')
  const [isLoadingProducts, setIsLoadingProducts] = useState(false)

  const isAdmin = auth?.user?.role === 'ADMIN'
  const categories = useMemo(() => [...new Set(products.map(product => product.category))], [products])
  const featuredProducts = useMemo(() => products.filter(product => product.featured).slice(0, 4), [products])
  const cartCount = useMemo(() => cart.reduce((sum, item) => sum + item.quantity, 0), [cart])
  const cartTotal = useMemo(() => cart.reduce((sum, item) => sum + item.quantity * item.price, 0), [cart])

  useEffect(() => {
    localStorage.setItem('smartthings-cart', JSON.stringify(cart))
  }, [cart])

  useEffect(() => {
    if (auth) localStorage.setItem('smartthings-auth', JSON.stringify(auth))
    else localStorage.removeItem('smartthings-auth')
  }, [auth])

  useEffect(() => {
    loadProducts(catalogFilters)
  }, [])

  useEffect(() => {
    if (auth?.token) {
      loadAccountOrders(auth.token)
      if (isAdmin) loadNotifications(auth.token)
    } else {
      setAccountOrders([])
      setNotifications([])
      setAdminOrders([])
    }
  }, [auth?.token, isAdmin])

  function setBanner(type, text) {
    setFlash({ type, text })
  }

  async function loadProducts(nextFilters = catalogFilters) {
    setIsLoadingProducts(true)
    setPageError('')
    try {
      setProducts(await request(buildProductQuery(nextFilters)))
    } catch (error) {
      setPageError(error.message)
    } finally {
      setIsLoadingProducts(false)
    }
  }

  function loadProductById(productId) {
    return request(`/api/products/${productId}`)
  }

  async function loadAccountOrders(token = auth?.token) {
    if (!token) return
    try {
      setAccountOrders(await request('/api/orders', {}, token))
    } catch (error) {
      setPageError(error.message)
    }
  }

  async function loadNotifications(token = auth?.token) {
    if (!token || !isAdmin) return
    try {
      setNotifications(await request('/api/notifications', {}, token))
    } catch (error) {
      setPageError(error.message)
    }
  }

  async function loadAdminOrders(userId) {
    if (!auth?.token || !userId) {
      setAdminOrders([])
      return
    }
    try {
      setAdminOrders(await request(`/api/orders?userId=${encodeURIComponent(userId)}`, {}, auth.token))
    } catch (error) {
      setPageError(error.message)
    }
  }

  function addToCart(product) {
    if (product.stockQuantity < 1) {
      setBanner('error', 'Товар отсутствует на складе')
      return
    }

    setCart(current => {
      const existing = current.find(item => item.id === product.id)
      if (existing) {
        const nextQuantity = Math.min(existing.quantity + 1, product.stockQuantity)
        return current.map(item => item.id === product.id ? { ...item, quantity: nextQuantity } : item)
      }
      return [...current, { ...product, quantity: 1 }]
    })
    setBanner('success', `Товар "${product.name}" добавлен в корзину`)
  }

  function updateCartQuantity(productId, nextQuantity) {
    if (nextQuantity <= 0) {
      setCart(current => current.filter(item => item.id !== productId))
      return
    }
    setCart(current => current.map(item => item.id === productId ? { ...item, quantity: nextQuantity } : item))
  }

  function clearCart() {
    setCart([])
  }

  async function login(credentials) {
    const data = await request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: credentials.email.trim(), password: credentials.password })
    })
    setAuth(data)
    setBanner('success', 'Вход выполнен')
    return data
  }

  async function register(payload) {
    const data = await request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        fullName: payload.fullName.trim(),
        email: payload.email.trim(),
        password: payload.password
      })
    })
    setAuth(data)
    setBanner('success', 'Регистрация выполнена')
    return data
  }

  async function loginAsAdmin() {
    try {
      await login({ email: ADMIN_EMAIL, password: ADMIN_PASSWORD })
      navigate('/admin')
    } catch (error) {
      setBanner('error', error.message)
    }
  }

  function logout() {
    setAuth(null)
    setBanner('success', 'Сессия завершена')
    navigate('/')
  }

  async function submitOrder(values) {
    const order = await request('/api/orders', {
      method: 'POST',
      body: JSON.stringify({
        customerName: values.customerName.trim(),
        customerEmail: values.customerEmail.trim(),
        deliveryAddress: values.deliveryAddress.trim(),
        notes: values.notes.trim(),
        items: cart.map(item => ({ productId: item.id, quantity: item.quantity }))
      })
    }, auth.token)
    clearCart()
    setBanner('success', `Заказ #${order.id} успешно создан`)
    await loadProducts(catalogFilters)
    await loadAccountOrders(auth.token)
    if (isAdmin) await loadNotifications(auth.token)
    return order
  }

  async function createProduct(values) {
    const created = await request('/api/products', {
      method: 'POST',
      body: JSON.stringify({
        name: values.name.trim(),
        description: values.description.trim(),
        category: values.category.trim(),
        brand: values.brand.trim(),
        price: Number(values.price),
        stockQuantity: Number(values.stockQuantity),
        imageUrl: values.imageUrl.trim() || null,
        featured: values.featured
      })
    }, auth.token)
    setBanner('success', `Товар "${created.name}" создан`)
    await loadProducts(catalogFilters)
    return created
  }

  async function updateProduct(productId, values) {
    const updated = await request(`/api/products/${productId}`, {
      method: 'PUT',
      body: JSON.stringify({
        name: values.name.trim(),
        description: values.description.trim(),
        category: values.category.trim(),
        brand: values.brand.trim(),
        price: Number(values.price),
        stockQuantity: Number(values.stockQuantity),
        imageUrl: values.imageUrl.trim() || null,
        featured: values.featured
      })
    }, auth.token)
    setBanner('success', `Товар "${updated.name}" обновлён`)
    await loadProducts(catalogFilters)
    return updated
  }

  async function deleteProduct(productId) {
    await request(`/api/products/${productId}`, { method: 'DELETE' }, auth.token)
    setBanner('success', 'Товар удалён')
    await loadProducts(catalogFilters)
  }

  async function updateOrderStatus(orderId, status) {
    await request(`/api/orders/${orderId}/status?status=${encodeURIComponent(status)}`, { method: 'PATCH' }, auth.token)
    setBanner('success', `Статус заказа #${orderId} обновлён`)
    if (selectedAdminUserId) await loadAdminOrders(selectedAdminUserId)
    await loadAccountOrders(auth.token)
  }

  const shared = {
    auth,
    cart,
    products,
    categories,
    featuredProducts,
    accountOrders,
    notifications,
    adminOrders,
    selectedAdminUserId,
    catalogFilters,
    cartTotal,
    isAdmin,
    isLoadingProducts,
    setCatalogFilters,
    setSelectedAdminUserId,
    addToCart,
    updateCartQuantity,
    clearCart,
    login,
    register,
    logout,
    submitOrder,
    createProduct,
    updateProduct,
    deleteProduct,
    updateOrderStatus,
    loadProducts,
    loadProductById,
    loadAdminOrders
  }

  return (
    <div className="app-shell">
      <AppHeader auth={auth} cartCount={cartCount} isAdmin={isAdmin} onAdminLogin={loginAsAdmin} />
      {flash && (
        <section className={`banner ${flash.type}`}>
          <span>{flash.text}</span>
          <button className="ghost-button" onClick={() => setFlash(null)}>Закрыть</button>
        </section>
      )}
      {pageError && (
        <section className="banner error">
          <span>{pageError}</span>
          <button className="ghost-button" onClick={() => setPageError('')}>Закрыть</button>
        </section>
      )}
      <Routes>
        <Route path="/" element={<HomePage {...shared} />} />
        <Route path="/catalog" element={<CatalogPage {...shared} />} />
        <Route path="/products/:productId" element={<ProductPage {...shared} />} />
        <Route path="/cart" element={<CartPage {...shared} />} />
        <Route path="/checkout" element={<ProtectedRoute auth={auth}><CheckoutPage {...shared} /></ProtectedRoute>} />
        <Route path="/checkout/success/:orderId" element={<ProtectedRoute auth={auth}><OrderSuccessPage /></ProtectedRoute>} />
        <Route path="/login" element={<GuestRoute auth={auth}><AuthPage mode="login" {...shared} /></GuestRoute>} />
        <Route path="/register" element={<GuestRoute auth={auth}><AuthPage mode="register" {...shared} /></GuestRoute>} />
        <Route path="/account" element={<ProtectedRoute auth={auth}><AccountPage {...shared} /></ProtectedRoute>} />
        <Route path="/account/orders/:orderId" element={<ProtectedRoute auth={auth}><OrderDetailsPage orders={accountOrders} backTo="/account" title="Детали заказа" /></ProtectedRoute>} />
        <Route path="/admin" element={<AdminRoute auth={auth}><AdminOverviewPage {...shared} /></AdminRoute>} />
        <Route path="/admin/products" element={<AdminRoute auth={auth}><AdminProductsPage {...shared} /></AdminRoute>} />
        <Route path="/admin/orders" element={<AdminRoute auth={auth}><AdminOrdersPage {...shared} /></AdminRoute>} />
        <Route path="/admin/notifications" element={<AdminRoute auth={auth}><AdminNotificationsPage {...shared} /></AdminRoute>} />
        <Route path="/403" element={<StatePage title="Доступ запрещён" text="Эта страница доступна только авторизованным пользователям с нужной ролью." />} />
        <Route path="*" element={<StatePage title="Страница не найдена" text="Проверьте адрес или вернитесь в каталог магазина." />} />
      </Routes>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AppShell />
    </BrowserRouter>
  )
}
