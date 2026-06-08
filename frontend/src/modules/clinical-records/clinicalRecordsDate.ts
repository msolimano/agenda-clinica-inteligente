export function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return 'Sin fecha';
  }
  return new Intl.DateTimeFormat('es-CL', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(new Date(value));
}

export function toDateTimeLocal(value: string | null | undefined) {
  const date = value ? new Date(value) : new Date();
  const offset = date.getTimezoneOffset();
  const local = new Date(date.getTime() - offset * 60_000);
  return local.toISOString().slice(0, 16);
}

export function fromDateTimeLocal(value: string) {
  return new Date(value).toISOString();
}
