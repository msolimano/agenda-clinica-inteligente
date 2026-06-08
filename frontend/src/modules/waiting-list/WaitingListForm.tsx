import { Save, UserPlus } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import type { PatientOption, ProfessionalOption, SpecialtyOption, WaitingListEntry, WaitingListPayload } from './waitingList.types';

interface WaitingListFormProps {
  patients: PatientOption[];
  professionals: ProfessionalOption[];
  specialties: SpecialtyOption[];
  selectedEntry?: WaitingListEntry;
  onSubmit: (payload: WaitingListPayload) => Promise<void>;
  onClear: () => void;
}

const emptyPayload: WaitingListPayload = {
  patientId: '',
  specialtyId: '',
  professionalId: '',
  requestedFrom: '',
  requestedTo: '',
  priority: 3,
  availabilityNotes: ''
};

export function WaitingListForm({ patients, professionals, specialties, selectedEntry, onSubmit, onClear }: WaitingListFormProps) {
  const [form, setForm] = useState<WaitingListPayload>(emptyPayload);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (!selectedEntry) {
      setForm({
        ...emptyPayload,
        patientId: patients[0]?.id ?? '',
        specialtyId: specialties[0]?.id ?? ''
      });
      return;
    }

    setForm({
      patientId: selectedEntry.patientId,
      specialtyId: selectedEntry.specialtyId,
      professionalId: selectedEntry.professionalId ?? '',
      requestedFrom: selectedEntry.requestedFrom ?? '',
      requestedTo: selectedEntry.requestedTo ?? '',
      priority: selectedEntry.priority,
      availabilityNotes: selectedEntry.availabilityNotes ?? ''
    });
  }, [patients, selectedEntry, specialties]);

  function updateField(field: keyof WaitingListPayload, value: string | number) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSaving(true);
    try {
      await onSubmit(form);
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="waiting-form" onSubmit={handleSubmit}>
      <div className="waiting-form__header">
        <span className="waiting-form__icon"><UserPlus aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="waiting-form__title">{selectedEntry ? 'Editar registro' : 'Nuevo registro'}</h2>
          <p className="waiting-form__subtitle">Paciente, especialidad y rango deseado.</p>
        </div>
      </div>

      <label className="waiting-form__field">
        <span>Paciente</span>
        <select value={form.patientId} onChange={(event) => updateField('patientId', event.target.value)} required>
          {patients.map((patient) => <option key={patient.id} value={patient.id}>{patient.firstName} {patient.lastName}{patient.documentNumber ? ` · ${patient.documentNumber}` : ''}</option>)}
        </select>
      </label>
      <label className="waiting-form__field">
        <span>Especialidad</span>
        <select value={form.specialtyId} onChange={(event) => updateField('specialtyId', event.target.value)} required>
          {specialties.map((specialty) => <option key={specialty.id} value={specialty.id}>{specialty.name}</option>)}
        </select>
      </label>
      <label className="waiting-form__field">
        <span>Profesional opcional</span>
        <select value={form.professionalId ?? ''} onChange={(event) => updateField('professionalId', event.target.value)}>
          <option value="">Sin preferencia</option>
          {professionals.map((professional) => <option key={professional.id} value={professional.id}>{professional.firstName} {professional.lastName}</option>)}
        </select>
      </label>
      <div className="waiting-form__grid">
        <label className="waiting-form__field">
          <span>Desde</span>
          <input type="date" value={form.requestedFrom ?? ''} onChange={(event) => updateField('requestedFrom', event.target.value)} />
        </label>
        <label className="waiting-form__field">
          <span>Hasta</span>
          <input type="date" value={form.requestedTo ?? ''} onChange={(event) => updateField('requestedTo', event.target.value)} />
        </label>
      </div>
      <label className="waiting-form__field">
        <span>Prioridad</span>
        <input type="number" min="1" max="5" value={form.priority} onChange={(event) => updateField('priority', Number(event.target.value))} />
      </label>
      <label className="waiting-form__field">
        <span>Disponibilidad</span>
        <textarea value={form.availabilityNotes ?? ''} onChange={(event) => updateField('availabilityNotes', event.target.value)} rows={3} />
      </label>

      <div className="waiting-form__actions">
        <button className="waiting-form__button waiting-form__button--secondary" type="button" onClick={onClear}>Limpiar</button>
        <button className="waiting-form__button waiting-form__button--primary" type="submit" disabled={isSaving || !form.patientId || !form.specialtyId}>
          <Save aria-hidden="true" size={17} />
          {isSaving ? 'Guardando' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
