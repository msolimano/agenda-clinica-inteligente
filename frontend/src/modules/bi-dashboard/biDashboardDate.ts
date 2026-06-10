export function dateInputValue(date: Date) {
  return date.toISOString().slice(0, 10);
}

export function defaultFromDate() {
  const date = new Date();
  date.setDate(date.getDate() - 30);
  return dateInputValue(date);
}

export function defaultToDate() {
  return dateInputValue(new Date());
}

export function startOfDate(value: string) {
  return new Date(`${value}T00:00:00`).toISOString();
}

export function endOfDate(value: string) {
  return new Date(`${value}T23:59:59.999`).toISOString();
}

export function shortDate(value: string) {
  return new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium' }).format(new Date(value));
}
