import { ArrowLeft, UserRound } from 'lucide-react';
import type { PatientSummary } from '../patients/patients.types';

interface PatientPortalHeaderProps {
  patients: PatientSummary[];
  selectedPatientId: string;
  isLoading: boolean;
  onPatientChange: (patientId: string) => void;
  onBackToLogin: () => void;
}

export function PatientPortalHeader({ patients, selectedPatientId, isLoading, onPatientChange, onBackToLogin }: PatientPortalHeaderProps) {
  return (
    <header className="patient-portal__header">
      <button className="patient-portal__back" type="button" onClick={onBackToLogin}>
        <ArrowLeft aria-hidden="true" size={18} />
        Login
      </button>
      <div className="patient-portal__heading">
        <span className="patient-portal__mark"><UserRound aria-hidden="true" size={24} /></span>
        <div>
          <p className="patient-portal__eyebrow">I-Clinical Technology</p>
          <h1>Portal del Paciente</h1>
        </div>
      </div>
      <div className="patient-portal__selector">
        <label htmlFor="patient-portal-selector">Paciente</label>
        <select
          id="patient-portal-selector"
          value={selectedPatientId}
          disabled={isLoading || patients.length === 0}
          onChange={(event) => onPatientChange(event.target.value)}
        >
          {patients.map((patient) => (
            <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}</option>
          ))}
        </select>
      </div>
    </header>
  );
}
