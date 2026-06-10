import { CalendarDays, FileText, Pill, Stethoscope } from 'lucide-react';
import { formatDate, fullName } from './patientPortalFormat';
import type { PatientPortalSummary } from './patientPortal.types';

interface PatientSummaryCardProps {
  summary: PatientPortalSummary;
}

export function PatientSummaryCard({ summary }: PatientSummaryCardProps) {
  const patient = summary.patient;
  return (
    <section className="patient-portal-card patient-portal-card--summary">
      <div className="patient-portal-card__header">
        <div>
          <p className="patient-portal-card__eyebrow">Resumen paciente</p>
          <h2>{fullName(patient.firstName, patient.lastName)}</h2>
        </div>
        <span className="patient-portal-badge patient-portal-badge--teal">Solo lectura</span>
      </div>
      <div className="patient-summary__grid">
        <Info label="Documento" value={[patient.documentType, patient.documentNumber].filter(Boolean).join(' ') || 'Sin documento'} />
        <Info label="Nacimiento" value={formatDate(patient.birthDate)} />
        <Info label="Correo" value={patient.email ?? 'Sin correo'} />
        <Info label="Telefono" value={patient.phone ?? 'Sin telefono'} />
        <Info label="Contacto emergencia" value={patient.emergencyContactName ?? 'Sin contacto'} />
        <Info label="Relacion" value={patient.emergencyContactRelationship ?? 'Sin relacion'} />
      </div>
      <div className="patient-summary__kpis">
        <Kpi icon={<CalendarDays size={18} />} label="Proximas horas" value={summary.upcomingAppointments} />
        <Kpi icon={<FileText size={18} />} label="Documentos" value={summary.activeDocuments} />
        <Kpi icon={<Pill size={18} />} label="Prescripciones" value={summary.activePrescriptions} />
        <Kpi icon={<Stethoscope size={18} />} label="Fichas" value={summary.clinicalRecords} />
      </div>
    </section>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return <div className="patient-summary__info"><span>{label}</span><strong>{value}</strong></div>;
}

function Kpi({ icon, label, value }: { icon: React.ReactNode; label: string; value: number }) {
  return <div className="patient-summary__kpi"><span>{icon}</span><strong>{value}</strong><small>{label}</small></div>;
}
