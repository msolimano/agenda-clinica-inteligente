import { Download } from 'lucide-react';
import { downloadClinicalDocumentUrl } from './documentsApi';

interface DownloadActionProps {
  documentId: string;
  fileName: string;
}

export function DownloadAction({ documentId, fileName }: DownloadActionProps) {
  return (
    <a className="document-action document-action--download" href={downloadClinicalDocumentUrl(documentId)} aria-label={`Descargar ${fileName}`}>
      <Download aria-hidden="true" size={17} />
      Descargar
    </a>
  );
}
