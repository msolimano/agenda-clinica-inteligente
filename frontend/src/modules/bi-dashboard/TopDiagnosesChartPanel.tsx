import { SimpleHorizontalBarChart } from './SimpleHorizontalBarChart';
import type { BIBarItem } from './biDashboard.types';

interface TopDiagnosesChartPanelProps {
  items: BIBarItem[];
}

export function TopDiagnosesChartPanel({ items }: TopDiagnosesChartPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Top diagnosticos</h2>
        <span>Agregado</span>
      </div>
      <SimpleHorizontalBarChart items={items} emptyLabel="Sin diagnosticos en el rango." />
    </section>
  );
}
