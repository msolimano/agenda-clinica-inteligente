import type { WaitingListStatus } from './waitingList.types';

interface WaitingListStatusBadgeProps {
  status: WaitingListStatus;
}

const labels: Record<WaitingListStatus, string> = {
  waiting: 'En espera',
  contacted: 'Contactado',
  scheduled: 'Agendado',
  cancelled: 'Cancelado',
  deleted: 'Eliminado'
};

export function WaitingListStatusBadge({ status }: WaitingListStatusBadgeProps) {
  return <span className={`waiting-status waiting-status--${status}`}>{labels[status] ?? status}</span>;
}
