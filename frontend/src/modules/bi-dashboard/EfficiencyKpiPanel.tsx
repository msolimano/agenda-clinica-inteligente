import type { BIEfficiency } from './biDashboard.types';

interface EfficiencyKpiPanelProps {
  efficiency: BIEfficiency | null;
}

export function EfficiencyKpiPanel({ efficiency }: EfficiencyKpiPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Eficiencia operacional</h2>
        <span>Tasas agregadas</span>
      </div>
      {efficiency?.metrics.length ? (
        <div className="bi-efficiency-grid">
          {efficiency.metrics.map((metric) => (
            <article className="bi-efficiency-card" key={metric.key}>
              <span>{metric.label}</span>
              <strong>{metric.rate.toFixed(1)}%</strong>
              <small>{metric.numerator}/{metric.denominator}</small>
              <p>{metric.description}</p>
              {metric.approximate ? <em>Aproximado</em> : null}
            </article>
          ))}
        </div>
      ) : <p className="bi-chart-empty">Sin indicadores de eficiencia en el rango.</p>}
    </section>
  );
}
