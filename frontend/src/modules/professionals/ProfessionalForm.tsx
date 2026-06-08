import { Save, UserPlus } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import type { Professional, ProfessionalPayload, Specialty } from './professionals.types';

interface ProfessionalFormProps {
  professional?: Professional;
  specialties: Specialty[];
  onSubmit: (payload: ProfessionalPayload) => Promise<void>;
  onCancel: () => void;
}

const emptyForm: ProfessionalPayload = {
  documentType: 'RUN',
  documentNumber: '',
  registryNumber: '',
  firstName: '',
  lastName: '',
  email: '',
  phone: '',
  specialties: []
};

export function ProfessionalForm({ professional, specialties, onSubmit, onCancel }: ProfessionalFormProps) {
  const [form, setForm] = useState<ProfessionalPayload>(emptyForm);
  const [primarySpecialtyId, setPrimarySpecialtyId] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  const isEditing = Boolean(professional);
  const activeSpecialties = useMemo(() => specialties.filter((specialty) => specialty.status === 'active'), [specialties]);

  useEffect(() => {
    if (!professional) {
      setForm(emptyForm);
      setPrimarySpecialtyId(activeSpecialties[0]?.id ?? '');
      return;
    }

    const primary = professional.specialties.find((specialty) => specialty.primary);
    setForm({
      documentType: professional.documentType ?? 'RUN',
      documentNumber: professional.documentNumber ?? '',
      registryNumber: professional.registryNumber ?? '',
      firstName: professional.firstName,
      lastName: professional.lastName,
      email: professional.email ?? '',
      phone: professional.phone ?? ''
    });
    setPrimarySpecialtyId(primary?.specialtyId ?? activeSpecialties[0]?.id ?? '');
  }, [activeSpecialties, professional]);

  function updateField(field: keyof ProfessionalPayload, value: string) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSaving(true);

    try {
      await onSubmit({
        ...form,
        specialties: isEditing || !primarySpecialtyId ? undefined : [{ specialtyId: primarySpecialtyId, primary: true }]
      });
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="professional-form" onSubmit={handleSubmit}>
      <div className="professional-form__header">
        <span className="professional-form__icon"><UserPlus aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="professional-form__title">{isEditing ? 'Editar profesional' : 'Nuevo profesional'}</h2>
          <p className="professional-form__subtitle">Datos base y especialidad principal.</p>
        </div>
      </div>

      <div className="professional-form__grid">
        <label className="professional-form__field">
          <span>Nombre</span>
          <input value={form.firstName} onChange={(event) => updateField('firstName', event.target.value)} required />
        </label>
        <label className="professional-form__field">
          <span>Apellido</span>
          <input value={form.lastName} onChange={(event) => updateField('lastName', event.target.value)} required />
        </label>
        <label className="professional-form__field">
          <span>Tipo documento</span>
          <input value={form.documentType ?? ''} onChange={(event) => updateField('documentType', event.target.value)} />
        </label>
        <label className="professional-form__field">
          <span>Número documento</span>
          <input value={form.documentNumber ?? ''} onChange={(event) => updateField('documentNumber', event.target.value)} />
        </label>
        <label className="professional-form__field professional-form__field--wide">
          <span>Número registro</span>
          <input value={form.registryNumber ?? ''} onChange={(event) => updateField('registryNumber', event.target.value)} />
        </label>
        <label className="professional-form__field professional-form__field--wide">
          <span>Correo electrónico</span>
          <input type="email" value={form.email ?? ''} onChange={(event) => updateField('email', event.target.value)} />
        </label>
        <label className="professional-form__field professional-form__field--wide">
          <span>Teléfono</span>
          <input value={form.phone ?? ''} onChange={(event) => updateField('phone', event.target.value)} />
        </label>
        {!isEditing ? (
          <label className="professional-form__field professional-form__field--wide">
            <span>Especialidad principal</span>
            <select value={primarySpecialtyId} onChange={(event) => setPrimarySpecialtyId(event.target.value)} required>
              {activeSpecialties.map((specialty) => (
                <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
              ))}
            </select>
          </label>
        ) : null}
      </div>

      <div className="professional-form__actions">
        <button className="professional-form__button professional-form__button--secondary" type="button" onClick={onCancel}>Limpiar</button>
        <button className="professional-form__button professional-form__button--primary" type="submit" disabled={isSaving}>
          <Save aria-hidden="true" size={17} />
          {isSaving ? 'Guardando' : 'Guardar'}
        </button>
      </div>
    </form>
  );
}
