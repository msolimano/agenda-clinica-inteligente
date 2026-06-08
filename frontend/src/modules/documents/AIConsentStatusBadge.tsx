import type { AIConsentStatus } from './documents.types';

const labels: Record<AIConsentStatus, string> = {
  active: 'Consentimiento activo',
  revoked: 'Consentimiento revocado',
  deleted: 'Eliminado',
  not_requested: 'Sin consentimiento'
};

interface AIConsentStatusBadgeProps {
  status: AIConsentStatus;
}

export function AIConsentStatusBadge({ status }: AIConsentStatusBadgeProps) {
  return <span className={`ai-consent-status ai-consent-status--${status}`}>{labels[status]}</span>;
}
