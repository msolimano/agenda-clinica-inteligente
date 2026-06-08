import { ShieldCheck } from 'lucide-react';
import { AIConsentAction } from './AIConsentAction';
import { AIConsentStatusBadge } from './AIConsentStatusBadge';
import { formatDateTime } from './documentsDate';
import type { AIConsent } from './documents.types';

interface AIConsentPanelProps {
  consent: AIConsent | null;
  hasPatient: boolean;
  isLoading: boolean;
  error: string | null;
  onAccept: () => Promise<void>;
  onRevoke: () => Promise<void>;
}

export function AIConsentPanel({ consent, hasPatient, isLoading, error, onAccept, onRevoke }: AIConsentPanelProps) {
  const active = Boolean(consent?.active);
  const status = consent?.status ?? 'not_requested';
  const grantedAt = consent?.grantedAt ? formatDateTime(consent.grantedAt) : 'Sin fecha de aceptación';
  const revokedAt = consent?.revokedAt ? formatDateTime(consent.revokedAt) : null;

  return (
    <section className="ai-consent-panel" aria-label="Consentimiento para análisis IA">
      <div className="ai-consent-panel__header">
        <span className="ai-consent-panel__icon"><ShieldCheck aria-hidden="true" size={20} /></span>
        <div className="ai-consent-panel__heading">
          <h2 className="ai-consent-panel__title">Consentimiento IA</h2>
          <p className="ai-consent-panel__subtitle">Versión {consent?.consentVersion ?? 'IA-CONSENT-V1'}</p>
        </div>
        <AIConsentStatusBadge status={status} />
      </div>

      <div className="ai-consent-panel__body">
        {error ? <div className="ai-consent-panel__error" role="alert">{error}</div> : null}
        {!hasPatient ? <p className="ai-consent-panel__empty">Seleccione un paciente activo.</p> : null}
        {hasPatient ? (
          <>
            <p className="ai-consent-panel__meta">{active ? `Aceptado: ${grantedAt}` : revokedAt ? `Revocado: ${revokedAt}` : 'Sin aceptación registrada'}</p>
            {!active ? <div className="ai-consent-panel__warning">Paciente sin consentimiento IA activo. El análisis IA está deshabilitado.</div> : null}
            <div className="ai-consent-panel__actions">
              <AIConsentAction active={active} disabled={isLoading} onAccept={onAccept} onRevoke={onRevoke} />
            </div>
          </>
        ) : null}
      </div>
    </section>
  );
}
