export function currency(value) {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    maximumFractionDigits: 0
  }).format(value)
}

export function readStorage(key, fallback) {
  const raw = localStorage.getItem(key)
  return raw ? JSON.parse(raw) : fallback
}

export const emptyCatalogFilters = {
  search: '',
  category: '',
  minPrice: '',
  maxPrice: ''
}

export const emptyProductForm = {
  name: '',
  description: '',
  category: 'Lighting',
  brand: '',
  price: '',
  stockQuantity: '',
  imageUrl: '',
  featured: true
}

