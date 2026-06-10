import { VariationBadge } from './VariationBadge';
import type { BIComparisonMetric } from './biDashboard.types';

interface BIComparisonCardProps {
  metric: BIComparisonMetric;
}

export function BIComparisonCard({ metric }: BIComparisonCardProps) {
  const differenceLabel = metric.absoluteDifference > 0 ? `+${metric.absoluteDifference}` : `${metric.absoluteDifference}`;

  return (
    <article className="bi-comparison-card">
      <div className="bi-comparison-card__header">
        <span>{metric.label}</span>
        <VariationBadge label={metric.variationLabel} type={metric.variationType} />
      </div>
      <strong>{formatNumber(metric.currentValue)}</strong>
      <div className="bi-comparison-card__meta">
        <span>Anterior: {formatNumber(metric.previousValue)}</span>
        <span>Diferencia: {differenceLabel}</span>
      </div>
    </article>
  );
}

function formatNumber(value: number) {
  return new Intl.NumberFormat('es-CL').format(value);
}
