import { ArrowLeft, ClipboardList } from 'lucide-react';
import { useEffect, useState } from 'react';
import {
  createWaitingListEntry,
  listPatients,
  listProfessionals,
  listRecommendations,
  listSpecialties,
  listWaitingList,
  scheduleFromWaitingList,
  updateWaitingListEntry,
  updateWaitingListStatus
} from './waitingListApi';
import { WaitingListForm } from './WaitingListForm';
import { WaitingListRecommendationPanel } from './WaitingListRecommendationPanel';
import { WaitingListTable } from './WaitingListTable';
import { WaitingListToolbar } from './WaitingListToolbar';
import type { PatientOption, ProfessionalOption, SpecialtyOption, WaitingListEntry, WaitingListPayload, WaitingListRecommendation } from './waitingList.types';
import './waiting-list.css';

interface WaitingListPageProps {
  onBackToLogin: () => void;
}

export function WaitingListPage({ onBackToLogin }: WaitingListPageProps) {
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [professionals, setProfessionals] = useState<ProfessionalOption[]>([]);
  const [specialties, setSpecialties] = useState<SpecialtyOption[]>([]);
  const [entries, setEntries] = useState<WaitingListEntry[]>([]);
  const [recommendations, setRecommendations] = useState<WaitingListRecommendation[]>([]);
  const [selectedEntry, setSelectedEntry] = useState<WaitingListEntry | undefined>();
  const [specialtyFilter, setSpecialtyFilter] = useState('');
  const [professionalFilter, setProfessionalFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('waiting');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function loadCatalogs() {
    const [patientsResponse, professionalsResponse, specialtiesResponse] = await Promise.all([
      listPatients(),
      listProfessionals(),
      listSpecialties()
    ]);
    setPatients(patientsResponse.filter((patient) => patient.status === 'active'));
    setProfessionals(professionalsResponse.filter((professional) => professional.status === 'active'));
    setSpecialties(specialtiesResponse.filter((specialty) => specialty.status === 'active'));
  }

  async function loadEntries() {
    setIsLoading(true);
    setError(null);
    try {
      const response = await listWaitingList({ specialtyId: specialtyFilter, professionalId: professionalFilter, status: statusFilter });
      setEntries(response);
      setSelectedEntry((current) => current ? response.find((entry) => entry.id === current.id) : undefined);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar lista de espera');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCatalogs().then(loadEntries).catch((currentError) => {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar catalogos');
      setIsLoading(false);
    });
  }, []);

  useEffect(() => {
    void loadEntries();
  }, [specialtyFilter, professionalFilter, statusFilter]);

  async function handleSubmit(payload: WaitingListPayload) {
    setError(null);
    try {
      const saved = selectedEntry
        ? await updateWaitingListEntry(selectedEntry.id, payload)
        : await createWaitingListEntry(payload);
      setSelectedEntry(saved);
      await loadEntries();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible guardar el registro');
    }
  }

  async function handleStatus(entry: WaitingListEntry, status: string) {
    setError(null);
    try {
      const updated = await updateWaitingListStatus(entry.id, status);
      setSelectedEntry((current) => current?.id === updated.id ? updated : current);
      await loadEntries();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible actualizar el estado');
    }
  }

  async function handleRecommend(params: { professionalId?: string; specialtyId?: string; startAt: string; endAt: string }) {
    setError(null);
    try {
      setRecommendations(await listRecommendations(params));
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible generar recomendaciones');
    }
  }

  async function handleSchedule(waitingListId: string, payload: { professionalId?: string; startAt: string; endAt: string; appointmentType: 'consultation'; reason: string }) {
    setError(null);
    try {
      const updated = await scheduleFromWaitingList(waitingListId, payload);
      setSelectedEntry(updated);
      setRecommendations((current) => current.filter((recommendation) => recommendation.waitingListId !== waitingListId));
      await loadEntries();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible agendar desde lista de espera');
    }
  }

  return (
    <main className="waiting-page">
      <header className="waiting-page__header">
        <button className="waiting-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="waiting-page__heading">
          <span className="waiting-page__mark"><ClipboardList aria-hidden="true" size={24} /></span>
          <div>
            <p className="waiting-page__eyebrow">I-Clinical Technology</p>
            <h1 className="waiting-page__title">Lista de Espera</h1>
          </div>
        </div>
      </header>

      <WaitingListToolbar
        specialties={specialties}
        professionals={professionals}
        specialtyFilter={specialtyFilter}
        professionalFilter={professionalFilter}
        statusFilter={statusFilter}
        onSpecialtyFilterChange={setSpecialtyFilter}
        onProfessionalFilterChange={setProfessionalFilter}
        onStatusFilterChange={setStatusFilter}
        onRefresh={() => void loadEntries()}
      />

      {error ? <div className="waiting-page__alert" role="alert">{error}</div> : null}

      <section className="waiting-page__content">
        <div className="waiting-page__list-panel">
          {isLoading ? <div className="waiting-page__loading">Cargando lista de espera...</div> : (
            <WaitingListTable
              entries={entries}
              selectedEntryId={selectedEntry?.id}
              onSelect={setSelectedEntry}
              onMarkContacted={(entry) => void handleStatus(entry, 'contacted')}
              onCancel={(entry) => void handleStatus(entry, 'cancelled')}
              onPrepareSchedule={setSelectedEntry}
            />
          )}
        </div>
        <aside className="waiting-page__side-panel">
          <WaitingListForm
            patients={patients}
            professionals={professionals}
            specialties={specialties}
            selectedEntry={selectedEntry}
            onSubmit={handleSubmit}
            onClear={() => setSelectedEntry(undefined)}
          />
          <WaitingListRecommendationPanel
            professionals={professionals}
            specialties={specialties}
            recommendations={recommendations}
            defaultEntry={selectedEntry}
            onRecommend={handleRecommend}
            onSchedule={handleSchedule}
          />
        </aside>
      </section>
    </main>
  );
}
