import { Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import type { DiagnosisCatalog, DiagnosisCatalogPayload } from './diagnosisCatalog.types';

interface DiagnosisCatalogFormProps {
  diagnosis: DiagnosisCatalog | null;
  onSubmit: (payload: DiagnosisCatalogPayload, diagnosisId?: string) => Promise<void>;
  onCancel: () => void;
}

export function DiagnosisCatalogForm({ diagnosis, onSubmit, onCancel }: DiagnosisCatalogFormProps) {
  const [organizationId, setOrganizationId] = useState('');
  const [diagnosisCode, setDiagnosisCode] = useState('');
  const [codeSystem, setCodeSystem] = useState('');
  const [diagnosisDisplay, setDiagnosisDisplay] = useState('');
  const [category, setCategory] = useState('');
  const [description, setDescription] = useState('');
  const [status, setStatus] = useState<'active' | 'inactive'>('active');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setOrganizationId(diagnosis?.organizationId ?? '');
    setDiagnosisCode(diagnosis?.diagnosisCode ?? '');
    setCodeSystem(diagnosis?.codeSystem ?? '');
    setDiagnosisDisplay(diagnosis?.diagnosisDisplay ?? '');
    setCategory(diagnosis?.category ?? '');
    setDescription(diagnosis?.description ?? '');
    setStatus(diagnosis?.status === 'inactive' ? 'inactive' : 'active');
  }, [diagnosis]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!diagnosisDisplay.trim()) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({
        organizationId,
        diagnosisCode,
        codeSystem,
        diagnosisDisplay,
        category,
        description,
        status
      }, diagnosis?.id);
      if (!diagnosis) {
        setDiagnosisCode('');
        setCodeSystem('');
        setDiagnosisDisplay('');
        setCategory('');
        setDescription('');
        setStatus('active');
      }
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <form className="diagnosis-catalog-form" onSubmit={handleSubmit}>
      <div className="diagnosis-catalog-form__header">
        <h2>{diagnosis ? 'Editar diagnostico' : 'Nuevo diagnostico'}</h2>
        <button type="button" onClick={onCancel}>Limpiar</button>
      </div>

      <label className="diagnosis-catalog-form__field diagnosis-catalog-form__field--wide">
        Diagnostico
        <input value={diagnosisDisplay} onChange={(event) => setDiagnosisDisplay(event.target.value)} required />
      </label>

      <label className="diagnosis-catalog-form__field">
        Organizacion
        <input value={organizationId} onChange={(event) => setOrganizationId(event.target.value)} placeholder="Global" />
      </label>

      <label className="diagnosis-catalog-form__field">
        Estado
        <select value={status} onChange={(event) => setStatus(event.target.value as 'active' | 'inactive')}>
          <option value="active">Activo</option>
          <option value="inactive">Inactivo</option>
        </select>
      </label>

      <label className="diagnosis-catalog-form__field">
        Codigo
        <input value={diagnosisCode} onChange={(event) => setDiagnosisCode(event.target.value)} />
      </label>

      <label className="diagnosis-catalog-form__field">
        Sistema codigo
        <input value={codeSystem} onChange={(event) => setCodeSystem(event.target.value)} placeholder="CIE-10 futuro" />
      </label>

      <label className="diagnosis-catalog-form__field diagnosis-catalog-form__field--wide">
        Categoria
        <input value={category} onChange={(event) => setCategory(event.target.value)} />
      </label>

      <label className="diagnosis-catalog-form__field diagnosis-catalog-form__field--wide">
        Descripcion
        <textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={4} />
      </label>

      <button className="diagnosis-catalog-form__submit" type="submit" disabled={isSaving || !diagnosisDisplay.trim()}>
        <Save aria-hidden="true" size={16} />
        Guardar diagnostico
      </button>
    </form>
  );
}
