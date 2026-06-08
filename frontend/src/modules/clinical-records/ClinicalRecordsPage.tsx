import { ArrowLeft, FileText } from 'lucide-react';
import { useEffect, useState } from 'react';
import { ClinicalRecordDetail } from './ClinicalRecordDetail';
import { ClinicalRecordForm } from './ClinicalRecordForm';
import { ClinicalRecordTimeline } from './ClinicalRecordTimeline';
import { createClinicalRecord, getClinicalRecord, listPatientClinicalRecords, listPatients, listProfessionals, updateClinicalRecord, updateClinicalRecordStatus } from './clinicalRecordsApi';
import type { ClinicalRecord, ClinicalRecordPayload, ClinicalRecordSummary, PatientOption, ProfessionalOption } from './clinicalRecords.types';
import './clinical-records.css';

interface ClinicalRecordsPageProps {
  onBackToLogin: () => void;
}

export function ClinicalRecordsPage({ onBackToLogin }: ClinicalRecordsPageProps) {
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [professionals, setProfessionals] = useState<ProfessionalOption[]>([]);
  const [selectedPatientId, setSelectedPatientId] = useState('');
  const [records, setRecords] = useState<ClinicalRecordSummary[]>([]);
  const [selectedRecord, setSelectedRecord] = useState<ClinicalRecord | null>(null);
  const [editingRecord, setEditingRecord] = useState<ClinicalRecord | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadCatalogs() {
    setIsLoading(true);
    setError(null);
    try {
      const [patientsResponse, professionalsResponse] = await Promise.all([listPatients(), listProfessionals()]);
      const activePatients = patientsResponse.filter((patient) => patient.status === 'active');
      const activeProfessionals = professionalsResponse.filter((professional) => professional.status === 'active');
      const initialPatientId = activePatients[0]?.id || '';
      setPatients(activePatients);
      setProfessionals(activeProfessionals);
      setSelectedPatientId(initialPatientId);
      await loadRecords(initialPatientId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar fichas clínicas');
      setIsLoading(false);
    }
  }

  async function loadRecords(patientId = selectedPatientId) {
    setIsLoading(true);
    setError(null);
    try {
      const response = patientId ? await listPatientClinicalRecords(patientId) : [];
      setRecords(response);
      if (response.length) {
        const detail = await getClinicalRecord(response[0].id);
        setSelectedRecord(detail);
      } else {
        setSelectedRecord(null);
      }
      setEditingRecord(null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar fichas clínicas');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCatalogs();
  }, []);

  async function handlePatientChange(patientId: string) {
    setSelectedPatientId(patientId);
    await loadRecords(patientId);
  }

  async function handleSelectRecord(recordId: string) {
    setError(null);
    try {
      const detail = await getClinicalRecord(recordId);
      setSelectedRecord(detail);
      setEditingRecord(null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar detalle de ficha clínica');
    }
  }

  async function handleSubmit(payload: ClinicalRecordPayload, recordId?: string) {
    setError(null);
    try {
      const saved = recordId ? await updateClinicalRecord(recordId, payload) : await createClinicalRecord(payload);
      setSelectedPatientId(saved.patientId);
      const refreshed = await listPatientClinicalRecords(saved.patientId);
      setRecords(refreshed);
      setSelectedRecord(saved);
      setEditingRecord(null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar ficha clínica');
      throw currentError;
    }
  }

  async function handleCloseRecord(record: ClinicalRecord) {
    setError(null);
    try {
      const closed = await updateClinicalRecordStatus(record.id, 'closed');
      const refreshed = await listPatientClinicalRecords(closed.patientId);
      setRecords(refreshed);
      setSelectedRecord(closed);
      setEditingRecord(null);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cerrar ficha clínica');
    }
  }

  return (
    <main className="clinical-records-page">
      <header className="clinical-records-page__header">
        <button className="clinical-records-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="clinical-records-page__heading">
          <span className="clinical-records-page__mark"><FileText aria-hidden="true" size={24} /></span>
          <div>
            <p className="clinical-records-page__eyebrow">I-Clinical Technology</p>
            <h1 className="clinical-records-page__title">Ficha Clínica</h1>
          </div>
        </div>
        <label className="clinical-records-page__selector">
          Paciente
          <select value={selectedPatientId} onChange={(event) => void handlePatientChange(event.target.value)}>
            <option value="">Seleccione paciente</option>
            {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}</option>)}
          </select>
        </label>
      </header>

      {error ? <div className="clinical-records-page__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="clinical-records-page__loading">Cargando fichas clínicas...</div> : null}

      <section className="clinical-records-page__content">
        <aside className="clinical-records-page__side">
          <ClinicalRecordForm
            patients={patients}
            professionals={professionals}
            selectedPatientId={selectedPatientId}
            record={editingRecord}
            onSubmit={handleSubmit}
          />
        </aside>
        <div className="clinical-records-page__main">
          <ClinicalRecordTimeline records={records} selectedId={selectedRecord?.id ?? null} onSelect={handleSelectRecord} />
          <ClinicalRecordDetail record={selectedRecord} onEdit={setEditingRecord} onCloseRecord={handleCloseRecord} />
        </div>
      </section>
    </main>
  );
}
