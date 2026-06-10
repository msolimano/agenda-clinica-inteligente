import { SimpleBarList } from './SimpleBarList';
import type { BIDocumentKpi } from './biDashboard.types';

interface DocumentsKpiPanelProps {
  documents: BIDocumentKpi;
}

export function DocumentsKpiPanel({ documents }: DocumentsKpiPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Documentos</h2>
        <span>Sin binarios adjuntos</span>
      </div>
      <div className="bi-metric-grid">
        <Metric label="Activos" value={documents.clinicalDocumentsTotal} />
        <Metric label="Eliminados" value={documents.deletedDocuments} />
      </div>
      <h3>Por tipo</h3>
      <SimpleBarList items={documents.documentsByType} emptyLabel="Sin documentos en el rango." />
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div className="bi-metric"><span>{label}</span><strong>{value}</strong></div>;
}
