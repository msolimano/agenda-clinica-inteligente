import { AlertTriangle } from 'lucide-react';
import { formatDateTime } from './documentsDate';
import { ClinicalInsightReviewActions } from './ClinicalInsightReviewActions';
import { ClinicalInsightStatusBadge } from './ClinicalInsightStatusBadge';
import { ClinicalInsightTypeBadge } from './ClinicalInsightTypeBadge';
import type { ClinicalInsight } from './clinicalInsights.types';

interface ClinicalInsightCardProps {
  insight: ClinicalInsight;
  isSaving: boolean;
  onAccept: (insight: ClinicalInsight) => Promise<void>;
  onReject: (insight: ClinicalInsight) => Promise<void>;
  onDismiss: (insight: ClinicalInsight) => Promise<void>;
}

function confidenceLabel(confidence: number | null) {
  return confidence == null ? 'Confianza no informada' : `Confianza ${Math.round(confidence * 100)}%`;
}

export function ClinicalInsightCard({ insight, isSaving, onAccept, onReject, onDismiss }: ClinicalInsightCardProps) {
  return (
    <article className="clinical-insight-card">
      <div className="clinical-insight-card__header">
        <div className="clinical-insight-card__badges">
          <ClinicalInsightTypeBadge type={insight.insightType} />
          <ClinicalInsightStatusBadge status={insight.status} />
        </div>
        <span className="clinical-insight-card__confidence">{confidenceLabel(insight.confidence)}</span>
      </div>

      <h4 className="clinical-insight-card__title">{insight.title}</h4>
      <p className="clinical-insight-card__description">{insight.description}</p>

      {insight.sourceText ? <p className="clinical-insight-card__source">Origen: {insight.sourceText}</p> : null}

      <div className="clinical-insight-card__meta">
        <span>Documento: {insight.sourceDocumentName}</span>
        <span>Creado: {formatDateTime(insight.createdAt)}</span>
        {insight.reviewedAt ? <span>Revisado: {formatDateTime(insight.reviewedAt)}</span> : null}
      </div>

      <div className="clinical-insight-card__disclaimer">
        <AlertTriangle aria-hidden="true" size={15} />
        {insight.disclaimer}
      </div>

      {insight.reviewNotes ? <p className="clinical-insight-card__notes">Nota: {insight.reviewNotes}</p> : null}

      <ClinicalInsightReviewActions insight={insight} isSaving={isSaving} onAccept={onAccept} onReject={onReject} onDismiss={onDismiss} />
    </article>
  );
}
