import type { BIFHIRKpi } from './biDashboard.types';

interface FHIRKpiPanelProps {
  fhir: BIFHIRKpi;
}

export function FHIRKpiPanel({ fhir }: FHIRKpiPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>FHIR</h2>
        <span>Interoperabilidad</span>
      </div>
      <div className="bi-metric-grid">
        <Metric label="Bundles generados" value={fhir.fhirBundlesGenerated} />
        <Metric label="Recursos disponibles" value={fhir.fhirResourcesAvailable} />
      </div>
      <p className="bi-panel__note">La generacion de bundles queda en cero hasta contar con auditoria persistida.</p>
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div className="bi-metric"><span>{label}</span><strong>{value}</strong></div>;
}
