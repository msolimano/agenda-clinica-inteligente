import { ClipboardPlus, Gauge, PlusCircle } from 'lucide-react';
import { useMemo, useState } from 'react';
import { fromDateTimeLocalValue, toDateTimeLocalValue } from './agendaControlsDate';
import type { OverbookingCapacity, PatientOption, ProfessionalOption } from './agendaControls.types';
import type { AppointmentType } from '../appointments/appointments.types';

interface OverbookingFormProps {
  professionals: ProfessionalOption[];
  patients: PatientOption[];
  selectedProfessionalId: string;
  onProfessionalChange: (professionalId: string) => void;
  capacity: OverbookingCapacity | null;
  onCapacity: (professionalId: string, startAt: string, endAt: string) => Promise<void>;
  onCreate: (payload: { professionalId: string; patientId: string; startAt: string; endAt: string; appointmentType: AppointmentType; reason: string }) => Promise<void>;
}

export function OverbookingForm({ professionals, patients, selectedProfessionalId, onProfessionalChange, capacity, onCapacity, onCreate }: OverbookingFormProps) {
  const defaultStart = useMemo(() => {
    const value = new Date();
    value.setHours(value.getHours() + 1, 0, 0, 0);
    return toDateTimeLocalValue(value);
  }, []);
  const defaultEnd = useMemo(() => {
    const value = new Date();
    value.setHours(value.getHours() + 2, 0, 0, 0);
    return toDateTimeLocalValue(value);
  }, []);

  const [patientId, setPatientId] = useState('');
  const [startAt, setStartAt] = useState(defaultStart);
  const [endAt, setEndAt] = useState(defaultEnd);
  const [appointmentType, setAppointmentType] = useState<AppointmentType>('consultation');
  const [reason, setReason] = useState('');

  async function handleCapacity() {
    await onCapacity(selectedProfessionalId, fromDateTimeLocalValue(startAt), fromDateTimeLocalValue(endAt));
  }

  async function handleCreate() {
    await onCreate({
      professionalId: selectedProfessionalId,
      patientId,
      startAt: fromDateTimeLocalValue(startAt),
      endAt: fromDateTimeLocalValue(endAt),
      appointmentType,
      reason
    });
  }

  return (
    <section className="agenda-control-card overbooking-form" aria-label="Crear sobrecupo">
      <div className="agenda-control-card__header">
        <span className="agenda-control-card__icon"><ClipboardPlus aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="agenda-control-card__title">Sobrecupo autorizado</h2>
          <p className="agenda-control-card__subtitle">Valide capacidad antes de reservar.</p>
        </div>
      </div>

      <div className="overbooking-form__grid">
        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Profesional</span>
          <select value={selectedProfessionalId} onChange={(event) => onProfessionalChange(event.target.value)}>
            {professionals.map((professional) => (
              <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>
            ))}
          </select>
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Paciente</span>
          <select value={patientId} onChange={(event) => setPatientId(event.target.value)}>
            <option value="">Seleccione paciente</option>
            {patients.map((patient) => (
              <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}</option>
            ))}
          </select>
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Inicio</span>
          <input type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} />
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Fin</span>
          <input type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} />
        </label>

        <label className="agenda-control-field">
          <span className="agenda-control-field__label">Tipo</span>
          <select value={appointmentType} onChange={(event) => setAppointmentType(event.target.value as AppointmentType)}>
            <option value="consultation">Consulta</option>
            <option value="control">Control</option>
            <option value="procedure">Procedimiento</option>
          </select>
        </label>

        <label className="agenda-control-field agenda-control-field--wide">
          <span className="agenda-control-field__label">Motivo</span>
          <textarea value={reason} maxLength={300} rows={3} onChange={(event) => setReason(event.target.value)} />
        </label>
      </div>

      {capacity ? (
        <div className={`overbooking-capacity ${capacity.remainingOverbookings > 0 && !capacity.blocked ? 'overbooking-capacity--available' : 'overbooking-capacity--closed'}`}>
          <Gauge aria-hidden="true" size={18} />
          <span>{capacity.message}</span>
          <strong>{capacity.remainingOverbookings}/{capacity.maxOverbookings}</strong>
        </div>
      ) : null}

      <div className="overbooking-form__actions">
        <button className="agenda-control-button agenda-control-button--ghost" type="button" onClick={() => void handleCapacity()} disabled={!selectedProfessionalId}>
          <Gauge aria-hidden="true" size={18} />
          Validar cupo
        </button>
        <button className="agenda-control-button agenda-control-button--primary" type="button" onClick={() => void handleCreate()} disabled={!selectedProfessionalId || !patientId || !reason.trim()}>
          <PlusCircle aria-hidden="true" size={18} />
          Crear sobrecupo
        </button>
      </div>
    </section>
  );
}
