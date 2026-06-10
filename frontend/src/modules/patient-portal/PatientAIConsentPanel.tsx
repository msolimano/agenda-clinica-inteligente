import { ShieldCheck } from 'lucide-react';
import { formatDateTime } from './patientPortalFormat';
import type { PatientPortalAIConsent } from './patientPortal.types';

interface PatientAIConsentPanelProps {
  consent: PatientPortalAIConsent;
}

export function PatientAIConsentPanel({ consent }: PatientAIConsentPanelProps) {
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Consentimiento IA</h2><ShieldCheck aria-hidden="true" size={18} /></div>
      <div className="patient-consent">
        <span className={`patient-portal-badge patient-portal-badge--${consent.active ? 'active' : 'inactive'}`}>
          {consent.active ? 'Activo' : 'No activo'}
        </span>
        <p>Version: {consent.consentVersion ?? 'Sin version registrada'}</p>
        <p>Aceptado: {formatDateTime(consent.grantedAt)}</p>
        {consent.revokedAt ? <p>Revocado: {formatDateTime(consent.revokedAt)}</p> : null}
      </div>
    </section>
  );
}
