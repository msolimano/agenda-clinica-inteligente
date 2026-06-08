import { Award, Plus } from 'lucide-react';
import { FormEvent, useMemo, useState } from 'react';
import type { Professional, Specialty } from './professionals.types';

interface SpecialtyAssignmentProps {
  professional?: Professional;
  specialties: Specialty[];
  onAssign: (specialtyId: string, primary: boolean) => Promise<void>;
}

export function SpecialtyAssignment({ professional, specialties, onAssign }: SpecialtyAssignmentProps) {
  const [specialtyId, setSpecialtyId] = useState('');
  const [primary, setPrimary] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const availableSpecialties = useMemo(() => {
    const assigned = new Set(professional?.specialties.filter((item) => item.status === 'active').map((item) => item.specialtyId));
    return specialties.filter((specialty) => specialty.status === 'active' && !assigned.has(specialty.id));
  }, [professional, specialties]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!professional || !specialtyId) {
      return;
    }

    setIsSaving(true);
    try {
      await onAssign(specialtyId, primary);
      setSpecialtyId('');
      setPrimary(false);
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <section className="specialty-assignment" aria-label="Especialidades del profesional">
      <div className="specialty-assignment__header">
        <span className="specialty-assignment__icon"><Award aria-hidden="true" size={20} /></span>
        <div>
          <h2 className="specialty-assignment__title">Especialidades</h2>
          <p className="specialty-assignment__subtitle">{professional ? `${professional.firstName} ${professional.lastName}` : 'Seleccione un profesional'}</p>
        </div>
      </div>

      <div className="specialty-assignment__chips">
        {professional?.specialties.filter((item) => item.status === 'active').map((specialty) => (
          <span className={`specialty-assignment__chip${specialty.primary ? ' specialty-assignment__chip--primary' : ''}`} key={specialty.id}>
            {specialty.specialtyName}
            {specialty.primary ? <strong>Principal</strong> : null}
          </span>
        ))}
        {professional && professional.specialties.filter((item) => item.status === 'active').length === 0 ? (
          <span className="specialty-assignment__empty">Sin especialidades asociadas.</span>
        ) : null}
      </div>

      <form className="specialty-assignment__form" onSubmit={handleSubmit}>
        <label className="specialty-assignment__field">
          <span>Agregar especialidad</span>
          <select value={specialtyId} onChange={(event) => setSpecialtyId(event.target.value)} disabled={!professional || availableSpecialties.length === 0}>
            <option value="">Seleccione</option>
            {availableSpecialties.map((specialty) => (
              <option key={specialty.id} value={specialty.id}>{specialty.name}</option>
            ))}
          </select>
        </label>
        <label className="specialty-assignment__check">
          <input type="checkbox" checked={primary} onChange={(event) => setPrimary(event.target.checked)} disabled={!professional} />
          Marcar como principal
        </label>
        <button className="specialty-assignment__button" type="submit" disabled={!professional || !specialtyId || isSaving}>
          <Plus aria-hidden="true" size={17} />
          Asociar
        </button>
      </form>
    </section>
  );
}
