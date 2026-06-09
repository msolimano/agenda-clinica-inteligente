import { Pencil } from 'lucide-react';
import { ClinicalPrescriptionStatusBadge } from './ClinicalPrescriptionStatusBadge';
import type { ClinicalPrescription, ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';

interface ClinicalPrescriptionCardProps {
  prescription: ClinicalPrescription;
  disabled: boolean;
  onEdit: (prescription: ClinicalPrescription) => void;
  onStatusChange: (prescription: ClinicalPrescription, status: ClinicalPrescriptionStatus) => Promise<void>;
}

function OptionalLine({ label, value }: { label: string; value: string | null }) {
  if (!value) {
    return null;
  }
  return <p><strong>{label}:</strong> {value}</p>;
}

export function ClinicalPrescriptionCard({ prescription, disabled, onEdit, onStatusChange }: ClinicalPrescriptionCardProps) {
  const catalogCode = prescription.medicationCode
    ? `${prescription.medicationCodeSystem ? `${prescription.medicationCodeSystem}: ` : ''}${prescription.medicationCode}`
    : null;

  return (
    <article className="clinical-prescription-card">
      <div className="clinical-prescription-card__header">
        <div className="clinical-prescription-card__title-wrap">
          <ClinicalPrescriptionStatusBadge status={prescription.prescriptionStatus} />
          {prescription.medicationCatalogId ? <span className="clinical-prescription-card__diagnosis">Catalogo</span> : null}
          {prescription.diagnosisText ? <span className="clinical-prescription-card__diagnosis">Con diagnostico</span> : null}
        </div>
        <button className="clinical-prescription-card__edit" type="button" disabled={disabled} onClick={() => onEdit(prescription)}>
          <Pencil aria-hidden="true" size={16} />
          Editar
        </button>
      </div>

      <div className="clinical-prescription-card__body">
        <h3>{prescription.medicationName}</h3>
        <p><strong>Dosis:</strong> {prescription.dosage}</p>
        <p><strong>Frecuencia:</strong> {prescription.frequency}</p>
        <OptionalLine label="Codigo" value={catalogCode} />
        <OptionalLine label="Duracion" value={prescription.duration} />
        <OptionalLine label="Via" value={prescription.route} />
        <OptionalLine label="Diagnostico" value={prescription.diagnosisText} />
        <OptionalLine label="Indicaciones" value={prescription.patientInstructions} />
        <OptionalLine label="Notas clinicas" value={prescription.clinicalNotes} />
      </div>

      <label className="clinical-prescription-card__status-control">
        Actualizar estado
        <select value={prescription.prescriptionStatus} disabled={disabled} onChange={(event) => void onStatusChange(prescription, event.target.value as ClinicalPrescriptionStatus)}>
          <option value="draft">Borrador</option>
          <option value="active">Activa</option>
          <option value="suspended">Suspendida</option>
          <option value="completed">Completada</option>
          <option value="cancelled">Cancelada</option>
        </select>
      </label>
    </article>
  );
}
