import { ClipboardList, HeartPulse, PhoneCall } from 'lucide-react';
import { PatientStatusBadge } from './PatientStatusBadge';
import type { Patient } from './patients.types';

interface PatientDetailPanelProps {
  patient?: Patient;
}

function valueOrPending(value?: string | null) {
  return value || 'Pendiente';
}

function formatDocument(patient: Patient) {
  if (!patient.documentNumber) {
    return 'Pendiente';
  }

  return patient.documentType ? `${patient.documentType} ${patient.documentNumber}` : patient.documentNumber;
}

function formatSex(sex?: string | null) {
  const labels: Record<string, string> = {
    female: 'Femenino',
    male: 'Masculino',
    other: 'Otro',
    unknown: 'No informado'
  };

  return sex ? labels[sex] ?? sex : 'Pendiente';
}

export function PatientDetailPanel({ patient }: PatientDetailPanelProps) {
  if (!patient) {
    return (
      <section className="patient-detail patient-detail--empty" aria-label="Ficha de paciente">
        <span className="patient-detail__empty-icon"><ClipboardList aria-hidden="true" size={22} /></span>
        <p className="patient-detail__empty-title">Seleccione un paciente</p>
        <p className="patient-detail__empty-copy">La ficha se mostrará en este panel.</p>
      </section>
    );
  }

  return (
    <section className="patient-detail" aria-label="Ficha de paciente">
      <div className="patient-detail__header">
        <span className="patient-detail__icon"><HeartPulse aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="patient-detail__title">Ficha del paciente</h2>
          <p className="patient-detail__subtitle">{patient.firstName} {patient.lastName}</p>
        </div>
        <PatientStatusBadge status={patient.status} />
      </div>

      <div className="patient-detail__grid">
        <div className="patient-detail__item">
          <span>Identificador</span>
          <strong>{formatDocument(patient)}</strong>
        </div>
        <div className="patient-detail__item">
          <span>Fecha nacimiento</span>
          <strong>{valueOrPending(patient.birthDate)}</strong>
        </div>
        <div className="patient-detail__item">
          <span>Sexo</span>
          <strong>{formatSex(patient.sex)}</strong>
        </div>
        <div className="patient-detail__item">
          <span>Correo</span>
          <strong>{valueOrPending(patient.email)}</strong>
        </div>
        <div className="patient-detail__item">
          <span>Teléfono</span>
          <strong>{valueOrPending(patient.phone)}</strong>
        </div>
        <div className="patient-detail__item patient-detail__item--wide">
          <span>Dirección</span>
          <strong>{valueOrPending(patient.address)}</strong>
        </div>
      </div>

      <div className="patient-detail__emergency">
        <div className="patient-detail__emergency-heading">
          <PhoneCall aria-hidden="true" size={18} />
          <span>Contacto de emergencia</span>
        </div>
        <dl className="patient-detail__emergency-list">
          <div>
            <dt>Nombre</dt>
            <dd>{valueOrPending(patient.emergencyContactName)}</dd>
          </div>
          <div>
            <dt>Teléfono</dt>
            <dd>{valueOrPending(patient.emergencyContactPhone)}</dd>
          </div>
          <div>
            <dt>Relación</dt>
            <dd>{valueOrPending(patient.emergencyContactRelationship)}</dd>
          </div>
        </dl>
      </div>
    </section>
  );
}
