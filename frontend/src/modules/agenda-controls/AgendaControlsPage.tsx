import { ArrowLeft, CalendarCog } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import {
  cancelAgendaBlock,
  createAgendaBlock,
  createOverbooking,
  getOverbookingCapacity,
  listAgendaBlocks,
  listOverbookings,
  listPatients,
  listProfessionals,
  previewAgendaBlock
} from './agendaControlsApi';
import { dateFromInput, endOfDay, startOfDay, toDateInputValue } from './agendaControlsDate';
import { AgendaBlockForm } from './AgendaBlockForm';
import { AgendaBlockPreview } from './AgendaBlockPreview';
import { AgendaBlocksList } from './AgendaBlocksList';
import { OverbookingForm } from './OverbookingForm';
import { OverbookingsList } from './OverbookingsList';
import type { AgendaBlock, AgendaBlockPreview as AgendaBlockPreviewModel, Overbooking, OverbookingCapacity, OverbookingPayload, PatientOption, ProfessionalOption } from './agendaControls.types';
import './agenda-controls.css';

interface AgendaControlsPageProps {
  onBackToLogin: () => void;
}

export function AgendaControlsPage({ onBackToLogin }: AgendaControlsPageProps) {
  const [professionals, setProfessionals] = useState<ProfessionalOption[]>([]);
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [selectedProfessionalId, setSelectedProfessionalId] = useState('');
  const [selectedDate, setSelectedDate] = useState(toDateInputValue(new Date()));
  const [blocks, setBlocks] = useState<AgendaBlock[]>([]);
  const [preview, setPreview] = useState<AgendaBlockPreviewModel | null>(null);
  const [capacity, setCapacity] = useState<OverbookingCapacity | null>(null);
  const [overbookings, setOverbookings] = useState<Overbooking[]>([]);
  const [lastBlockPayload, setLastBlockPayload] = useState<{ professionalId: string; startAt: string; endAt: string; reason: string } | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const selectedRange = useMemo(() => {
    const date = dateFromInput(selectedDate);
    return { from: startOfDay(date), to: endOfDay(date) };
  }, [selectedDate]);

  async function loadCatalogs() {
    setError(null);
    try {
      const [professionalsResponse, patientsResponse] = await Promise.all([listProfessionals(), listPatients()]);
      const activeProfessionals = professionalsResponse.filter((professional) => professional.status === 'active');
      const activePatients = patientsResponse.filter((patient) => patient.status === 'active');
      setProfessionals(activeProfessionals);
      setPatients(activePatients);
      setSelectedProfessionalId((current) => current || activeProfessionals[0]?.id || '');
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar catalogos');
    }
  }

  async function loadControls(professionalId = selectedProfessionalId) {
    if (!professionalId) {
      setBlocks([]);
      setOverbookings([]);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const [blocksResponse, overbookingsResponse] = await Promise.all([
        listAgendaBlocks(professionalId, selectedRange.from, selectedRange.to),
        listOverbookings(professionalId, selectedRange.from, selectedRange.to)
      ]);
      setBlocks(blocksResponse);
      setOverbookings(overbookingsResponse);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar controles de agenda');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCatalogs();
  }, []);

  useEffect(() => {
    if (selectedProfessionalId) {
      void loadControls(selectedProfessionalId);
    }
  }, [selectedProfessionalId, selectedDate]);

  async function handlePreview(payload: { professionalId: string; startAt: string; endAt: string; reason: string }) {
    setError(null);
    try {
      const response = await previewAgendaBlock(payload);
      setPreview(response);
      setLastBlockPayload(payload);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible calcular el preview');
    }
  }

  async function handleCreateBlock(confirmAffectedAppointments: boolean) {
    if (!lastBlockPayload) {
      return;
    }

    setError(null);
    try {
      await createAgendaBlock({ ...lastBlockPayload, confirmAffectedAppointments });
      setPreview(null);
      setLastBlockPayload(null);
      await loadControls(lastBlockPayload.professionalId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible crear el bloqueo');
    }
  }

  async function handleCancelBlock(blockId: string) {
    setError(null);
    try {
      await cancelAgendaBlock(blockId, 'Cancelado desde controles de agenda');
      await loadControls();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cancelar el bloqueo');
    }
  }

  async function handleCapacity(professionalId: string, startAt: string, endAt: string) {
    setError(null);
    try {
      const response = await getOverbookingCapacity(professionalId, startAt, endAt);
      setCapacity(response);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible validar capacidad');
    }
  }

  async function handleCreateOverbooking(payload: OverbookingPayload) {
    setError(null);
    try {
      await createOverbooking(payload);
      setCapacity(null);
      await loadControls(payload.professionalId);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible crear el sobrecupo');
    }
  }

  return (
    <main className="agenda-controls-page">
      <header className="agenda-controls-page__header">
        <button className="agenda-controls-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="agenda-controls-page__heading">
          <span className="agenda-controls-page__mark"><CalendarCog aria-hidden="true" size={24} /></span>
          <div>
            <p className="agenda-controls-page__eyebrow">I-Clinical Technology</p>
            <h1 className="agenda-controls-page__title">Controles de Agenda</h1>
          </div>
        </div>
        <label className="agenda-controls-page__date">
          <span>Fecha de control</span>
          <input type="date" value={selectedDate} onChange={(event) => setSelectedDate(event.target.value)} />
        </label>
      </header>

      {error ? <div className="agenda-controls-page__alert" role="alert">{error}</div> : null}
      {isLoading ? <div className="agenda-controls-page__loading">Cargando controles...</div> : null}

      <section className="agenda-controls-page__content">
        <div className="agenda-controls-page__primary">
          <AgendaBlockForm
            professionals={professionals}
            selectedProfessionalId={selectedProfessionalId}
            onProfessionalChange={setSelectedProfessionalId}
            onPreview={handlePreview}
            onCreate={handleCreateBlock}
            preview={preview}
          />
          <AgendaBlockPreview preview={preview} />
          <AgendaBlocksList blocks={blocks} onCancel={handleCancelBlock} />
        </div>

        <aside className="agenda-controls-page__side">
          <OverbookingForm
            professionals={professionals}
            patients={patients}
            selectedProfessionalId={selectedProfessionalId}
            onProfessionalChange={setSelectedProfessionalId}
            capacity={capacity}
            onCapacity={handleCapacity}
            onCreate={handleCreateOverbooking}
          />
          <OverbookingsList overbookings={overbookings} />
        </aside>
      </section>
    </main>
  );
}
