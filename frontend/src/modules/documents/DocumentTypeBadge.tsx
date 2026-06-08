import type { ClinicalDocumentType } from './documents.types';

const labels: Record<ClinicalDocumentType, string> = {
  medical_report: 'Informe',
  laboratory_exam: 'Laboratorio',
  imaging_exam: 'Imagenologia',
  prescription: 'Receta',
  medical_order: 'Orden medica',
  certificate: 'Certificado',
  other: 'Otro'
};

interface DocumentTypeBadgeProps {
  type: ClinicalDocumentType;
}

export function DocumentTypeBadge({ type }: DocumentTypeBadgeProps) {
  return <span className={`document-type-badge document-type-badge--${type}`}>{labels[type] ?? type}</span>;
}
