import { Archive, Check, X } from 'lucide-react';
import type { ClinicalInsight } from './clinicalInsights.types';

interface ClinicalInsightReviewActionsProps {
  insight: ClinicalInsight;
  isSaving: boolean;
  onAccept: (insight: ClinicalInsight) => Promise<void>;
  onReject: (insight: ClinicalInsight) => Promise<void>;
  onDismiss: (insight: ClinicalInsight) => Promise<void>;
}

export function ClinicalInsightReviewActions({ insight, isSaving, onAccept, onReject, onDismiss }: ClinicalInsightReviewActionsProps) {
  const disabled = isSaving || insight.status !== 'pending';

  return (
    <div className="clinical-insight-actions" aria-label="Acciones de revision de insight">
      <button className="document-action document-action--insight-accept" type="button" disabled={disabled} onClick={() => void onAccept(insight)}>
        <Check aria-hidden="true" size={16} />
        Aceptar
      </button>
      <button className="document-action document-action--insight-reject" type="button" disabled={disabled} onClick={() => void onReject(insight)}>
        <X aria-hidden="true" size={16} />
        Rechazar
      </button>
      <button className="document-action document-action--insight-dismiss" type="button" disabled={disabled} onClick={() => void onDismiss(insight)}>
        <Archive aria-hidden="true" size={16} />
        Descartar
      </button>
    </div>
  );
}
