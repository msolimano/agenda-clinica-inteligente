import type { MedicationCatalog } from './medications.types';
import { MedicationStatusBadge } from './MedicationStatusBadge';

interface MedicationCatalogTableProps {
  medications: MedicationCatalog[];
  selectedMedicationId?: string;
  onSelect: (medication: MedicationCatalog) => void;
  onToggleStatus: (medication: MedicationCatalog) => Promise<void>;
}

export function MedicationCatalogTable({ medications, selectedMedicationId, onSelect, onToggleStatus }: MedicationCatalogTableProps) {
  if (!medications.length) {
    return <div className="medication-table__empty">No hay medicamentos registrados.</div>;
  }

  return (
    <div className="medication-table">
      <div className="medication-table__header">
        <span>Medicamento</span>
        <span>Principio activo</span>
        <span>Codigo</span>
        <span>Ambito</span>
        <span>Estado</span>
        <span>Accion</span>
      </div>
      {medications.map((medication) => (
        <div className={`medication-table__row${medication.id === selectedMedicationId ? ' medication-table__row--selected' : ''}`} key={medication.id}>
          <button className="medication-table__identity" type="button" onClick={() => onSelect(medication)}>
            <span className="medication-table__name">{medication.medicationName}</span>
            <span className="medication-table__meta">{[medication.presentation, medication.strength, medication.pharmaceuticalForm, medication.route].filter(Boolean).join(' · ') || 'Sin detalle'}</span>
          </button>
          <span>{medication.activeIngredient || 'Sin registro'}</span>
          <span>{medication.medicationCode || 'Sin codigo'}</span>
          <span>{medication.organizationId ? 'Organizacion' : 'Global'}</span>
          <MedicationStatusBadge status={medication.status} />
          <button className="medication-table__status-button" type="button" onClick={() => void onToggleStatus(medication)}>{medication.status === 'active' ? 'Inactivar' : 'Activar'}</button>
        </div>
      ))}
    </div>
  );
}
