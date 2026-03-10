export const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export async function request(path, options = {}, token) {
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
    throw new Error(payload?.detail || payload?.message || payload?.error || 'Request failed')
  }

  return payload
}

export function buildProductQuery(filters) {
  const params = new URLSearchParams()

  if (filters.search.trim()) params.set('search', filters.search.trim())
  if (filters.category.trim()) params.set('category', filters.category.trim())
  if (filters.minPrice !== '') params.set('minPrice', filters.minPrice)
  if (filters.maxPrice !== '') params.set('maxPrice', filters.maxPrice)

  const query = params.toString()
  return query ? `/api/products?${query}` : '/api/products'
}

