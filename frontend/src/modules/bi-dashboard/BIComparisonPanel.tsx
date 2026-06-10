import { BIComparisonCard } from './BIComparisonCard';
import { shortDate } from './biDashboardDate';
import type { BIComparison } from './biDashboard.types';

interface BIComparisonPanelProps {
  comparison: BIComparison | null;
}

export function BIComparisonPanel({ comparison }: BIComparisonPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Comparativo operacional</h2>
        <span>{comparison ? `${shortDate(comparison.currentPeriod.from)} - ${shortDate(comparison.currentPeriod.to)}` : 'Periodo actual vs anterior'}</span>
      </div>
      {comparison?.metrics.length ? (
        <div className="bi-comparison-grid">
          {comparison.metrics.map((metric) => <BIComparisonCard key={metric.key} metric={metric} />)}
        </div>
      ) : <p className="bi-chart-empty">Sin indicadores comparativos en el rango.</p>}
      {comparison ? <p className="bi-panel__note">Periodo anterior: {shortDate(comparison.previousPeriod.from)} - {shortDate(comparison.previousPeriod.to)}.</p> : null}
    </section>
  );
}
