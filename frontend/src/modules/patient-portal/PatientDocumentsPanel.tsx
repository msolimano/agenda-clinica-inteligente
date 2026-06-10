import { Download } from 'lucide-react';
import { formatBytes, formatDateTime } from './patientPortalFormat';
import type { PatientPortalDocument } from './patientPortal.types';

interface PatientDocumentsPanelProps {
  documents: PatientPortalDocument[];
}

export function PatientDocumentsPanel({ documents }: PatientDocumentsPanelProps) {
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Documentos clinicos</h2><span>Activos</span></div>
      {documents.length ? documents.map((document) => (
        <article className="patient-list-item patient-list-item--action" key={document.id}>
          <div>
            <strong>{document.title || document.fileName}</strong>
            <span>{document.documentType} · {formatBytes(document.fileSize)} · {formatDateTime(document.createdAt)}</span>
          </div>
          <a className="patient-portal-action" href={document.downloadUrl} aria-label={`Descargar ${document.fileName}`}>
            <Download aria-hidden="true" size={16} />
            Descargar
          </a>
        </article>
      )) : <p className="patient-empty">Sin documentos clinicos activos.</p>}
    </section>
  );
}
