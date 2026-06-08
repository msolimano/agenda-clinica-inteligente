import { ArrowLeft, RefreshCcw, Search, UsersRound } from 'lucide-react';
import { useEffect, useState } from 'react';
import { createPatient, getPatient, listPatients, updatePatient, updatePatientStatus } from './patientsApi';
import { PatientDetailPanel } from './PatientDetailPanel';
import { PatientForm } from './PatientForm';
import { PatientList } from './PatientList';
import type { Patient, PatientPayload, PatientSummary } from './patients.types';
import './patients.css';

interface PatientsPageProps {
  onBackToLogin: () => void;
}

export function PatientsPage({ onBackToLogin }: PatientsPageProps) {
  const [patients, setPatients] = useState<PatientSummary[]>([]);
  const [selectedPatient, setSelectedPatient] = useState<Patient | undefined>();
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadData(searchValue = search) {
    setIsLoading(true);
    setError(null);

    try {
      const patientsResponse = await listPatients({ search: searchValue, includeInactive: true });
      setPatients(patientsResponse);

      if (selectedPatient && !patientsResponse.some((item) => item.id === selectedPatient.id)) {
        setSelectedPatient(undefined);
      }
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar pacientes');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadData('');
  }, []);

  async function handleSelect(patient: PatientSummary) {
    setError(null);

    try {
      setSelectedPatient(await getPatient(patient.id));
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar la ficha del paciente');
    }
  }

  async function handleSubmit(payload: PatientPayload) {
    setError(null);

    try {
      const saved = selectedPatient
        ? await updatePatient(selectedPatient.id, payload)
        : await createPatient(payload);
      setSelectedPatient(saved);
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar el paciente');
    }
  }

  async function handleToggleStatus(patient: PatientSummary) {
    setError(null);

    try {
      const updated = await updatePatientStatus(patient.id, patient.status !== 'active');
      setSelectedPatient((current) => (current?.id === updated.id ? updated : current));
      await loadData();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar el estado');
    }
  }

  return (
    <main className="patients-page">
      <header className="patients-page__header">
        <button className="patients-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="patients-page__heading">
          <span className="patients-page__mark"><UsersRound aria-hidden="true" size={24} /></span>
          <div>
            <p className="patients-page__eyebrow">I-Clinical Technology</p>
            <h1 className="patients-page__title">Gestión de Pacientes</h1>
          </div>
        </div>
        <button className="patients-page__refresh" type="button" onClick={() => void loadData()}>
          <RefreshCcw aria-hidden="true" size={17} />
          Actualizar
        </button>
      </header>

      <section className="patients-toolbar" aria-label="Filtros de pacientes">
        <label className="patients-toolbar__search">
          <Search aria-hidden="true" size={18} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && void loadData()} placeholder="Buscar por nombre, documento, correo o teléfono" />
        </label>
        <button className="patients-toolbar__button" type="button" onClick={() => void loadData()}>Buscar</button>
      </section>

      {error ? <div className="patients-page__alert" role="alert">{error}</div> : null}

      <section className="patients-page__content">
        <div className="patients-page__list-panel">
          {isLoading ? <div className="patients-page__loading">Cargando pacientes...</div> : (
            <PatientList
              patients={patients}
              selectedPatientId={selectedPatient?.id}
              onSelect={(patient) => void handleSelect(patient)}
              onToggleStatus={(patient) => void handleToggleStatus(patient)}
            />
          )}
        </div>

        <aside className="patients-page__side-panel">
          <PatientForm
            patient={selectedPatient}
            onSubmit={handleSubmit}
            onCancel={() => setSelectedPatient(undefined)}
          />
          <PatientDetailPanel patient={selectedPatient} />
        </aside>
      </section>
    </main>
  );
}
