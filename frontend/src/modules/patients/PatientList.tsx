import { Edit3, Power } from 'lucide-react';
import { PatientStatusBadge } from './PatientStatusBadge';
import type { PatientSummary } from './patients.types';

interface PatientListProps {
  patients: PatientSummary[];
  selectedPatientId?: string;
  onSelect: (patient: PatientSummary) => void;
  onToggleStatus: (patient: PatientSummary) => void;
}

function formatDocument(patient: PatientSummary) {
  if (!patient.documentNumber) {
    return 'Identificador pendiente';
  }

  return patient.documentType ? `${patient.documentType} ${patient.documentNumber}` : patient.documentNumber;
}

function formatContact(patient: PatientSummary) {
  return patient.email || patient.phone || 'Contacto pendiente';
}

export function PatientList({ patients, selectedPatientId, onSelect, onToggleStatus }: PatientListProps) {
  return (
    <div className="patients-list" aria-label="Listado de pacientes">
      <div className="patients-list__header">
        <span>Paciente</span>
        <span>Identificador</span>
        <span>Contacto</span>
        <span>Estado</span>
        <span>Acciones</span>
      </div>

      {patients.map((patient) => {
        const fullName = `${patient.firstName} ${patient.lastName}`;
        const isSelected = patient.id === selectedPatientId;
        const isActive = patient.status === 'active';

        return (
          <article className={`patients-list__row${isSelected ? ' patients-list__row--selected' : ''}`} key={patient.id}>
            <button className="patients-list__identity" type="button" onClick={() => onSelect(patient)}>
              <span className="patients-list__name">{fullName}</span>
              <span className="patients-list__meta">{patient.birthDate || 'Fecha nacimiento pendiente'}</span>
            </button>
            <span className="patients-list__text">{formatDocument(patient)}</span>
            <span className="patients-list__text">{formatContact(patient)}</span>
            <PatientStatusBadge status={patient.status} />
            <div className="patients-list__actions">
              <button className="patients-list__icon-button" type="button" onClick={() => onSelect(patient)} aria-label={`Editar ${fullName}`}>
                <Edit3 aria-hidden="true" size={17} />
              </button>
              <button className="patients-list__icon-button" type="button" onClick={() => onToggleStatus(patient)} aria-label={`${isActive ? 'Desactivar' : 'Activar'} ${fullName}`}>
                <Power aria-hidden="true" size={17} />
              </button>
            </div>
          </article>
        );
      })}

      {patients.length === 0 ? (
        <div className="patients-list__empty">No hay pacientes para los filtros actuales.</div>
      ) : null}
    </div>
  );
}
