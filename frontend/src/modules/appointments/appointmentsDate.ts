export function toDateInputValue(date: Date) {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export function dateFromInput(value: string) {
  const [year, month, day] = value.split('-').map(Number);
  return new Date(year, month - 1, day);
}

export function startOfDay(value: Date) {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate(), 0, 0, 0, 0);
}

export function endOfDay(value: Date) {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate() + 1, 0, 0, 0, 0);
}

export function startOfWeek(value: Date) {
  const date = startOfDay(value);
  const day = date.getDay() || 7;
  date.setDate(date.getDate() - day + 1);
  return date;
}

export function addDays(value: Date, days: number) {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate() + days, 0, 0, 0, 0);
}

export function sameLocalDate(isoValue: string, date: Date) {
  const current = new Date(isoValue);
  return current.getFullYear() === date.getFullYear()
    && current.getMonth() === date.getMonth()
    && current.getDate() === date.getDate();
}

export function formatTime(isoValue: string) {
  return new Intl.DateTimeFormat('es-CL', { hour: '2-digit', minute: '2-digit' }).format(new Date(isoValue));
}

export function formatShortDate(date: Date) {
  return new Intl.DateTimeFormat('es-CL', { weekday: 'short', day: '2-digit', month: '2-digit' }).format(date);
}
