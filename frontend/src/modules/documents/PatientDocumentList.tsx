import { FolderOpen } from 'lucide-react';
import { DocumentCard } from './DocumentCard';
import type { AIConsent, ClinicalDocumentSummary } from './documents.types';

interface PatientDocumentListProps {
  documents: ClinicalDocumentSummary[];
  aiConsent: AIConsent | null;
  onDelete: (documentId: string) => Promise<void>;
  onRefresh: () => Promise<void>;
}

export function PatientDocumentList({ documents, aiConsent, onDelete, onRefresh }: PatientDocumentListProps) {
  return (
    <section className="documents-panel" aria-label="Documentos del paciente">
      <div className="documents-panel__header">
        <span className="documents-panel__icon"><FolderOpen aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="documents-panel__title">Documentos clínicos</h2>
          <p className="documents-panel__subtitle">Documentos activos asociados al paciente seleccionado.</p>
        </div>
      </div>

      <div className="documents-panel__list">
        {documents.length ? documents.map((document) => (
          <DocumentCard key={document.id} document={document} aiConsent={aiConsent} onDelete={onDelete} onRefresh={onRefresh} />
        )) : <p className="documents-panel__empty">No hay documentos clínicos activos.</p>}
      </div>
    </section>
  );
}
