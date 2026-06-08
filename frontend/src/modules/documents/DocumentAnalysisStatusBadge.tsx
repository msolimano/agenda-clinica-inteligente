import type { AIAnalysisDocumentStatus } from './documents.types';

const labels: Record<AIAnalysisDocumentStatus, string> = {
  not_requested: 'Sin análisis',
  pending: 'Pendiente',
  processing: 'Procesando',
  completed: 'Completado',
  failed: 'Fallido',
  reviewed: 'Revisado',
  rejected: 'Rechazado',
  deleted: 'Eliminado'
};

interface DocumentAnalysisStatusBadgeProps {
  status: AIAnalysisDocumentStatus;
}

export function DocumentAnalysisStatusBadge({ status }: DocumentAnalysisStatusBadgeProps) {
  return <span className={`document-analysis-status document-analysis-status--${status}`}>{labels[status]}</span>;
}
