import { SimpleBarList } from './SimpleBarList';
import type { BIClinicalKpi } from './biDashboard.types';

interface ClinicalKpiPanelProps {
  clinical: BIClinicalKpi;
}

export function ClinicalKpiPanel({ clinical }: ClinicalKpiPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Actividad clinica</h2>
        <span>Agregado operacional</span>
      </div>
      <div className="bi-metric-grid bi-metric-grid--clinical">
        <Metric label="Fichas" value={clinical.clinicalRecordsTotal} />
        <Metric label="Fichas cerradas" value={clinical.clinicalRecordsClosed} />
        <Metric label="Evoluciones SOAP" value={clinical.evolutionsTotal} />
        <Metric label="Diagnosticos" value={clinical.diagnosesTotal} />
        <Metric label="Prescripciones" value={clinical.prescriptionsTotal} />
      </div>
      <div className="bi-panel__lists">
        <div>
          <h3>Top diagnosticos</h3>
          <SimpleBarList items={clinical.topDiagnoses} emptyLabel="Sin diagnosticos en el rango." />
        </div>
        <div>
          <h3>Top medicamentos</h3>
          <SimpleBarList items={clinical.topMedications} emptyLabel="Sin medicamentos en el rango." />
        </div>
      </div>
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div className="bi-metric"><span>{label}</span><strong>{value}</strong></div>;
}
