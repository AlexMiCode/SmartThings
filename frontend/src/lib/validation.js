export function isEmail(value) {
  return /\S+@\S+\.\S+/.test(value)
}

export function isPositiveNumber(value) {
  return value !== '' && !Number.isNaN(Number(value)) && Number(value) >= 0
}

export function isImageUrl(value) {
  if (!value) return true

  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}

export function validateLogin(values) {
  const errors = {}
  if (!values.email.trim()) errors.email = 'Укажите email'
  else if (!isEmail(values.email)) errors.email = 'Введите корректный email'
  if (!values.password.trim()) errors.password = 'Укажите пароль'
  return errors
}

export function validateRegister(values) {
  const errors = validateLogin(values)
  if (!values.fullName.trim()) errors.fullName = 'Укажите имя'
  if (values.password && values.password.length < 6) errors.password = 'Пароль должен быть не короче 6 символов'
  return errors
}

export function validateCheckout(values, cart) {
  const errors = {}
  if (!values.customerName.trim()) errors.customerName = 'Укажите имя получателя'
  if (!values.customerEmail.trim()) errors.customerEmail = 'Укажите email'
  else if (!isEmail(values.customerEmail)) errors.customerEmail = 'Введите корректный email'
  if (!values.deliveryAddress.trim()) errors.deliveryAddress = 'Укажите адрес доставки'
  if (!cart.length) errors.items = 'Добавьте товары в корзину'
  return errors
}

export function validateProduct(values) {
  const errors = {}
  if (!values.name.trim()) errors.name = 'Укажите название'
  if (!values.description.trim()) errors.description = 'Укажите описание'
  if (!values.category.trim()) errors.category = 'Укажите категорию'
  if (!values.brand.trim()) errors.brand = 'Укажите бренд'
  if (!isPositiveNumber(values.price) || Number(values.price) <= 0) errors.price = 'Цена должна быть больше 0'
  if (!isPositiveNumber(values.stockQuantity)) errors.stockQuantity = 'Остаток должен быть 0 или больше'
  if (!isImageUrl(values.imageUrl)) errors.imageUrl = 'Укажите корректный URL изображения'
  return errors
}

export function validateStatusForm(values) {
  const errors = {}
  if (!values.status) errors.status = 'Выберите статус'
  return errors
}
