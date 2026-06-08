import { ArrowLeft, CalendarClock } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import {
  cancelAppointment,
  confirmAppointment,
  createAppointment,
  listCalendar,
  listPatients,
  listProfessionals,
  listSlots,
  registerNoShow,
  rescheduleAppointment
} from './appointmentsApi';
import { addDays, dateFromInput, endOfDay, startOfDay, startOfWeek, toDateInputValue } from './appointmentsDate';
import { AgendaToolbar } from './AgendaToolbar';
import { AppointmentForm } from './AppointmentForm';
import { CalendarDayView } from './CalendarDayView';
import { CalendarWeekView } from './CalendarWeekView';
import { ProfessionalSelector } from './ProfessionalSelector';
import type { Appointment, AppointmentPayload, AppointmentReschedulePayload, AppointmentSlot, AppointmentViewMode, PatientOption, ProfessionalOption } from './appointments.types';
import './appointments.css';

interface AppointmentsPageProps {
  onBackToLogin: () => void;
}

export function AppointmentsPage({ onBackToLogin }: AppointmentsPageProps) {
  const [professionals, setProfessionals] = useState<ProfessionalOption[]>([]);
  const [patients, setPatients] = useState<PatientOption[]>([]);
  const [selectedProfessionalId, setSelectedProfessionalId] = useState('');
  const [selectedDate, setSelectedDate] = useState(toDateInputValue(new Date()));
  const [viewMode, setViewMode] = useState<AppointmentViewMode>('day');
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [slots, setSlots] = useState<AppointmentSlot[]>([]);
  const [reschedulingAppointment, setReschedulingAppointment] = useState<Appointment | undefined>();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const selectedDateObject = useMemo(() => dateFromInput(selectedDate), [selectedDate]);
  const weekStart = useMemo(() => startOfWeek(selectedDateObject), [selectedDateObject]);

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
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar profesionales y pacientes');
    }
  }

  async function loadAgenda(professionalId = selectedProfessionalId, dateValue = selectedDate, mode = viewMode) {
    if (!professionalId) {
      setAppointments([]);
      setSlots([]);
      setIsLoading(false);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const date = dateFromInput(dateValue);
      const from = mode === 'week' ? startOfWeek(date) : startOfDay(date);
      const to = mode === 'week' ? addDays(from, 7) : endOfDay(date);
      const [calendarResponse, slotsResponse] = await Promise.all([
        listCalendar(professionalId, from, to),
        listSlots(professionalId, dateValue)
      ]);
      setAppointments(calendarResponse);
      setSlots(slotsResponse);
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cargar la agenda');
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void loadCatalogs();
  }, []);

  useEffect(() => {
    if (selectedProfessionalId) {
      void loadAgenda(selectedProfessionalId, selectedDate, viewMode);
    }
  }, [selectedProfessionalId, selectedDate, viewMode]);

  async function handleCreate(payload: AppointmentPayload) {
    setError(null);
    try {
      await createAppointment(payload);
      await loadAgenda();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible reservar la cita');
    }
  }

  async function handleReschedule(id: string, payload: AppointmentReschedulePayload) {
    setError(null);
    try {
      await rescheduleAppointment(id, payload);
      await loadAgenda();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible reagendar la cita');
    }
  }

  async function handleCancel(appointment: Appointment) {
    setError(null);
    try {
      await cancelAppointment(appointment.id, 'Cancelada desde Agenda Base');
      await loadAgenda();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible cancelar la cita');
    }
  }

  async function handleConfirm(appointment: Appointment) {
    setError(null);
    try {
      await confirmAppointment(appointment.id);
      await loadAgenda();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible confirmar la cita');
    }
  }

  async function handleNoShow(appointment: Appointment) {
    setError(null);
    try {
      await registerNoShow(appointment.id);
      await loadAgenda();
    } catch (currentError) {
      setError(currentError instanceof Error ? currentError.message : 'No fue posible registrar inasistencia');
    }
  }

  return (
    <main className="appointments-page">
      <header className="appointments-page__header">
        <button className="appointments-page__back" type="button" onClick={onBackToLogin}>
          <ArrowLeft aria-hidden="true" size={18} />
          Login
        </button>
        <div className="appointments-page__heading">
          <span className="appointments-page__mark"><CalendarClock aria-hidden="true" size={24} /></span>
          <div>
            <p className="appointments-page__eyebrow">I-Clinical Technology</p>
            <h1 className="appointments-page__title">Agenda Médica</h1>
          </div>
        </div>
        <ProfessionalSelector professionals={professionals} selectedProfessionalId={selectedProfessionalId} onChange={setSelectedProfessionalId} />
      </header>

      <AgendaToolbar
        date={selectedDate}
        viewMode={viewMode}
        onDateChange={setSelectedDate}
        onViewModeChange={setViewMode}
        onRefresh={() => void loadAgenda()}
      />

      {error ? <div className="appointments-page__alert" role="alert">{error}</div> : null}

      <section className="appointments-page__content">
        <div className="appointments-page__calendar-panel">
          {isLoading ? <div className="appointments-page__loading">Cargando agenda...</div> : null}
          {!isLoading && viewMode === 'day' ? (
            <CalendarDayView
              slots={slots}
              appointments={appointments}
              onConfirm={handleConfirm}
              onCancel={handleCancel}
              onNoShow={handleNoShow}
              onReschedule={setReschedulingAppointment}
            />
          ) : null}
          {!isLoading && viewMode === 'week' ? (
            <CalendarWeekView
              weekStart={weekStart}
              appointments={appointments}
              onConfirm={handleConfirm}
              onCancel={handleCancel}
              onNoShow={handleNoShow}
              onReschedule={setReschedulingAppointment}
            />
          ) : null}
        </div>

        <aside className="appointments-page__side-panel">
          <AppointmentForm
            professionalId={selectedProfessionalId}
            patients={patients}
            slots={slots}
            reschedulingAppointment={reschedulingAppointment}
            onCreate={handleCreate}
            onReschedule={handleReschedule}
            onClearReschedule={() => setReschedulingAppointment(undefined)}
          />
          <section className="agenda-summary" aria-label="Resumen de agenda">
            <h2 className="agenda-summary__title">Resumen</h2>
            <div className="agenda-summary__grid">
              <span><strong>{appointments.filter((item) => item.status === 'scheduled').length}</strong> programadas</span>
              <span><strong>{appointments.filter((item) => item.status === 'confirmed').length}</strong> confirmadas</span>
              <span><strong>{slots.filter((item) => item.status === 'available').length}</strong> cupos libres</span>
            </div>
          </section>
        </aside>
      </section>
    </main>
  );
}
