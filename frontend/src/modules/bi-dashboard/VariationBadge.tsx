import type { BIVariationType } from './biDashboard.types';

interface VariationBadgeProps {
  label: string;
  type: BIVariationType;
}

export function VariationBadge({ label, type }: VariationBadgeProps) {
  return <span className={`bi-variation-badge bi-variation-badge--${type}`}>{label}</span>;
}
