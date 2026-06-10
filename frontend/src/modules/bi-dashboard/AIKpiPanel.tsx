import type { BIAIKpi } from './biDashboard.types';

interface AIKpiPanelProps {
  ai: BIAIKpi;
}

export function AIKpiPanel({ ai }: AIKpiPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>IA documental</h2>
        <span>Estado operacional</span>
      </div>
      <div className="bi-metric-grid">
        <Metric label="Solicitudes" value={ai.aiAnalysesTotal} />
        <Metric label="Completadas" value={ai.aiAnalysesCompleted} />
        <Metric label="Fallidas" value={ai.aiAnalysesFailed} />
        <Metric label="Consentimientos activos" value={ai.aiConsentsActive} />
      </div>
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div className="bi-metric"><span>{label}</span><strong>{value}</strong></div>;
}
