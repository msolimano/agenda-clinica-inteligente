import { FileText, Trash2 } from 'lucide-react';
import { formatDateTime, formatFileSize } from './documentsDate';
import { DocumentTypeBadge } from './DocumentTypeBadge';
import { DownloadAction } from './DownloadAction';
import { DocumentAnalysisPanel } from './DocumentAnalysisPanel';
import type { AIConsent, ClinicalDocumentSummary } from './documents.types';

interface DocumentCardProps {
  document: ClinicalDocumentSummary;
  aiConsent: AIConsent | null;
  onDelete: (documentId: string) => Promise<void>;
  onRefresh: () => Promise<void>;
}

export function DocumentCard({ document, aiConsent, onDelete, onRefresh }: DocumentCardProps) {
  return (
    <article className="document-card">
      <div className="document-card__icon"><FileText aria-hidden="true" size={22} /></div>
      <div className="document-card__body">
        <div className="document-card__heading">
          <h3 className="document-card__title">{document.title}</h3>
          <DocumentTypeBadge type={document.documentType} />
        </div>
        <p className="document-card__meta">{document.fileName} · {formatFileSize(document.fileSize)} · {formatDateTime(document.createdAt)}</p>
        <p className="document-card__meta">{document.professionalName ? `Profesional: ${document.professionalName}` : 'Sin profesional asociado'}</p>
      </div>
      <div className="document-card__actions">
        <DownloadAction documentId={document.id} fileName={document.fileName} />
        <button className="document-action document-action--delete" type="button" onClick={() => void onDelete(document.id)}>
          <Trash2 aria-hidden="true" size={17} />
          Eliminar
        </button>
      </div>
      <div className="document-card__analysis">
        <DocumentAnalysisPanel documentId={document.id} initialStatus={document.aiAnalysisStatus} aiConsentActive={Boolean(aiConsent?.active)} onStatusChange={onRefresh} />
      </div>
    </article>
  );
}
