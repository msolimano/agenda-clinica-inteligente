import { CalendarPlus, Edit3, PhoneCall, X } from 'lucide-react';
import { formatDate } from './waitingListDate';
import { WaitingListStatusBadge } from './WaitingListStatusBadge';
import type { WaitingListEntry } from './waitingList.types';

interface WaitingListTableProps {
  entries: WaitingListEntry[];
  selectedEntryId?: string;
  onSelect: (entry: WaitingListEntry) => void;
  onMarkContacted: (entry: WaitingListEntry) => void;
  onCancel: (entry: WaitingListEntry) => void;
  onPrepareSchedule: (entry: WaitingListEntry) => void;
}

export function WaitingListTable({ entries, selectedEntryId, onSelect, onMarkContacted, onCancel, onPrepareSchedule }: WaitingListTableProps) {
  return (
    <div className="waiting-table" aria-label="Lista de espera">
      <div className="waiting-table__header">
        <span>Paciente</span>
        <span>Especialidad</span>
        <span>Preferencia</span>
        <span>Prioridad</span>
        <span>Estado</span>
        <span>Acciones</span>
      </div>
      {entries.map((entry) => {
        const isActive = entry.status === 'waiting' || entry.status === 'contacted';
        return (
          <article className={`waiting-table__row${selectedEntryId === entry.id ? ' waiting-table__row--selected' : ''}`} key={entry.id}>
            <button className="waiting-table__identity" type="button" onClick={() => onSelect(entry)}>
              <strong>{entry.patientName}</strong>
              <span>{formatDate(entry.requestedFrom)} - {formatDate(entry.requestedTo)}</span>
            </button>
            <span className="waiting-table__text">{entry.specialtyName}</span>
            <span className="waiting-table__text">{entry.professionalName ?? 'Sin profesional'}</span>
            <span className="waiting-table__priority">{entry.priority}</span>
            <WaitingListStatusBadge status={entry.status} />
            <div className="waiting-table__actions">
              <button type="button" onClick={() => onSelect(entry)} aria-label="Editar registro"><Edit3 aria-hidden="true" size={16} /></button>
              <button type="button" onClick={() => onMarkContacted(entry)} disabled={!isActive} aria-label="Marcar contactado"><PhoneCall aria-hidden="true" size={16} /></button>
              <button type="button" onClick={() => onPrepareSchedule(entry)} disabled={!isActive} aria-label="Agendar desde lista"><CalendarPlus aria-hidden="true" size={16} /></button>
              <button type="button" onClick={() => onCancel(entry)} disabled={!isActive} aria-label="Cancelar registro"><X aria-hidden="true" size={16} /></button>
            </div>
          </article>
        );
      })}
      {entries.length === 0 ? <div className="waiting-table__empty">No hay registros para los filtros actuales.</div> : null}
    </div>
  );
}
