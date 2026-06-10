import { formatDateTime } from './patientPortalFormat';
import type { PatientPortalDiagnosis } from './patientPortal.types';

interface PatientDiagnosesPanelProps {
  diagnoses: PatientPortalDiagnosis[];
}

export function PatientDiagnosesPanel({ diagnoses }: PatientDiagnosesPanelProps) {
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Diagnosticos</h2><span>Agregado clinico</span></div>
      {diagnoses.length ? diagnoses.map((diagnosis) => (
        <article className="patient-list-item" key={diagnosis.id}>
          <div>
            <strong>{diagnosis.diagnosisCodeDisplay || diagnosis.diagnosisText}</strong>
            <span>{diagnosis.professionalName} · {formatDateTime(diagnosis.createdAt)}</span>
            {diagnosis.observations ? <p>{diagnosis.observations}</p> : null}
          </div>
          <span className={`patient-portal-badge patient-portal-badge--${diagnosis.diagnosisStatus}`}>
            {diagnosis.primary ? 'Principal · ' : ''}{diagnosis.displayStatus}
          </span>
        </article>
      )) : <p className="patient-empty">Sin diagnosticos registrados.</p>}
    </section>
  );
}
