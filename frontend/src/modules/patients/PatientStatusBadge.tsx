import type { PatientStatus } from './patients.types';

interface PatientStatusBadgeProps {
  status: PatientStatus;
}

export function PatientStatusBadge({ status }: PatientStatusBadgeProps) {
  const label = status === 'active' ? 'Activo' : 'Inactivo';
  return <span className={`patient-status patient-status--${status}`}>{label}</span>;
}
