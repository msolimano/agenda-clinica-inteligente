import { Download } from 'lucide-react';
import { patientPortalFHIRDownloadUrl } from './patientPortalApi';

interface PatientFHIRExportPanelProps {
  patientId: string;
}

export function PatientFHIRExportPanel({ patientId }: PatientFHIRExportPanelProps) {
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Exportacion FHIR</h2><span>JSON</span></div>
      <p className="patient-portal-card__note">Descarga consolidada compatible con FHIR R4. No incluye archivos binarios.</p>
      <a className="patient-portal-action patient-portal-action--primary" href={patientPortalFHIRDownloadUrl(patientId)}>
        <Download aria-hidden="true" size={16} />
        Descargar Bundle JSON
      </a>
    </section>
  );
}
