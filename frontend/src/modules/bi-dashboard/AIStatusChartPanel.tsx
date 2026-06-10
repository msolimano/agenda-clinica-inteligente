import { SimpleStatusDistribution } from './SimpleStatusDistribution';
import type { BIAITrend } from './biDashboard.types';

interface AIStatusChartPanelProps {
  trend: BIAITrend | null;
}

export function AIStatusChartPanel({ trend }: AIStatusChartPanelProps) {
  const items = [
    { label: 'Completados', value: trend?.completed ?? 0, color: '#00B7B3' },
    { label: 'Fallidos', value: trend?.failed ?? 0, color: '#B42318' },
    { label: 'Pendientes', value: trend?.pending ?? 0, color: '#1E4E8C' },
    { label: 'Procesando', value: trend?.processing ?? 0, color: '#7B61FF' }
  ];

  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Estado analisis IA</h2>
        <span>Documental</span>
      </div>
      <SimpleStatusDistribution items={items} emptyLabel="Sin analisis IA en el rango." />
    </section>
  );
}
