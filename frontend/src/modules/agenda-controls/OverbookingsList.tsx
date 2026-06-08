import { ClipboardList } from 'lucide-react';
import { formatDateTime } from './agendaControlsDate';
import type { Overbooking } from './agendaControls.types';

interface OverbookingsListProps {
  overbookings: Overbooking[];
}

export function OverbookingsList({ overbookings }: OverbookingsListProps) {
  return (
    <section className="agenda-control-card overbookings-list" aria-label="Sobrecupos registrados">
      <div className="agenda-control-card__header">
        <span className="agenda-control-card__icon"><ClipboardList aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="agenda-control-card__title">Sobrecupos</h2>
          <p className="agenda-control-card__subtitle">Reservas autorizadas sobre capacidad base.</p>
        </div>
      </div>

      <div className="overbookings-list__items">
        {overbookings.length ? overbookings.map((overbooking) => (
          <article className="overbooking-item" key={overbooking.id}>
            <div>
              <h3 className="overbooking-item__title">{overbooking.patientName}</h3>
              <p className="overbooking-item__meta">{formatDateTime(overbooking.startAt)} - {formatDateTime(overbooking.endAt)}</p>
              <p className="overbooking-item__reason">{overbooking.reason}</p>
            </div>
            <span className="overbooking-item__status">{overbooking.status}</span>
          </article>
        )) : <p className="agenda-control-empty">No hay sobrecupos en el rango.</p>}
      </div>
    </section>
  );
}
