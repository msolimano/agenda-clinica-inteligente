import { Save, X } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { DiagnosisSearchInput } from '../diagnosis-catalog/DiagnosisSearchInput';
import type { DiagnosisCatalog } from '../diagnosis-catalog/diagnosisCatalog.types';
import type { ClinicalDiagnosis, ClinicalDiagnosisPayload, ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';

interface ClinicalDiagnosisFormProps {
  diagnosis: ClinicalDiagnosis | null;
  organizationId?: string | null;
  disabled: boolean;
  onCancel: () => void;
  onSubmit: (payload: ClinicalDiagnosisPayload, diagnosisId?: string) => Promise<void>;
}

export function ClinicalDiagnosisForm({ diagnosis, organizationId, disabled, onCancel, onSubmit }: ClinicalDiagnosisFormProps) {
  const [diagnosisCatalogId, setDiagnosisCatalogId] = useState('');
  const [selectedDiagnosisLabel, setSelectedDiagnosisLabel] = useState<string | null>(null);
  const [diagnosisText, setDiagnosisText] = useState('');
  const [primary, setPrimary] = useState(false);
  const [diagnosisStatus, setDiagnosisStatus] = useState<ClinicalDiagnosisStatus>('suspected');
  const [observations, setObservations] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    setDiagnosisCatalogId(diagnosis?.diagnosisCatalogId ?? '');
    setSelectedDiagnosisLabel(diagnosis?.diagnosisCatalogId ? diagnosis.diagnosisCodeDisplay || diagnosis.diagnosisText : null);
    setDiagnosisText(diagnosis?.diagnosisText ?? '');
    setPrimary(diagnosis?.primary ?? false);
    setDiagnosisStatus(diagnosis?.diagnosisStatus ?? 'suspected');
    setObservations(diagnosis?.observations ?? '');
  }, [diagnosis]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!diagnosisText.trim() || disabled) {
      return;
    }

    setIsSaving(true);
    try {
      await onSubmit({ diagnosisCatalogId: diagnosisCatalogId || null, diagnosisText, primary, diagnosisStatus, observations }, diagnosis?.id);
      if (!diagnosis) {
        setDiagnosisCatalogId('');
        setSelectedDiagnosisLabel(null);
        setDiagnosisText('');
        setPrimary(false);
        setDiagnosisStatus('suspected');
        setObservations('');
      }
    } finally {
      setIsSaving(false);
    }
  }


  function handleDiagnosisSelect(catalogDiagnosis: DiagnosisCatalog) {
    setDiagnosisCatalogId(catalogDiagnosis.id);
    setSelectedDiagnosisLabel(catalogDiagnosis.diagnosisDisplay);
    setDiagnosisText(catalogDiagnosis.diagnosisDisplay);
  }

  function handleDiagnosisClear() {
    setDiagnosisCatalogId('');
    setSelectedDiagnosisLabel(null);
  }

  return (
    <form className="clinical-diagnosis-form" onSubmit={handleSubmit}>
      <div className="clinical-diagnosis-form__header">
        <h3>{diagnosis ? 'Editar diagnostico' : 'Nuevo diagnostico'}</h3>
        <button className="clinical-diagnosis-form__close" type="button" onClick={onCancel}>
          <X aria-hidden="true" size={16} />
        </button>
      </div>

      <DiagnosisSearchInput
        organizationId={organizationId}
        disabled={disabled}
        selectedLabel={selectedDiagnosisLabel}
        onSelect={handleDiagnosisSelect}
        onClear={handleDiagnosisClear}
      />

      <label className="clinical-diagnosis-field clinical-diagnosis-field--wide">
        Diagnostico clinico
        <textarea value={diagnosisText} onChange={(event) => { setDiagnosisText(event.target.value); if (diagnosisCatalogId) { setDiagnosisCatalogId(''); setSelectedDiagnosisLabel(null); } }} disabled={disabled} rows={3} required />
      </label>

      <label className="clinical-diagnosis-field">
        Estado
        <select value={diagnosisStatus} onChange={(event) => setDiagnosisStatus(event.target.value as ClinicalDiagnosisStatus)} disabled={disabled}>
          <option value="suspected">Sospechado</option>
          <option value="confirmed">Confirmado</option>
          <option value="resolved">Resuelto</option>
          <option value="ruled_out">Descartado</option>
        </select>
      </label>

      <label className="clinical-diagnosis-check">
        <input type="checkbox" checked={primary} onChange={(event) => setPrimary(event.target.checked)} disabled={disabled} />
        Diagnostico principal
      </label>

      <label className="clinical-diagnosis-field clinical-diagnosis-field--wide">
        Observaciones
        <textarea value={observations} onChange={(event) => setObservations(event.target.value)} disabled={disabled} rows={3} />
      </label>

      <button className="clinical-diagnosis-form__submit" type="submit" disabled={disabled || isSaving || !diagnosisText.trim()}>
        <Save aria-hidden="true" size={16} />
        Guardar diagnostico
      </button>
    </form>
  );
}
