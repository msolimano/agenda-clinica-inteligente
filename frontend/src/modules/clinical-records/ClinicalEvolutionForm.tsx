import { Save, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { fromDateTimeLocal, toDateTimeLocal } from './clinicalRecordsDate';
import type { ClinicalEvolution, ClinicalEvolutionPayload, ClinicalEvolutionStatus } from './clinicalEvolutions.types';

interface ClinicalEvolutionFormProps {
  evolution: ClinicalEvolution | null;
  disabled: boolean;
  onCancel: () => void;
  onSubmit: (payload: ClinicalEvolutionPayload, evolutionId?: string) => Promise<void>;
}

export function ClinicalEvolutionForm({ evolution, disabled, onCancel, onSubmit }: ClinicalEvolutionFormProps) {
  const [evolutionDate, setEvolutionDate] = useState(toDateTimeLocal(null));
  const [subjective, setSubjective] = useState('');
  const [objective, setObjective] = useState('');
  const [assessment, setAssessment] = useState('');
  const [plan, setPlan] = useState('');
  const [notes, setNotes] = useState('');
  const [evolutionStatus, setEvolutionStatus] = useState<ClinicalEvolutionStatus>('draft');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setEvolutionDate(toDateTimeLocal(evolution?.evolutionDate));
    setSubjective(evolution?.subjective ?? '');
    setObjective(evolution?.objective ?? '');
    setAssessment(evolution?.assessment ?? '');
    setPlan(evolution?.plan ?? '');
    setNotes(evolution?.notes ?? '');
    setEvolutionStatus(evolution?.evolutionStatus ?? 'draft');
  }, [evolution]);

  const hasSoapContent = [subjective, objective, assessment, plan].some((value) => value.trim().length > 0);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!hasSoapContent || !evolutionDate || disabled) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({
        evolutionDate: fromDateTimeLocal(evolutionDate),
        subjective,
        objective,
        assessment,
        plan,
        notes,
        evolutionStatus
      }, evolution?.id);
      if (!evolution) {
        setEvolutionDate(toDateTimeLocal(null));
        setSubjective('');
        setObjective('');
        setAssessment('');
        setPlan('');
        setNotes('');
        setEvolutionStatus('draft');
      }
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="clinical-evolution-form" onSubmit={handleSubmit}>
      <div className="clinical-evolution-form__header">
        <h3>{evolution ? 'Editar evolucion SOAP' : 'Nueva evolucion SOAP'}</h3>
        <button className="clinical-evolution-form__close" type="button" onClick={onCancel}>
          <X aria-hidden="true" size={16} />
        </button>
      </div>

      <label className="clinical-evolution-field">
        Fecha de evolucion
        <input type="datetime-local" value={evolutionDate} onChange={(event) => setEvolutionDate(event.target.value)} disabled={disabled} required />
      </label>

      <label className="clinical-evolution-field">
        Estado
        <select value={evolutionStatus} onChange={(event) => setEvolutionStatus(event.target.value as ClinicalEvolutionStatus)} disabled={disabled}>
          <option value="draft">Borrador</option>
          <option value="active">Activa</option>
          <option value="corrected">Corregida</option>
          <option value="cancelled">Cancelada</option>
        </select>
      </label>

      <label className="clinical-evolution-field clinical-evolution-field--wide">
        Subjective
        <textarea value={subjective} onChange={(event) => setSubjective(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <label className="clinical-evolution-field clinical-evolution-field--wide">
        Objective
        <textarea value={objective} onChange={(event) => setObjective(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <label className="clinical-evolution-field clinical-evolution-field--wide">
        Assessment
        <textarea value={assessment} onChange={(event) => setAssessment(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <label className="clinical-evolution-field clinical-evolution-field--wide">
        Plan
        <textarea value={plan} onChange={(event) => setPlan(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <label className="clinical-evolution-field clinical-evolution-field--wide">
        Notas adicionales
        <textarea value={notes} onChange={(event) => setNotes(event.target.value)} disabled={disabled} rows={3} />
      </label>

      {!hasSoapContent ? <p className="clinical-evolution-form__hint">Ingrese contenido en al menos un campo SOAP.</p> : null}
      <button className="clinical-evolution-form__submit" type="submit" disabled={disabled || isSaving || !hasSoapContent || !evolutionDate}>
        <Save aria-hidden="true" size={16} />
        Guardar evolucion
      </button>
    </form>
  );
}
