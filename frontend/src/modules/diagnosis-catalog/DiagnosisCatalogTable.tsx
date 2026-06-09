import type { DiagnosisCatalog } from './diagnosisCatalog.types';
import { DiagnosisCatalogStatusBadge } from './DiagnosisCatalogStatusBadge';

interface DiagnosisCatalogTableProps {
  diagnoses: DiagnosisCatalog[];
  selectedDiagnosisId?: string;
  onSelect: (diagnosis: DiagnosisCatalog) => void;
  onToggleStatus: (diagnosis: DiagnosisCatalog) => Promise<void>;
}

export function DiagnosisCatalogTable({ diagnoses, selectedDiagnosisId, onSelect, onToggleStatus }: DiagnosisCatalogTableProps) {
  if (!diagnoses.length) {
    return <div className="diagnosis-catalog-table__empty">No hay diagnosticos registrados.</div>;
  }

  return (
    <div className="diagnosis-catalog-table">
      <div className="diagnosis-catalog-table__header">
        <span>Diagnostico</span>
        <span>Categoria</span>
        <span>Codigo</span>
        <span>Ambito</span>
        <span>Estado</span>
        <span>Accion</span>
      </div>
      {diagnoses.map((diagnosis) => (
        <div className={`diagnosis-catalog-table__row${diagnosis.id === selectedDiagnosisId ? ' diagnosis-catalog-table__row--selected' : ''}`} key={diagnosis.id}>
          <button className="diagnosis-catalog-table__identity" type="button" onClick={() => onSelect(diagnosis)}>
            <span className="diagnosis-catalog-table__name">{diagnosis.diagnosisDisplay}</span>
            <span className="diagnosis-catalog-table__meta">{diagnosis.description || 'Sin descripcion'}</span>
          </button>
          <span>{diagnosis.category || 'Sin categoria'}</span>
          <span>{diagnosis.diagnosisCode || 'Sin codigo'}</span>
          <span>{diagnosis.organizationId ? 'Organizacion' : 'Global'}</span>
          <DiagnosisCatalogStatusBadge status={diagnosis.status} />
          <button className="diagnosis-catalog-table__status-button" type="button" onClick={() => void onToggleStatus(diagnosis)}>{diagnosis.status === 'active' ? 'Inactivar' : 'Activar'}</button>
        </div>
      ))}
    </div>
  );
}
