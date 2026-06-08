import { Save, UserPlus } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import type { Patient, PatientPayload, PatientSex } from './patients.types';

interface PatientFormProps {
  patient?: Patient;
  onSubmit: (payload: PatientPayload) => Promise<void>;
  onCancel: () => void;
}

const emptyForm: PatientPayload = {
  documentType: 'RUN',
  documentNumber: '',
  firstName: '',
  lastName: '',
  birthDate: '',
  sex: '',
  email: '',
  phone: '',
  address: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  emergencyContactRelationship: ''
};

export function PatientForm({ patient, onSubmit, onCancel }: PatientFormProps) {
  const [form, setForm] = useState<PatientPayload>(emptyForm);
  const [isSaving, setIsSaving] = useState(false);

  const isEditing = Boolean(patient);

  useEffect(() => {
    if (!patient) {
      setForm(emptyForm);
      return;
    }

    setForm({
      documentType: patient.documentType ?? 'RUN',
      documentNumber: patient.documentNumber ?? '',
      firstName: patient.firstName,
      lastName: patient.lastName,
      birthDate: patient.birthDate ?? '',
      sex: patient.sex ?? '',
      email: patient.email ?? '',
      phone: patient.phone ?? '',
      address: patient.address ?? '',
      emergencyContactName: patient.emergencyContactName ?? '',
      emergencyContactPhone: patient.emergencyContactPhone ?? '',
      emergencyContactRelationship: patient.emergencyContactRelationship ?? ''
    });
  }, [patient]);

  function updateField(field: keyof PatientPayload, value: string) {
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
    <form className="patient-form" onSubmit={handleSubmit}>
      <div className="patient-form__header">
        <span className="patient-form__icon"><UserPlus aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="patient-form__title">{isEditing ? 'Editar paciente' : 'Nuevo paciente'}</h2>
          <p className="patient-form__subtitle">Datos demográficos, contacto y emergencia.</p>
        </div>
      </div>

      <div className="patient-form__grid">
        <label className="patient-form__field">
          <span>Nombre</span>
          <input value={form.firstName} onChange={(event) => updateField('firstName', event.target.value)} required />
        </label>
        <label className="patient-form__field">
          <span>Apellido</span>
          <input value={form.lastName} onChange={(event) => updateField('lastName', event.target.value)} required />
        </label>
        <label className="patient-form__field">
          <span>Tipo documento</span>
          <input value={form.documentType ?? ''} onChange={(event) => updateField('documentType', event.target.value)} />
        </label>
        <label className="patient-form__field">
          <span>Número documento</span>
          <input value={form.documentNumber ?? ''} onChange={(event) => updateField('documentNumber', event.target.value)} />
        </label>
        <label className="patient-form__field">
          <span>Fecha nacimiento</span>
          <input type="date" value={form.birthDate ?? ''} onChange={(event) => updateField('birthDate', event.target.value)} />
        </label>
        <label className="patient-form__field">
          <span>Sexo</span>
          <select value={form.sex ?? ''} onChange={(event) => updateField('sex', event.target.value as PatientSex)}>
            <option value="">Sin registrar</option>
            <option value="female">Femenino</option>
            <option value="male">Masculino</option>
            <option value="other">Otro</option>
            <option value="unknown">No informado</option>
          </select>
        </label>
        <label className="patient-form__field">
          <span>Correo electrónico</span>
          <input type="email" value={form.email ?? ''} onChange={(event) => updateField('email', event.target.value)} />
        </label>
        <label className="patient-form__field">
          <span>Teléfono</span>
          <input value={form.phone ?? ''} onChange={(event) => updateField('phone', event.target.value)} />
        </label>
        <label className="patient-form__field patient-form__field--wide">
          <span>Dirección</span>
          <textarea value={form.address ?? ''} onChange={(event) => updateField('address', event.target.value)} rows={3} />
        </label>
        <label className="patient-form__field">
          <span>Contacto emergencia</span>
          <input value={form.emergencyContactName ?? ''} onChange={(event) => updateField('emergencyContactName', event.target.value)} />
        </label>
        <label className="patient-form__field">
          <span>Teléfono emergencia</span>
          <input value={form.emergencyContactPhone ?? ''} onChange={(event) => updateField('emergencyContactPhone', event.target.value)} />
        </label>
        <label className="patient-form__field patient-form__field--wide">
          <span>Relación emergencia</span>
          <input value={form.emergencyContactRelationship ?? ''} onChange={(event) => updateField('emergencyContactRelationship', event.target.value)} />
        </label>
      </div>

      <div className="patient-form__actions">
        <button className="patient-form__button patient-form__button--secondary" type="button" onClick={onCancel}>Limpiar</button>
        <button className="patient-form__button patient-form__button--primary" type="submit" disabled={isSaving}>
          <Save aria-hidden="true" size={17} />
          {isSaving ? 'Guardando' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
