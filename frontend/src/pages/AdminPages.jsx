import { useState } from 'react'
import { currency, emptyProductForm } from '../lib/format'
import { validateProduct, validateStatusForm } from '../lib/validation'
import { AdminTabs, EmptyState, Field, InlineError, SectionTitle, StatPanel } from '../components/Shared'

function ProductForm({ values, errors, onChange, onSubmit, submitLabel }) {
  return (
    <form className="stack" onSubmit={onSubmit} noValidate>
      <Field label="Название" error={errors.name}>
        <input value={values.name} onChange={event => onChange({ ...values, name: event.target.value })} />
      </Field>
      <Field label="Описание" error={errors.description}>
        <textarea value={values.description} onChange={event => onChange({ ...values, description: event.target.value })} />
      </Field>
      <Field label="Категория" error={errors.category}>
        <input value={values.category} onChange={event => onChange({ ...values, category: event.target.value })} />
      </Field>
      <Field label="Бренд" error={errors.brand}>
        <input value={values.brand} onChange={event => onChange({ ...values, brand: event.target.value })} />
      </Field>
      <Field label="Цена" error={errors.price}>
        <input type="number" min="1" value={values.price} onChange={event => onChange({ ...values, price: event.target.value })} />
      </Field>
      <Field label="Остаток" error={errors.stockQuantity}>
        <input type="number" min="0" value={values.stockQuantity} onChange={event => onChange({ ...values, stockQuantity: event.target.value })} />
      </Field>
      <Field label="URL изображения" error={errors.imageUrl}>
        <input value={values.imageUrl} onChange={event => onChange({ ...values, imageUrl: event.target.value })} />
      </Field>
      <label className="checkbox-row">
        <input type="checkbox" checked={values.featured} onChange={event => onChange({ ...values, featured: event.target.checked })} />
        <span>Показывать как featured</span>
      </label>
      {errors.form && <InlineError text={errors.form} />}
      <button type="submit">{submitLabel}</button>
    </form>
  )
}

export function AdminOverviewPage({ products, notifications, accountOrders }) {
  return (
    <main className="page-wrap">
      <AdminTabs />
      <section className="dashboard-grid">
        <StatPanel title="Товары" value={products.length} text="Каталог доступных SKU" />
        <StatPanel title="Мои заказы" value={accountOrders.length} text="Заказы текущего admin-пользователя" />
        <StatPanel title="Уведомления" value={notifications.length} text="Лог создания заказов" />
      </section>
      <section className="content-section split-layout">
        <section className="panel">
          <SectionTitle eyebrow="Каталог" title="Последние товары" />
          <div className="stack">
            {products.slice(0, 5).map(product => (
              <div key={product.id} className="list-row compact">
                <div>
                  <strong>{product.name}</strong>
                  <span>{product.brand}</span>
                </div>
                <strong>{currency(product.price)}</strong>
              </div>
            ))}
          </div>
        </section>
        <section className="panel">
          <SectionTitle eyebrow="Операции" title="Последние уведомления" />
          <div className="stack">
            {notifications.slice(0, 5).map(item => (
              <div key={item.id} className="order-card">
                <strong>Заказ #{item.orderId}</strong>
                <span>{item.message}</span>
              </div>
            ))}
            {!notifications.length && <EmptyState title="Пока пусто" text="Уведомления появятся после создания заказов." compact />}
          </div>
        </section>
      </section>
    </main>
  )
}

export function AdminProductsPage({ products, createProduct, updateProduct, deleteProduct }) {
  const [createValues, setCreateValues] = useState(emptyProductForm)
  const [createErrors, setCreateErrors] = useState({})
  const [editingId, setEditingId] = useState(null)
  const [editValues, setEditValues] = useState(emptyProductForm)
  const [editErrors, setEditErrors] = useState({})
  const [busy, setBusy] = useState(false)

  async function handleCreate(event) {
    event.preventDefault()
    const validationErrors = validateProduct(createValues)
    setCreateErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    setBusy(true)
    try {
      await createProduct(createValues)
      setCreateValues(emptyProductForm)
      setCreateErrors({})
    } catch (error) {
      setCreateErrors({ form: error.message })
    } finally {
      setBusy(false)
    }
  }

  function startEdit(product) {
    setEditingId(product.id)
    setEditValues({
      name: product.name,
      description: product.description,
      category: product.category,
      brand: product.brand,
      price: String(product.price),
      stockQuantity: String(product.stockQuantity),
      imageUrl: product.imageUrl || '',
      featured: product.featured
    })
    setEditErrors({})
  }

  async function handleUpdate(event) {
    event.preventDefault()
    const validationErrors = validateProduct(editValues)
    setEditErrors(validationErrors)
    if (Object.keys(validationErrors).length > 0) return

    setBusy(true)
    try {
      await updateProduct(editingId, editValues)
      setEditingId(null)
    } catch (error) {
      setEditErrors({ form: error.message })
    } finally {
      setBusy(false)
    }
  }

  async function handleDelete(productId) {
    await deleteProduct(productId)
    if (editingId === productId) setEditingId(null)
  }

  return (
    <main className="page-wrap">
      <AdminTabs />
      <section className="content-section split-layout">
        <section className="panel">
          <SectionTitle eyebrow="Товары" title="Создать товар" />
          <ProductForm values={createValues} errors={createErrors} onChange={setCreateValues} onSubmit={handleCreate} submitLabel={busy ? 'Сохранение...' : 'Создать товар'} />
        </section>
        <section className="panel">
          <SectionTitle eyebrow="Список" title="Управление каталогом" />
          <div className="stack">
            {products.map(product => (
              <article key={product.id} className="order-card">
                <div className="inline-between">
                  <strong>{product.name}</strong>
                  <span className="tag">{product.category}</span>
                </div>
                <span>{product.brand}</span>
                <span>{currency(product.price)} · Остаток: {product.stockQuantity}</span>
                <div className="inline-actions">
                  <button type="button" onClick={() => startEdit(product)}>Редактировать</button>
                  <button type="button" className="secondary" onClick={() => handleDelete(product.id)}>Удалить</button>
                </div>
                {editingId === product.id && (
                  <div className="embedded-form">
                    <ProductForm values={editValues} errors={editErrors} onChange={setEditValues} onSubmit={handleUpdate} submitLabel={busy ? 'Сохранение...' : 'Сохранить изменения'} />
                    <button type="button" className="ghost-button" onClick={() => setEditingId(null)}>Отменить</button>
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>
      </section>
    </main>
  )
}

export function AdminOrdersPage({
  selectedAdminUserId,
  setSelectedAdminUserId,
  loadAdminOrders,
  adminOrders,
  updateOrderStatus
}) {
  const [lookupError, setLookupError] = useState('')
  const [statusForms, setStatusForms] = useState({})

  async function handleLookup(event) {
    event.preventDefault()
    if (!selectedAdminUserId.trim() || Number.isNaN(Number(selectedAdminUserId))) {
      setLookupError('Укажите корректный numeric userId')
      return
    }
    setLookupError('')
    await loadAdminOrders(selectedAdminUserId)
  }

  async function submitStatus(orderId) {
    const formState = statusForms[orderId] || { status: '', errors: {} }
    const validationErrors = validateStatusForm(formState)
    if (Object.keys(validationErrors).length > 0) {
      setStatusForms(current => ({ ...current, [orderId]: { ...formState, errors: validationErrors } }))
      return
    }

    try {
      await updateOrderStatus(orderId, formState.status)
      setStatusForms(current => ({ ...current, [orderId]: { status: '', errors: {} } }))
    } catch (error) {
      setStatusForms(current => ({ ...current, [orderId]: { ...formState, errors: { form: error.message } } }))
    }
  }

  return (
    <main className="page-wrap">
      <AdminTabs />
      <section className="content-section panel">
        <SectionTitle eyebrow="Заказы" title="Поиск и смена статусов" />
        <form className="inline-form" onSubmit={handleLookup}>
          <input placeholder="Введите userId" value={selectedAdminUserId} onChange={event => setSelectedAdminUserId(event.target.value)} />
          <button type="submit">Показать заказы</button>
        </form>
        {lookupError && <InlineError text={lookupError} />}
        {!adminOrders.length && <EmptyState title="Нет заказов" text="Выберите userId, чтобы загрузить связанные заказы." compact />}
        <div className="stack">
          {adminOrders.map(order => {
            const formState = statusForms[order.id] || { status: '', errors: {} }
            return (
              <article key={order.id} className="order-card">
                <div className="inline-between">
                  <strong>Заказ #{order.id}</strong>
                  <span className="tag">{order.status}</span>
                </div>
                <span>User ID: {order.userId}</span>
                <span>{order.customerName}</span>
                <span>{currency(order.totalAmount)}</span>
                <div className="stack">
                  <select value={formState.status} onChange={event => setStatusForms(current => ({ ...current, [order.id]: { ...formState, status: event.target.value, errors: {} } }))}>
                    <option value="">Выберите новый статус</option>
                    <option value="NEW">NEW</option>
                    <option value="PAID">PAID</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                  {formState.errors?.status && <InlineError text={formState.errors.status} />}
                  {formState.errors?.form && <InlineError text={formState.errors.form} />}
                  <button type="button" onClick={() => submitStatus(order.id)}>Обновить статус</button>
                </div>
              </article>
            )
          })}
        </div>
      </section>
    </main>
  )
}

export function AdminNotificationsPage({ notifications }) {
  return (
    <main className="page-wrap">
      <AdminTabs />
      <section className="content-section panel">
        <SectionTitle eyebrow="Уведомления" title="Журнал событий" />
        {!notifications.length && <EmptyState title="Пока нет уведомлений" text="Они появятся после создания новых заказов." compact />}
        <div className="stack">
          {notifications.map(item => (
            <article key={item.id} className="order-card">
              <div className="inline-between">
                <strong>Уведомление #{item.id}</strong>
                <span className="tag">Order #{item.orderId}</span>
              </div>
              <span>{item.message}</span>
              <span>{new Date(item.createdAt).toLocaleString('ru-RU')}</span>
            </article>
          ))}
        </div>
      </section>
    </main>
  )
}
