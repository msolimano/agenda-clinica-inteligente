import { formatDateTime } from './patientPortalFormat';
import type { PatientPortalPrescription } from './patientPortal.types';

interface PatientPrescriptionsPanelProps {
  prescriptions: PatientPortalPrescription[];
}

export function PatientPrescriptionsPanel({ prescriptions }: PatientPrescriptionsPanelProps) {
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Prescripciones</h2><span>Vigentes e historicas</span></div>
      {prescriptions.length ? prescriptions.map((prescription) => (
        <article className="patient-list-item" key={prescription.id}>
          <div>
            <strong>{prescription.medicationName}</strong>
            <span>{prescription.dosage} · {prescription.frequency}{prescription.duration ? ` · ${prescription.duration}` : ''}</span>
            {prescription.patientInstructions ? <p>{prescription.patientInstructions}</p> : null}
            <small>{prescription.professionalName} · {formatDateTime(prescription.createdAt)}</small>
          </div>
          <span className={`patient-portal-badge patient-portal-badge--${prescription.historical ? 'historical' : prescription.prescriptionStatus}`}>
            {prescription.historical ? 'Historica' : label(prescription.prescriptionStatus)}
          </span>
        </article>
      )) : <p className="patient-empty">Sin prescripciones registradas.</p>}
    </section>
  );
}

function label(status: string) {
  const labels: Record<string, string> = { draft: 'Borrador', active: 'Activa', suspended: 'Suspendida', completed: 'Completada', cancelled: 'Cancelada' };
  return labels[status] ?? status;
}
