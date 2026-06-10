import { useEffect, useState } from 'react';
import { listPatients } from '../patients/patientsApi';
import type { PatientSummary } from '../patients/patients.types';
import { PatientAIConsentPanel } from './PatientAIConsentPanel';
import { PatientAppointmentsPanel } from './PatientAppointmentsPanel';
import { PatientClinicalHistoryPanel } from './PatientClinicalHistoryPanel';
import { PatientDiagnosesPanel } from './PatientDiagnosesPanel';
import { PatientDocumentsPanel } from './PatientDocumentsPanel';
import { PatientFHIRExportPanel } from './PatientFHIRExportPanel';
import { PatientPortalHeader } from './PatientPortalHeader';
import { PatientPrescriptionsPanel } from './PatientPrescriptionsPanel';
import { PatientSummaryCard } from './PatientSummaryCard';
import {
  getPatientPortalAIConsent,
  getPatientPortalAppointments,
  getPatientPortalClinicalHistory,
  getPatientPortalDiagnoses,
  getPatientPortalDocuments,
  getPatientPortalPrescriptions,
  getPatientPortalSummary
} from './patientPortalApi';
import type { PatientPortalData } from './patientPortal.types';
import './patient-portal.css';

interface PatientPortalPageProps {
  onBackToLogin: () => void;
}

export function PatientPortalPage({ onBackToLogin }: PatientPortalPageProps) {
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [data, setData] = useState<PatientPortalData | null>(null);
  const [isLoadingPatients, setIsLoadingPatients] = useState(true);
  const [isLoadingPortal, setIsLoadingPortal] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadPatients() {
    setIsLoadingPatients(true);
    setError(null);
    try {
      const response = await listPatients({ includeInactive: false });
      const activePatients = response.filter((patient) => patient.status === 'active');
      setPatients(activePatients);
      if (!selectedPatientId && activePatients.length) {
        setSelectedPatientId(activePatients[0].id);
      }
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar pacientes');
    } finally {
      setIsLoadingPatients(false);
    }
  }

  async function loadPortal(patientId: string) {
    if (!patientId) {
      setData(null);
      return;
    }
    setIsLoadingPortal(true);
    setError(null);
    try {
      const [summary, appointments, documents, prescriptions, diagnoses, clinicalHistory, aiConsent] = await Promise.all([
        getPatientPortalSummary(patientId),
        getPatientPortalAppointments(patientId),
        getPatientPortalDocuments(patientId),
        getPatientPortalPrescriptions(patientId),
        getPatientPortalDiagnoses(patientId),
        getPatientPortalClinicalHistory(patientId),
        getPatientPortalAIConsent(patientId)
      ]);
      setData({ summary, appointments, documents, prescriptions, diagnoses, clinicalHistory, aiConsent });
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar portal paciente');
      setData(null);
    } finally {
      setIsLoadingPortal(false);
    }
  }

  useEffect(() => {
    void loadPatients();
  }, []);

  useEffect(() => {
    void loadPortal(selectedPatientId);
  }, [selectedPatientId]);

  const isLoading = isLoadingPatients || isLoadingPortal;

  return (
    <main className="patient-portal">
      <PatientPortalHeader
        patients={patients}
        selectedPatientId={selectedPatientId}
        isLoading={isLoading}
        onPatientChange={setSelectedPatientId}
        onBackToLogin={onBackToLogin}
      />

      <section className="patient-portal__demo" role="note">
        <strong>Modo demo: autenticacion real pendiente.</strong>
        <span>Portal solo lectura. La seleccion temporal de paciente sera reemplazada por autenticacion real.</span>
      </section>

      {error ? <div className="patient-portal__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="patient-portal__loading">Cargando informacion del paciente...</div> : null}

      {!isLoading && patients.length === 0 ? <p className="patient-empty patient-empty--page">No existen pacientes activos para visualizar.</p> : null}

      {data ? (
        <section className="patient-portal__grid" aria-label="Informacion consolidada del paciente">
          <PatientSummaryCard summary={data.summary} />
          <PatientAIConsentPanel consent={data.aiConsent} />
          <PatientFHIRExportPanel patientId={selectedPatientId} />
          <PatientAppointmentsPanel appointments={data.appointments} />
          <PatientDocumentsPanel documents={data.documents} />
          <PatientPrescriptionsPanel prescriptions={data.prescriptions} />
          <PatientDiagnosesPanel diagnoses={data.diagnoses} />
          <PatientClinicalHistoryPanel clinicalHistory={data.clinicalHistory} />
        </section>
      ) : null}
    </main>
  );
}
