import { ClipboardList } from 'lucide-react';
import { ClinicalRecordStatusBadge } from './ClinicalRecordStatusBadge';
import { formatDateTime } from './clinicalRecordsDate';
import type { ClinicalRecordSummary } from './clinicalRecords.types';

interface ClinicalRecordTimelineProps {
  records: ClinicalRecordSummary[];
  selectedId: string | null;
  onSelect: (recordId: string) => void;
}

export function ClinicalRecordTimeline({ records, selectedId, onSelect }: ClinicalRecordTimelineProps) {
  return (
    <section className="clinical-records-panel" aria-label="Historial de fichas clínicas">
      <div className="clinical-records-panel__header">
        <span className="clinical-records-panel__icon"><ClipboardList aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="clinical-records-panel__title">Línea clínica</h2>
          <p className="clinical-records-panel__subtitle">Fichas registradas para el paciente seleccionado.</p>
        </div>
      </div>

      <div className="clinical-record-timeline">
        {records.length ? records.map((record) => (
          <button
            className={`clinical-record-timeline__item${record.id === selectedId ? ' clinical-record-timeline__item--selected' : ''}`}
            key={record.id}
            type="button"
            onClick={() => onSelect(record.id)}
          >
            <span className="clinical-record-timeline__date">{formatDateTime(record.recordDate)}</span>
            <span className="clinical-record-timeline__title">{record.chiefComplaint || 'Sin motivo registrado'}</span>
            <span className="clinical-record-timeline__meta">{record.professionalName}</span>
            <ClinicalRecordStatusBadge status={record.status} />
          </button>
        )) : <p className="clinical-record-timeline__empty">No hay fichas clínicas registradas.</p>}
      </div>
    </section>
  );
}
