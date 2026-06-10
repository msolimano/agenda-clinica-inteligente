import { formatDateTime } from './patientPortalFormat';
import type { PatientPortalClinicalHistory } from './patientPortal.types';

interface PatientClinicalHistoryPanelProps {
  clinicalHistory: PatientPortalClinicalHistory;
}

export function PatientClinicalHistoryPanel({ clinicalHistory }: PatientClinicalHistoryPanelProps) {
  return (
    <section className="patient-portal-card patient-portal-card--wide">
      <div className="patient-portal-card__header"><h2>Historia clinica</h2><span>Evoluciones visibles</span></div>
      {clinicalHistory.records.length ? clinicalHistory.records.map((record) => (
        <article className="patient-record" key={record.id}>
          <div className="patient-record__header">
            <div><strong>{formatDateTime(record.recordDate)}</strong><span>{record.professionalName}</span></div>
            <span className={`patient-portal-badge patient-portal-badge--${record.status}`}>{record.status}</span>
          </div>
          {record.chiefComplaint ? <p><b>Motivo:</b> {record.chiefComplaint}</p> : null}
          {record.assessment ? <p><b>Evaluacion:</b> {record.assessment}</p> : null}
          {record.plan ? <p><b>Plan:</b> {record.plan}</p> : null}
          <div className="patient-record__evolutions">
            {record.evolutions.length ? record.evolutions.map((evolution) => (
              <div className="patient-evolution" key={evolution.id}>
                <div><strong>{formatDateTime(evolution.evolutionDate)}</strong><span className={`patient-portal-badge patient-portal-badge--${evolution.evolutionStatus}`}>{evolution.cancelled ? 'Cancelada' : evolution.evolutionStatus}</span></div>
                {evolution.subjective ? <p><b>S:</b> {evolution.subjective}</p> : null}
                {evolution.objective ? <p><b>O:</b> {evolution.objective}</p> : null}
                {evolution.assessment ? <p><b>A:</b> {evolution.assessment}</p> : null}
                {evolution.plan ? <p><b>P:</b> {evolution.plan}</p> : null}
                {evolution.notes ? <p>{evolution.notes}</p> : null}
              </div>
            )) : <p className="patient-empty">Sin evoluciones asociadas.</p>}
          </div>
        </article>
      )) : <p className="patient-empty">Sin historia clinica registrada.</p>}
    </section>
  );
}
