import { CalendarPlus, Save, X } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { formatTime } from './appointmentsDate';
import type { Appointment, AppointmentPayload, AppointmentReschedulePayload, AppointmentSlot, AppointmentType, PatientOption } from './appointments.types';

interface AppointmentFormProps {
  professionalId: string;
  patients: PatientOption[];
  slots: AppointmentSlot[];
  reschedulingAppointment?: Appointment;
  onCreate: (payload: AppointmentPayload) => Promise<void>;
  onReschedule: (appointmentId: string, payload: AppointmentReschedulePayload) => Promise<void>;
  onClearReschedule: () => void;
}

export function AppointmentForm({ professionalId, patients, slots, reschedulingAppointment, onCreate, onReschedule, onClearReschedule }: AppointmentFormProps) {
  const availableSlots = useMemo(() => slots.filter((slot) => slot.status === 'available'), [slots]);
  const [patientId, setPatientId] = useState('');
  const [slotKey, setSlotKey] = useState('');
  const [appointmentType, setAppointmentType] = useState<AppointmentType>('consultation');
  const [reason, setReason] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setPatientId(patients[0]?.id ?? '');
  }, [patients]);

  useEffect(() => {
    setSlotKey(availableSlots[0] ? `${availableSlots[0].startAt}|${availableSlots[0].endAt}` : '');
  }, [availableSlots]);

  useEffect(() => {
    if (reschedulingAppointment) {
      setReason(reschedulingAppointment.reason ?? '');
      setAppointmentType(reschedulingAppointment.appointmentType);
    }
  }, [reschedulingAppointment]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const [startAt, endAt] = slotKey.split('|');
    if (!startAt || !endAt) {
      return;
    }

    setIsSaving(true);
    try {
      if (reschedulingAppointment) {
        await onReschedule(reschedulingAppointment.id, { startAt, endAt, reason });
        onClearReschedule();
      } else {
        await onCreate({ professionalId, patientId, startAt, endAt, appointmentType, reason });
      }
      setReason('');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="appointment-form" onSubmit={handleSubmit}>
      <div className="appointment-form__header">
        <span className="appointment-form__icon"><CalendarPlus aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="appointment-form__title">{reschedulingAppointment ? 'Reagendar cita' : 'Nueva reserva'}</h2>
          <p className="appointment-form__subtitle">{reschedulingAppointment?.patientName ?? 'Seleccione paciente y cupo disponible.'}</p>
        </div>
        {reschedulingAppointment ? (
          <button className="appointment-form__clear" type="button" onClick={onClearReschedule} aria-label="Cancelar reagendamiento">
            <X aria-hidden="true" size={16} />
          </button>
        ) : null}
      </div>

      {!reschedulingAppointment ? (
        <label className="appointment-form__field">
          <span>Paciente</span>
          <select value={patientId} onChange={(event) => setPatientId(event.target.value)} required>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.id}>
                {patient.firstName} {patient.lastName}{patient.documentNumber ? ` · ${patient.documentNumber}` : ''}
              </option>
            ))}
          </select>
        </label>
      ) : null}

      <label className="appointment-form__field">
        <span>Cupo</span>
        <select value={slotKey} onChange={(event) => setSlotKey(event.target.value)} required>
          {availableSlots.map((slot) => (
            <option key={`${slot.startAt}|${slot.endAt}`} value={`${slot.startAt}|${slot.endAt}`}>
              {formatTime(slot.startAt)} - {formatTime(slot.endAt)}
            </option>
          ))}
        </select>
      </label>

      {!reschedulingAppointment ? (
        <label className="appointment-form__field">
          <span>Tipo</span>
          <select value={appointmentType} onChange={(event) => setAppointmentType(event.target.value as AppointmentType)}>
            <option value="consultation">Consulta</option>
            <option value="control">Control</option>
            <option value="procedure">Procedimiento</option>
          </select>
        </label>
      ) : null}

      <label className="appointment-form__field">
        <span>Motivo</span>
        <textarea value={reason} onChange={(event) => setReason(event.target.value)} rows={3} placeholder="Motivo de consulta" />
      </label>

      <button className="appointment-form__submit" type="submit" disabled={isSaving || !slotKey || (!reschedulingAppointment && !patientId)}>
        <Save aria-hidden="true" size={17} />
        {isSaving ? 'Guardando' : reschedulingAppointment ? 'Reagendar' : 'Reservar'}
      </button>
    </form>
  );
}
