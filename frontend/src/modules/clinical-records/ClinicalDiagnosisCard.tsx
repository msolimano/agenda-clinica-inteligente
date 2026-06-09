import { Pencil } from 'lucide-react';
import { ClinicalDiagnosisStatusBadge } from './ClinicalDiagnosisStatusBadge';
import type { ClinicalDiagnosis, ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';

interface ClinicalDiagnosisCardProps {
  diagnosis: ClinicalDiagnosis;
  disabled: boolean;
  onEdit: (diagnosis: ClinicalDiagnosis) => void;
  onStatusChange: (diagnosis: ClinicalDiagnosis, status: ClinicalDiagnosisStatus) => Promise<void>;
}

export function ClinicalDiagnosisCard({ diagnosis, disabled, onEdit, onStatusChange }: ClinicalDiagnosisCardProps) {
  return (
    <article className="clinical-diagnosis-card">
      <div className="clinical-diagnosis-card__header">
        <div className="clinical-diagnosis-card__title-wrap">
          {diagnosis.primary ? <span className="clinical-diagnosis-card__primary">Principal</span> : <span className="clinical-diagnosis-card__secondary">Secundario</span>}
          <ClinicalDiagnosisStatusBadge status={diagnosis.diagnosisStatus} />
          {diagnosis.diagnosisCatalogId ? <span className="clinical-diagnosis-card__catalog">Catalogo</span> : null}
        </div>
        <button className="clinical-diagnosis-card__edit" type="button" disabled={disabled} onClick={() => onEdit(diagnosis)}>
          <Pencil aria-hidden="true" size={16} />
          Editar
        </button>
      </div>

      <p className="clinical-diagnosis-card__text">{diagnosis.diagnosisText}</p>
      {diagnosis.diagnosisCode ? <p className="clinical-diagnosis-card__code">{diagnosis.codeSystem ? `${diagnosis.codeSystem}: ` : ''}{diagnosis.diagnosisCode}</p> : null}
      {diagnosis.observations ? <p className="clinical-diagnosis-card__observations">{diagnosis.observations}</p> : null}

      <label className="clinical-diagnosis-card__status-control">
        Actualizar estado
        <select value={diagnosis.diagnosisStatus} disabled={disabled} onChange={(event) => void onStatusChange(diagnosis, event.target.value as ClinicalDiagnosisStatus)}>
          <option value="suspected">Sospechado</option>
          <option value="confirmed">Confirmado</option>
          <option value="resolved">Resuelto</option>
          <option value="ruled_out">Descartado</option>
        </select>
      </label>
    </article>
  );
}
