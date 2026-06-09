import { FileText, LockKeyhole, Pencil } from 'lucide-react';
import { ClinicalDiagnosisPanel } from './ClinicalDiagnosisPanel';
import { ClinicalRecordStatusBadge } from './ClinicalRecordStatusBadge';
import { formatDateTime } from './clinicalRecordsDate';
import type { ClinicalRecord } from './clinicalRecords.types';

interface ClinicalRecordDetailProps {
  record: ClinicalRecord | null;
  onEdit: (record: ClinicalRecord) => void;
  onCloseRecord: (record: ClinicalRecord) => Promise<void>;
}

function DetailBlock({ title, value }: { title: string; value: string | null }) {
  return (
    <div className="clinical-record-detail__block">
      <h3>{title}</h3>
      <p>{value || 'Sin registro.'}</p>
    </div>
  );
}

export function ClinicalRecordDetail({ record, onEdit, onCloseRecord }: ClinicalRecordDetailProps) {
  if (!record) {
    return (
      <section className="clinical-record-detail clinical-record-detail--empty" aria-label="Detalle de ficha clínica">
        <FileText aria-hidden="true" size={24} />
        <p>Seleccione una ficha clínica para revisar el detalle.</p>
      </section>
    );
  }

  const isClosed = record.status === 'closed';

  return (
    <section className="clinical-record-detail" aria-label="Detalle de ficha clínica">
      <div className="clinical-record-detail__header">
        <div>
          <p className="clinical-record-detail__eyebrow">{formatDateTime(record.recordDate)}</p>
          <h2 className="clinical-record-detail__title">{record.chiefComplaint || 'Ficha clínica'}</h2>
          <p className="clinical-record-detail__meta">{record.patientName} · {record.professionalName}</p>
        </div>
        <ClinicalRecordStatusBadge status={record.status} />
      </div>

      <div className="clinical-record-detail__actions">
        <button className="clinical-record-action" type="button" disabled={isClosed} onClick={() => onEdit(record)}>
          <Pencil aria-hidden="true" size={17} />
          Editar
        </button>
        <button className="clinical-record-action clinical-record-action--close" type="button" disabled={isClosed} onClick={() => void onCloseRecord(record)}>
          <LockKeyhole aria-hidden="true" size={17} />
          Cerrar ficha
        </button>
      </div>

      <div className="clinical-record-detail__grid">
        <DetailBlock title="Motivo de consulta" value={record.chiefComplaint} />
        <DetailBlock title="Anamnesis" value={record.anamnesis} />
        <DetailBlock title="Examen físico" value={record.physicalExam} />
        <DetailBlock title="Evaluación" value={record.assessment} />
        <DetailBlock title="Plan" value={record.plan} />
        <DetailBlock title="Notas" value={record.notes} />
      </div>

      <ClinicalDiagnosisPanel record={record} />
    </section>
  );
}
